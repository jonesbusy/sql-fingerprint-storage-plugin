package io.jenkins.plugins.sql.fingerprint.storage;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.thoughtworks.xstream.converters.basic.DateConverter;
import hudson.Util;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class DataConversionTest {

    private static final DateConverter DATE_CONVERTER = new DateConverter();
    public static final String FINGERPRINT_ID = Util.getDigestOf("FINGERPRINT_ID");
    public static final Timestamp TIMESTAMP = new Timestamp(new Date().getTime());
    public static final String FILENAME = "FILENAME";
    public static final String JOB = "JOB";
    public static final int BUILD_NUMBER = 3;

    @Test
    public void testExtractFingerprintMetadata() {
        Map<String, String> fingerprintMetadata = DataConversion.extractFingerprintMetadata(
                FINGERPRINT_ID, TIMESTAMP, FILENAME, JOB, String.valueOf(BUILD_NUMBER));
        assertThat(fingerprintMetadata.get(DataConversion.ID), is(equalTo(FINGERPRINT_ID)));
        assertThat(
                fingerprintMetadata.get(DataConversion.TIMESTAMP),
                is(equalTo(DATE_CONVERTER.toString(new Date(TIMESTAMP.getTime())))));
        assertThat(fingerprintMetadata.get(DataConversion.FILENAME), is(equalTo(FILENAME)));
        assertThat(fingerprintMetadata.get(DataConversion.ORIGINAL_JOB_NAME), is(equalTo(JOB)));
        assertThat(
                fingerprintMetadata.get(DataConversion.ORIGINAL_JOB_BUILD_NUMBER),
                is(equalTo(String.valueOf(BUILD_NUMBER))));

        fingerprintMetadata =
                DataConversion.extractFingerprintMetadata(FINGERPRINT_ID, TIMESTAMP, FILENAME, null, null);
        assertThat(fingerprintMetadata.get(DataConversion.ID), is(equalTo(FINGERPRINT_ID)));
        assertThat(
                fingerprintMetadata.get(DataConversion.TIMESTAMP),
                is(equalTo(DATE_CONVERTER.toString(new Date(TIMESTAMP.getTime())))));
        assertThat(fingerprintMetadata.get(DataConversion.FILENAME), is(equalTo(FILENAME)));
        assertThat(fingerprintMetadata.get(DataConversion.ORIGINAL_JOB_NAME), is(nullValue()));
        assertThat(fingerprintMetadata.get(DataConversion.ORIGINAL_JOB_BUILD_NUMBER), is(nullValue()));
    }
}
