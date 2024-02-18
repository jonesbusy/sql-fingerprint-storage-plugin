package io.jenkins.plugins.sql.fingerprint.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.casc.misc.junit.jupiter.WithJenkinsConfiguredWithCode;
import jenkins.fingerprints.GlobalFingerprintConfiguration;
import org.junit.jupiter.api.Test;

@WithJenkinsConfiguredWithCode
public class ConfigurationAsCodeCompatibilityTest {

    @Test
    @ConfiguredWithCode("casc.yml")
    public void shouldSupportConfigurationAsCodeForSQL(JenkinsConfiguredWithCodeRule rule) {
        SqlFingerprintStorage postgreSQLFingerprintStorage =
                (SqlFingerprintStorage) GlobalFingerprintConfiguration.get().getStorage();
        assertThat(postgreSQLFingerprintStorage, is(notNullValue()));
    }
}
