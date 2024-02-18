package io.jenkins.plugins.sql.fingerprint.storage;

import static org.hamcrest.text.IsEqualCompressingWhiteSpace.equalToCompressingWhiteSpace;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import hudson.Util;
import hudson.model.Fingerprint;
import hudson.util.Secret;
import io.jenkins.plugins.database.mariadb.MariaDbDatabase;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import jenkins.fingerprints.FingerprintStorage;
import jenkins.fingerprints.GlobalFingerprintConfiguration;
import jenkins.model.FingerprintFacet;
import org.hamcrest.Matchers;
import org.jenkinsci.main.modules.instance_identity.InstanceIdentity;
import org.jenkinsci.plugins.database.AbstractRemoteDatabase;
import org.jenkinsci.plugins.database.GlobalDatabaseConfiguration;
import org.jenkinsci.plugins.database.postgresql.PostgreSQLDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@WithJenkins
@Testcontainers
public class SqlFingerprintStorageTest {

    // Tested databases
    private static Stream<String> databases() {
        return Stream.of("postgresql", "mariadb");
    }

    // Test containers
    @Container
    public PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.1");

    @Container
    public MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.2.2");

    public void setConfiguration(String type) throws IOException {

        // The remote database configuration
        AbstractRemoteDatabase database;

        // PostgreSQL
        if (type.equals("postgresql")) {
            database = new PostgreSQLDatabase(
                    postgres.getHost() + ":" + postgres.getMappedPort(5432),
                    postgres.getDatabaseName(),
                    postgres.getUsername(),
                    Secret.fromString(postgres.getPassword()),
                    null);
            database.setValidationQuery("SELECT 1");
        }

        // MariaDB
        else if (type.equals("mariadb")) {
            database = new MariaDbDatabase(
                    mariadb.getHost() + ":" + mariadb.getMappedPort(3306),
                    mariadb.getDatabaseName(),
                    mariadb.getUsername(),
                    Secret.fromString(mariadb.getPassword()),
                    null);
            database.setValidationQuery("SELECT 1");
        } else {
            throw new IllegalArgumentException("Invalid database type");
        }

        // Set configuration
        GlobalDatabaseConfiguration.get().setDatabase(database);
        SqlFingerprintStorage postgreSQLFingerprintStorage = SqlFingerprintStorage.get();
        GlobalFingerprintConfiguration.get().setStorage(postgreSQLFingerprintStorage);
        DatabaseSchemaLoader.migrateSchema();
    }

    @ParameterizedTest
    @MethodSource("databases")
    public void checkFingerprintStorage(String database, JenkinsRule j) throws IOException {
        setConfiguration(database);
        Object fingerprintStorage = FingerprintStorage.get();
        assertThat(fingerprintStorage, instanceOf(SqlFingerprintStorage.class));
    }

