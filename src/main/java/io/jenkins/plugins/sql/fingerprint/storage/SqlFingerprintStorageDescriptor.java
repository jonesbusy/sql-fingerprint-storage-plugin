package io.jenkins.plugins.sql.fingerprint.storage;

import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.fingerprints.FingerprintStorageDescriptor;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * Descriptor class for {@link SqlFingerprintStorage}.
 */
@Restricted(NoExternalUse.class)
public class SqlFingerprintStorageDescriptor extends FingerprintStorageDescriptor {

    @Override
    public @NonNull String getDisplayName() {
        return Messages.SqlFingerprintStorage_DisplayName();
    }
}
