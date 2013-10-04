package com.consol.citrus.admin.configuration;

import java.util.Properties;

/**
 * Abstract run configuration with id handling.
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class AbstractRunConfiguration implements RunConfiguration {

    /** Name of this run configuration */
    private String id;
    private boolean standard = false;
    private Properties systemProperties = new Properties();

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Properties getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(Properties systemProperties) {
        this.systemProperties = systemProperties;
    }

    @Override
    public boolean isStandard() {
        return standard;
    }

    public void setStandard(boolean standard) {
        this.standard = standard;
    }
}