    @ParameterizedTest
    @MethodSource("databases")
    public void testSave(String database, JenkinsRule j) throws IOException, SQLException {
        setConfiguration(database);

        String instanceId = Util.getDigestOf(
                new ByteArrayInputStream(InstanceIdentity.get().getPublic().getEncoded()));
        String id = Util.getDigestOf("testSave");
        Fingerprint fingerprint = new Fingerprint(null, "foo.jar", Util.fromHexString(id));
        fingerprint.add("a", 3);
        fingerprint.getPersistedFacets().add(new TestFacet(fingerprint, 3, "a"));

        try (Connection connection =
                SqlFingerprintStorage.get().getConnectionSupplier().connection()) {
            try (PreparedStatement preparedStatement =
                    connection.prepareStatement(Queries.getQuery(database, Queries.SELECT_FINGERPRINT))) {
                preparedStatement.setString(1, id);
                preparedStatement.setString(2, instanceId);
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.next(), is(true));
                assertThat(
                        resultSet.getTimestamp(ColumnName.TIMESTAMP).getTime(),
                        is(fingerprint.getTimestamp().getTime()));
                assertThat(resultSet.getString(ColumnName.FILENAME), is(fingerprint.getFileName()));
                assertThat(resultSet.getString(ColumnName.ORIGINAL_JOB_NAME), is(nullValue()));
                assertThat(resultSet.getString(ColumnName.ORIGINAL_JOB_BUILD_NUMBER), is(nullValue()));
                assertThat(
                        resultSet.getString(ColumnName.USAGES).replaceAll(" ", ""),
                        is(equalToCompressingWhiteSpace("[{\"job\":\"a\",\"build_number\":3}]")));
                assertThat(
                        resultSet.getString(ColumnName.FACETS).replaceAll(" ", ""),
                        is(equalToCompressingWhiteSpace("[{"
                                + "\"facet_name\":\"io.jenkins.plugins.sql.fingerprint.storage.SqlFingerprintStorageTest$TestFacet\","
                                + "\"facet_entry\":{\"property\":\"a\",\"timestamp\":3}}]")));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("databases")
    public void roundTripEmptyFingerprint(String database, JenkinsRule j) throws IOException {
        setConfiguration(database);
        String id = Util.getDigestOf("roundTrip");

        Fingerprint fingerprintSaved = new Fingerprint(null, "foo.jar", Util.fromHexString(id));

        Fingerprint fingerprintLoaded = Fingerprint.load(id);
        assertThat(fingerprintLoaded, is(not(Matchers.nullValue())));
        assertThat(fingerprintSaved.toString(), is(Matchers.equalTo(fingerprintLoaded.toString())));
    }

    @ParameterizedTest
    @MethodSource("databases")
    public void roundTripWithMultipleFingerprints(String database, JenkinsRule j) throws IOException {
        setConfiguration(database);

        String[] fingerprintIds = {
            Util.getDigestOf("id1"), Util.getDigestOf("id2"), Util.getDigestOf("id3"),
        };

        List<Fingerprint> savedFingerprints = new ArrayList<>();

        for (String fingerprintId : fingerprintIds) {
            Fingerprint fingerprintSaved = new Fingerprint(null, "foo.jar", Util.fromHexString(fingerprintId));
            fingerprintSaved.add(fingerprintId, 3);
            fingerprintSaved.getPersistedFacets().add(new TestFacet(fingerprintSaved, 3, fingerprintId));
            savedFingerprints.add(fingerprintSaved);
        }

        for (Fingerprint fingerprintSaved : savedFingerprints) {
            Fingerprint fingerprintLoaded = Fingerprint.load(fingerprintSaved.getHashString());
            assertThat(fingerprintLoaded, is(not(Matchers.nullValue())));
            assertThat(fingerprintLoaded.toString(), is(Matchers.equalTo(fingerprintSaved.toString())));
        }
    }

    @ParameterizedTest
    @MethodSource("databases")
    public void roundTripWithMultipleUsages(String database, JenkinsRule j) throws IOException {
        setConfiguration(database);
        String id = Util.getDigestOf("roundTripWithUsages");

        Fingerprint fingerprintSaved = new Fingerprint(null, "foo.jar", Util.fromHexString(id));
        fingerprintSaved.add("a", 3);
        fingerprintSaved.add("b", 33);
        fingerprintSaved.add("c", 333);

        Fingerprint fingerprintLoaded = Fingerprint.load(id);
        assertThat(fingerprintLoaded, is(not(Matchers.nullValue())));
        assertThat(fingerprintLoaded.toString(), is(Matchers.equalTo(fingerprintSaved.toString())));
    }

    @Test
    public void roundTripWithMultipleFacets(JenkinsRule j) throws IOException {
        String id = Util.getDigestOf("roundTripWithFacets");

        Fingerprint fingerprintSaved = new Fingerprint(null, "foo.jar", Util.fromHexString(id));
        fingerprintSaved.getPersistedFacets().add(new TestFacet(fingerprintSaved, 3, "a"));
        fingerprintSaved.getPersistedFacets().add(new TestFacet(fingerprintSaved, 33, "b"));
        fingerprintSaved.getPersistedFacets().add(new TestFacet(fingerprintSaved, 333, "c"));

        Fingerprint fingerprintLoaded = Fingerprint.load(id);
        assertThat(fingerprintLoaded, is(not(Matchers.nullValue())));
        assertThat(fingerprintLoaded.getHashString(), is(Matchers.equalTo(fingerprintSaved.getHashString())));
        assertThat(fingerprintLoaded.getFileName(), is(Matchers.equalTo(fingerprintSaved.getFileName())));
        assertThat(fingerprintLoaded.getTimestamp(), is(Matchers.equalTo(fingerprintSaved.getTimestamp())));
        assertThat(
                fingerprintSaved.getPersistedFacets(),
                Matchers.containsInAnyOrder(
                        fingerprintLoaded.getPersistedFacets().toArray()));
    }

    @ParameterizedTest
    @MethodSource("databases")
    public void loadingNonExistentFingerprintShouldReturnNull(String database, JenkinsRule j) throws IOException {
        setConfiguration(database);
        String id = Util.getDigestOf("loadingNonExistentFingerprintShouldReturnNull");
        Fingerprint fingerprint = Fingerprint.load(id);
        assertThat(fingerprint, is(Matchers.nullValue()));
    }

    @ParameterizedTest
    @MethodSource("databases")
    public void shouldDeleteFingerprint(String database, JenkinsRule j) throws IOException {
        setConfiguration(database);
        String id = Util.getDigestOf("shouldDeleteFingerprint");
        new Fingerprint(null, "foo.jar", Util.fromHexString(id));
        Fingerprint.delete(id);
        Fingerprint fingerprintLoaded = Fingerprint.load(id);
        assertThat(fingerprintLoaded, is(Matchers.nullValue()));
        Fingerprint.delete(id);
        fingerprintLoaded = Fingerprint.load(id);
        assertThat(fingerprintLoaded, is(Matchers.nullValue()));
    }

    @ParameterizedTest
    @MethodSource("databases")
    public void testIsReady(String database, JenkinsRule j) throws IOException {
        setConfiguration(database);
        FingerprintStorage fingerprintStorage = FingerprintStorage.get();
        assertThat(fingerprintStorage.isReady(), is(false));
        String id = Util.getDigestOf("testIsReady");
        new Fingerprint(null, "foo.jar", Util.fromHexString(id));
        assertThat(fingerprintStorage.isReady(), is(true));
    }

    public static final class TestFacet extends FingerprintFacet {
        final String property;

        public TestFacet(Fingerprint fingerprint, long timestamp, String property) {
            super(fingerprint, timestamp);
            this.property = property;
        }

        @Override
        public String toString() {
            return "TestFacet[" + property + "@" + getTimestamp() + "]";
        }

        @Override
        public boolean equals(Object object) {
            if (object == this) {
                return true;
            }

            if (!(object instanceof TestFacet)) {
                return false;
            }

            TestFacet testFacet = (TestFacet) object;
            return this.toString().equals(testFacet.toString());
        }
    }
}
