package io.jenkins.plugins.sql.fingerprint.storage;

/**
 * Defines constants which hold PostgreSQL table names.
 */
public class ColumnName {

    static final String TIMESTAMP = "timestamp";
    static final String FILENAME = "filename";
    static final String ORIGINAL_JOB_NAME = "original_job_name";
    static final String ORIGINAL_JOB_BUILD_NUMBER = "original_job_build_number";
    static final String USAGES = "usages";
    static final String FACETS = "facets";
    static final String TOTAL = "total";
    static final String EXISTS = "exists";
}
