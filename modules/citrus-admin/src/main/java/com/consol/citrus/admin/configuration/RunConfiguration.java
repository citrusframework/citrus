package com.consol.citrus.admin.configuration;

import java.util.Properties;

/**
 * @author Christoph Deppisch
 */
public interface RunConfiguration {

    /**
     * Unique name for this run configuration.
     * @return
     */
    String getId();

    /**
     * System properties.
      * @return
     */
    Properties getSystemProperties();
}
