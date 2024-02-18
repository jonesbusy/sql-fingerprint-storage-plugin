package io.jenkins.plugins.sql.fingerprint.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import hudson.Util;
import hudson.model.Fingerprint;
import hudson.util.Secret;
import io.jenkins.plugins.database.mariadb.MariaDbDatabase;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.stream.Stream;

import jenkins.fingerprints.GlobalFingerprintConfiguration;
import org.hamcrest.Matchers;
import org.jenkinsci.plugins.database.AbstractRemoteDatabase;
import org.jenkinsci.plugins.database.GlobalDatabaseConfiguration;
import org.jenkinsci.plugins.database.postgresql.PostgreSQLDatabase;
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
public class SqlSchemaInitializationTest {

    // Tested databases
    private static Stream<String> databases() {
        return Stream.of("postgresql", "mariadb");
    }

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

        GlobalDatabaseConfiguration.get().setDatabase(database);
        SqlFingerprintStorage sqlFingerPrintStorage = SqlFingerprintStorage.get();
        GlobalFingerprintConfiguration.get().setStorage(sqlFingerPrintStorage);
        DatabaseSchemaLoader.migrateSchema();
    }

    @ParameterizedTest
    @MethodSource("databases")
    public void testSchemaInitialization(String database, JenkinsRule rule) throws Exception {
        setConfiguration(database);
        assertThat(DatabaseSchemaLoader.MIGRATED, is(true));
    }

    @ParameterizedTest
    @MethodSource("databases")
    public void testSchemaIntializationDoesNotDeleteData(String database, JenkinsRule rule) throws Exception {
        setConfiguration(database);
        String id = Util.getDigestOf("testSchemaIntializationDoesNotDeleteData");
        Fingerprint fingerprintSaved = new Fingerprint(null, "foo.jar", Util.fromHexString(id));
        fingerprintSaved.add(id, 3);
        fingerprintSaved.getPersistedFacets().add(new SqlFingerprintStorageTest.TestFacet(fingerprintSaved, 3, id));

        Fingerprint fingerprintLoaded = Fingerprint.load(id);
        assertThat(fingerprintLoaded, is(not(Matchers.nullValue())));
        assertThat(fingerprintLoaded.toString(), is(Matchers.equalTo(fingerprintSaved.toString())));
    }

    @ParameterizedTest
    @MethodSource("databases")
    public void testSchemaIntializationTwice(String database, JenkinsRule rule) throws Exception {
        setConfiguration(database);
        String id = Util.getDigestOf("testSchemaIntializationDoesNotDeleteData");
        Fingerprint fingerprintSaved = new Fingerprint(null, "foo.jar", Util.fromHexString(id));
        fingerprintSaved.add(id, 3);
        fingerprintSaved.getPersistedFacets().add(new SqlFingerprintStorageTest.TestFacet(fingerprintSaved, 3, id));

        Fingerprint fingerprintLoaded = Fingerprint.load(id);
        assertThat(fingerprintLoaded, is(not(Matchers.nullValue())));
        assertThat(fingerprintLoaded.toString(), is(Matchers.equalTo(fingerprintSaved.toString())));
    }
}
