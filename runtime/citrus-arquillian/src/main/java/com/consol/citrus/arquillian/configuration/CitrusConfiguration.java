/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.arquillian.configuration;

import com.consol.citrus.arquillian.CitrusExtensionConstants;
import com.consol.citrus.config.CitrusSpringConfig;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * Citrus extension configuration. User can customize properties either by setting extension properties in Arquillian descriptor or
 * in remote extension property file.
 *
 * @author Christoph Deppisch
 * @since 2.2
 */
public final class CitrusConfiguration implements Serializable {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CitrusConfiguration.class);

    /** Citrus version to use in archive packages */
    private String citrusVersion;

    /** Automatically adds Citrus dependencies to archive packages */
    private boolean autoPackage = true;

    /** Test suite name */
    private String suiteName = "citrus-arquillian-suite";

    /** Configuration class loaded when creating Spring application context */
    private Class<? extends CitrusSpringConfig> configurationClass = CitrusSpringConfig.class;

    /** Property set this configuration was created from */
    private final Properties extensionProperties;

    private CitrusConfiguration(Properties extensionProperties) {
        this.extensionProperties = extensionProperties;
    }

    /**
     * Constructs Citrus configuration instance from Arquillian extension descriptor.
     * @param descriptor
     * @return
     */
    public static CitrusConfiguration from(ArquillianDescriptor descriptor) {
        return from(readPropertiesFromDescriptor(descriptor));
    }

    /**
     * Constructs Citrus configuration instance from given property set.
     * @param extensionProperties
     * @return
     */
    public static CitrusConfiguration from(Properties extensionProperties) {
        CitrusConfiguration configuration = new CitrusConfiguration(extensionProperties);
        configuration.setCitrusVersion(getProperty(extensionProperties, "citrusVersion"));

        if (extensionProperties.containsKey("autoPackage")) {
            configuration.setAutoPackage(Boolean.valueOf(getProperty(extensionProperties, "autoPackage")));
        }

        if (extensionProperties.containsKey("suiteName")) {
            configuration.setSuiteName(getProperty(extensionProperties, "suiteName"));
        }

        if (extensionProperties.containsKey("configurationClass")) {
            String configurationClass = getProperty(extensionProperties, "configurationClass");
            try {
                Class<?> configType = Class.forName(configurationClass);

                if (CitrusSpringConfig.class.isAssignableFrom(configType)) {
                    configuration.setConfigurationClass((Class<? extends CitrusSpringConfig>) configType);
                } else {
                    log.warn(String.format("Found invalid Citrus configuration class: %s, must be a subclass of %s", configurationClass, CitrusSpringConfig.class));
                }
            } catch (ClassNotFoundException e) {
                log.warn(String.format("Unable to access Citrus configuration class: %s", configurationClass), e);
            }
        }

        log.debug(String.format("Using Citrus configuration:%n%s", configuration.toString()));

        return configuration;
    }

    /**
     * Try to read property from property set. When not set or null value return null else
     * return String representation of value object.
     * @param extensionProperties
     * @param propertyName
     * @return
     */
    private static String getProperty(Properties extensionProperties, String propertyName) {
        if (extensionProperties.containsKey(propertyName)) {
            Object value = extensionProperties.get(propertyName);
            if (value != null) {
                return value.toString();
            }
        }

        return null;
    }

    /**
     * Find Citrus extension configuration in descriptor and read properties.
     * @param descriptor
     * @return
     */
    private static Properties readPropertiesFromDescriptor(ArquillianDescriptor descriptor) {
        for (ExtensionDef extension : descriptor.getExtensions()) {
            if (CitrusExtensionConstants.CITRUS_EXTENSION_QUALIFIER.equals(extension.getExtensionName())) {
                Properties properties = new Properties();
                properties.putAll(extension.getExtensionProperties());
                return properties;
            }
        }

        return new Properties();
    }

    @Override
    public String toString() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            extensionProperties.store(outputStream, "arquillian-citrus-remote-configuration");
            return outputStream.toString();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Could not write the properties file.", e);
        }
    }

    /**
     * Gets the auto package flag.
     * @return
     */
    public boolean isAutoPackage() {
        return autoPackage;
    }

    /**
     * Sets the auto package flag.
     * @param autoPackage
     */
    public void setAutoPackage(boolean autoPackage) {
        this.autoPackage = autoPackage;
    }

    /**
     * Gets the test suite name.
     * @return
     */
    public String getSuiteName() {
        return suiteName;
    }

    /**
     * Sets the test suite name.
     * @param suiteName
     */
    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    /**
     * Gets the Citrus version.
     * @return
     */
    public String getCitrusVersion() {
        return citrusVersion;
    }

    /**
     * Sets the Citrus version.
     * @param citrusVersion
     */
    public void setCitrusVersion(String citrusVersion) {
        this.citrusVersion = citrusVersion;
    }

    /**
     * Gets the Citrus Spring configuration class.
     * @return
     */
    public Class<? extends CitrusSpringConfig> getConfigurationClass() {
        return configurationClass;
    }

    /**
     * Sets the Citrus Spring configuration class.
     * @param configurationClass
     */
    public void setConfigurationClass(Class<? extends CitrusSpringConfig> configurationClass) {
        this.configurationClass = configurationClass;
    }
}
