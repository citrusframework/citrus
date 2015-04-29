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
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.2
 */
public class CitrusConfiguration implements Serializable {

    /** Citrus version to use in archive packages */
    private String citrusVersion;

    /** Automatically adds Citrus dependencies to archive packages */
    private boolean autoPackage = true;

    /**
     * Constructs Citrus configuration instance from Arquillian extension descriptor.
     * @param descriptor
     * @return
     */
    public static CitrusConfiguration from(ArquillianDescriptor descriptor) {
        Map<String, String> extensionProperties = readPropertiesFromDescriptor(descriptor);

        CitrusConfiguration configuration = new CitrusConfiguration();
        configuration.setCitrusVersion(extensionProperties.get("citrusVersion"));

        if (extensionProperties.containsKey("autoPackage")) {
            configuration.setAutoPackage(Boolean.valueOf(extensionProperties.get("autoPackage")));
        }

        return configuration;
    }

    /**
     * Find Citrus extension configuration in descriptor and read properties.
     * @param descriptor
     * @return
     */
    private static Map<String, String> readPropertiesFromDescriptor(ArquillianDescriptor descriptor) {
        for (ExtensionDef extension : descriptor.getExtensions()) {
            if (CitrusExtensionConstants.CITRUS_EXTENSION_QUALIFIER.equals(extension.getExtensionName())) {
                return extension.getExtensionProperties();
            }
        }

        return Collections.emptyMap();
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
}
