package io.jenkins.plugins.sql.fingerprint.sql.storage;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Fingerprint;
import hudson.util.HexBinaryConverter;
import hudson.util.XStream2;
import java.util.ArrayList;

/**
 * Supports ORM to and from JSON using XStream's {@link JettisonMappedXmlDriver} driver.
 */
public class XStreamHandler {

    private static XStream2 XSTREAM = new XStream2(new JettisonMappedXmlDriver());

    /**
     * Returns {@link XStream2} instance.
     */
    static @NonNull XStream2 getXStream() {
        return XSTREAM;
    }

    static {
        XSTREAM.setMode(XStream.NO_REFERENCES);
        XSTREAM.alias(DataConversion.FINGERPRINT, Fingerprint.class);
        XSTREAM.alias(DataConversion.RANGE, Fingerprint.Range.class);
        XSTREAM.alias(DataConversion.RANGES, Fingerprint.RangeSet.class);
        XSTREAM.registerConverter(new HexBinaryConverter(), 10);
        XSTREAM.registerConverter(
                new Fingerprint.RangeSet.ConverterImpl(new CollectionConverter(XSTREAM.getMapper()) {
                    @Override
                    protected Object createCollection(Class type) {
                        return new ArrayList<>();
                    }
                }),
                10);
    }
}
