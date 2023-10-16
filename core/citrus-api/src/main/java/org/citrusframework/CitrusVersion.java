package org.citrusframework;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 */
public final class CitrusVersion {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CitrusVersion.class);

    /** Citrus version */
    private static String version;

    /* Load Citrus version */
    static {
        try (final InputStream in = CitrusVersion.class.getClassLoader().getResourceAsStream("META-INF/citrus.version")) {
            Properties versionProperties = new Properties();
            versionProperties.load(in);
            version = versionProperties.get("citrus.version").toString();

            if (version.equals("${project.version}")) {
                logger.warn("Citrus version has not been filtered with Maven project properties yet");
                version = "";
            }
        } catch (IOException e) {
            logger.warn("Unable to read Citrus version information", e);
            version = "";
        }
    }

    /**
     * Prevent instantiation.
     */
    private CitrusVersion() {
        super();
    }

    /**
     * Gets the Citrus version.
     * @return
     */
    public static String version() {
        return version;
    }

}
