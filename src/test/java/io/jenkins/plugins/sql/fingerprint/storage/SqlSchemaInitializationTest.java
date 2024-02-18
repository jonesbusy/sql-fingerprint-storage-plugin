package io.jenkins.plugins.sql.fingerprint.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import hudson.Util;
import hudson.model.Fingerprint;
import hudson.util.Secret;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import jenkins.fingerprints.GlobalFingerprintConfiguration;
import org.hamcrest.Matchers;
import org.jenkinsci.plugins.database.GlobalDatabaseConfiguration;
import org.jenkinsci.plugins.database.postgresql.PostgreSQLDatabase;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@WithJenkins
@Testcontainers
public class SqlSchemaInitializationTest {

    @Container
    public PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(PostgreSQLContainer.IMAGE);

    public void setConfiguration() throws IOException {
        PostgreSQLDatabase database = new PostgreSQLDatabase(
                postgres.getHost() + ":" + postgres.getMappedPort(5432),
                postgres.getDatabaseName(),
                postgres.getUsername(),
                Secret.fromString(postgres.getPassword()),
                null);
        database.setValidationQuery("SELECT 1");
        GlobalDatabaseConfiguration.get().setDatabase(database);
        SqlFingerprintStorage sqlFingerPrintStorage = SqlFingerprintStorage.get();
        GlobalFingerprintConfiguration.get().setStorage(sqlFingerPrintStorage);
        DatabaseSchemaLoader.migrateSchema();
    }

    @Test
    public void testSchemaInitialization(JenkinsRule rule) throws Exception {
        setConfiguration();
        SqlFingerprintStorage sqlFingerPrintStorage = SqlFingerprintStorage.get();

        try (Connection connection =
                sqlFingerPrintStorage.getConnectionSupplier().connection()) {

            try (PreparedStatement preparedStatement =
                    connection.prepareStatement(Queries.getQuery(Queries.CHECK_FINGERPRINT_TABLE_EXISTS))) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.next(), is(true));
                assertThat(resultSet.getInt(ColumnName.TOTAL), is(1));
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    Queries.getQuery(Queries.CHECK_FINGERPRINT_JOB_BUILD_RELATION_TABLE_EXISTS))) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.next(), is(true));
                assertThat(resultSet.getInt(ColumnName.TOTAL), is(1));
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    Queries.getQuery(Queries.CHECK_FINGERPRINT_FACET_RELATION_TABLE_EXISTS))) {
                ResultSet resultSet = preparedStatement.executeQuery();
                assertThat(resultSet.next(), is(true));
                assertThat(resultSet.getInt(ColumnName.TOTAL), is(1));
            }
        }
    }

    @Test
    public void testSchemaIntializationDoesNotDeleteData(JenkinsRule rule) throws Exception {
        setConfiguration();
        SqlFingerprintStorage sqlFingerPrintStorage = SqlFingerprintStorage.get();

        String id = Util.getDigestOf("testSchemaIntializationDoesNotDeleteData");
        Fingerprint fingerprintSaved = new Fingerprint(null, "foo.jar", Util.fromHexString(id));
        fingerprintSaved.add(id, 3);
        fingerprintSaved.getPersistedFacets().add(new SqlFingerprintStorageTest.TestFacet(fingerprintSaved, 3, id));

        Fingerprint fingerprintLoaded = Fingerprint.load(id);
        assertThat(fingerprintLoaded, is(not(Matchers.nullValue())));
        assertThat(fingerprintLoaded.toString(), is(Matchers.equalTo(fingerprintSaved.toString())));
    }

    @Test
    public void testSchemaIntializationTwice(JenkinsRule rule) throws Exception {
        setConfiguration();
        SqlFingerprintStorage sqlFingerPrintStorage = SqlFingerprintStorage.get();

        String id = Util.getDigestOf("testSchemaIntializationDoesNotDeleteData");
        Fingerprint fingerprintSaved = new Fingerprint(null, "foo.jar", Util.fromHexString(id));
        fingerprintSaved.add(id, 3);
        fingerprintSaved.getPersistedFacets().add(new SqlFingerprintStorageTest.TestFacet(fingerprintSaved, 3, id));

        Fingerprint fingerprintLoaded = Fingerprint.load(id);
        assertThat(fingerprintLoaded, is(not(Matchers.nullValue())));
        assertThat(fingerprintLoaded.toString(), is(Matchers.equalTo(fingerprintSaved.toString())));
    }
}
