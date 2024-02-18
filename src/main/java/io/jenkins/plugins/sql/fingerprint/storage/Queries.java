package io.jenkins.plugins.sql.fingerprint.storage;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Reads SQL queries from {@link #propertiesFileName}.
 */
@Restricted(NoExternalUse.class)
public class Queries {

    static final String INSERT_FINGERPRINT = "insert_fingerprint";
    static final String INSERT_FINGERPRINT_JOB_BUILD_RELATION = "insert_fingerprint_job_build_relation";
    static final String INSERT_FINGERPRINT_FACET_RELATION = "insert_fingerprint_facet_relation";
    static final String SELECT_FINGERPRINT = "select_fingerprint";
    static final String SELECT_FINGERPRINT_EXISTS_FOR_INSTANCE = "select_fingerprint_exists_for_instance";
    static final String DELETE_FINGERPRINT = "delete_fingerprint";
    static final String SELECT_FINGERPRINT_COUNT = "select_fingerprint_count";
    static final String SELECT_FINGERPRINT_JOB_BUILD_RELATION_COUNT = "select_fingerprint_job_build_relation_count";
    static final String SELECT_FINGERPRINT_FACET_RELATION_COUNT = "select_fingerprint_facet_relation_count";

    private static Properties postgresqlProperties;
    private static Properties mysqlProperties;
    private static Properties mariaDbProperties;

    static {

        // Load all properties
        try (InputStream inputStream = Queries.class.getResourceAsStream("postgresql_Queries.properties")) {
            postgresqlProperties = new Properties();
            postgresqlProperties.load(inputStream);
        } catch (IOException e) {
            postgresqlProperties = null;
        }
        try (InputStream inputStream = Queries.class.getResourceAsStream("mariadb_Queries.properties")) {
            mariaDbProperties = new Properties();
            mariaDbProperties.load(inputStream);
        } catch (IOException e) {
            mariaDbProperties = null;
        }
        try (InputStream inputStream = Queries.class.getResourceAsStream("mysql_Queries.properties")) {
            mysqlProperties = new Properties();
            mysqlProperties.load(inputStream);
        } catch (IOException e) {
            mysqlProperties = null;
        }
    }

    /**
     * Returns the SQL query with the given query name from {@link #propertiesFileName}.
     */
    static @NonNull String getQuery(String database, @NonNull String query) throws SQLException {
        if (postgresqlProperties == null) {
            throw new SQLException("Unable to load property file: " + postgresqlProperties);
        }
        if (mariaDbProperties == null) {
            throw new SQLException("Unable to load property file: " + mariaDbProperties);
        }
        if (mysqlProperties == null) {
            throw new SQLException("Unable to load property file: " + mysqlProperties);
        }

        switch (database) {
            case "postgresql":
                return postgresqlProperties.getProperty(query);
            case "mariadb":
                return mariaDbProperties.getProperty(query);
            case "mysql":
                return mysqlProperties.getProperty(query);
            default:
                throw new SQLException("Unknown database: " + database);
        }
    }
}
