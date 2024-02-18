package io.jenkins.plugins.sql.fingerprint.sql.storage;

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
    static final String CHECK_FINGERPRINT_TABLE_EXISTS = "check_fingerprint_table_exists";
    static final String CHECK_FINGERPRINT_JOB_BUILD_RELATION_TABLE_EXISTS =
            "check_fingerprint_job_build_relation_table_exists";
    static final String CHECK_FINGERPRINT_FACET_RELATION_TABLE_EXISTS = "check_fingerprint_facet_relation_table_exists";
    static final String SELECT_FINGERPRINT_COUNT = "select_fingerprint_count";
    static final String SELECT_FINGERPRINT_JOB_BUILD_RELATION_COUNT = "select_fingerprint_job_build_relation_count";
    static final String SELECT_FINGERPRINT_FACET_RELATION_COUNT = "select_fingerprint_facet_relation_count";

    private static final String propertiesFileName = "Queries.properties";
    private static Properties properties;

    static {
        try (InputStream inputStream = Queries.class.getResourceAsStream(propertiesFileName)) {
            properties = new Properties();
            properties.load(inputStream);
        } catch (IOException e) {
            properties = null;
        }
    }

    /**
     * Returns the SQL query with the given query name from {@link #propertiesFileName}.
     */
    static @NonNull String getQuery(@NonNull String query) throws SQLException {
        if (properties == null) {
            throw new SQLException("Unable to load property file: " + propertiesFileName);
        }
        return properties.getProperty(query);
    }
}
