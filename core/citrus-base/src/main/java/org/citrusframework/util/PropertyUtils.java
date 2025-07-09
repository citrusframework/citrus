/*
 * Copyright the original author or authors.
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

package org.citrusframework.util;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.citrusframework.CitrusSettings;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class supporting property replacement in template files.
 * For usage see doc generators and test case creator.
 *
 * @since 2009
 */
public final class PropertyUtils {

    /** Constant marking a property in template files */
    private static final char PROPERTY_MARKER = '@';
    public static final String BEAN_REF_PREFIX = "#bean:";

    public static final String CITRUS_COMPONENT_ENV_PREFIX = "CITRUS_COMPONENT_";
    public static final String CITRUS_ENDPOINT_ENV_PREFIX = "CITRUS_ENDPOINT_";
    public static final String CITRUS_ENDPOINT_CONFIG_ENV_PREFIX = "CITRUS_ENDPOINT_CONFIG_";
    public static final String CITRUS_COMPONENT_PROPERTY_PREFIX = "citrus.component.";
    public static final String CITRUS_ENDPOINT_PROPERTY_PREFIX = "citrus.endpoint.";

    private static final Logger logger = LoggerFactory.getLogger(PropertyUtils.class);

    /**
     * Prevent instantiation.
     */
    private PropertyUtils() {
        super();
    }

    /**
     * Replaces properties in string.
     *
     * @param line
     * @param propertyResource
     * @return
     */
    public static String replacePropertiesInString(String line, Resource propertyResource) {
        Properties properties = new Properties();
        try {
            properties.load(propertyResource.getInputStream());
        } catch (IOException e) {
            return line;
        }

        return replacePropertiesInString(line, properties);
    }

    /**
     * Replaces properties in string.
     *
     * @param line
     * @param properties
     * @return
     */
    public static String replacePropertiesInString(final String line, Properties properties) {
        StringBuilder newStr = new StringBuilder();

        boolean isVarComplete = false;

        StringBuilder propertyName = new StringBuilder();

        int startIndex = 0;
        int curIndex;
        int searchIndex;
        while ((searchIndex = line.indexOf(PROPERTY_MARKER, startIndex)) != -1) {
            //first check if property Marker is escaped by '\' character
            if (searchIndex != 0 && line.charAt((searchIndex-1)) == '\\') {
                newStr.append(line, startIndex, searchIndex-1);
                newStr.append(PROPERTY_MARKER);
                startIndex = searchIndex + 1;
                continue;
            }

            curIndex = searchIndex + 1;

            while (curIndex < line.length() && !isVarComplete) {
                if ((line.charAt(curIndex) == PROPERTY_MARKER) || (curIndex+1 == line.length())) {
                    isVarComplete = true;
                }

                if (!isVarComplete) {
                    propertyName.append(line.charAt(curIndex));
                }
                ++curIndex;
            }

            if (!properties.containsKey(propertyName.toString())) {
                throw new CitrusRuntimeException("No such property '"
                        + PROPERTY_MARKER + propertyName + PROPERTY_MARKER + "'");
            }

            newStr.append(line, startIndex, searchIndex);
            newStr.append(properties.getProperty(propertyName.toString(), "")); // property value

            startIndex = curIndex;

            propertyName = new StringBuilder();
            isVarComplete = false;
        }

        newStr.append(line.substring(startIndex));

        return newStr.toString();
    }

    /**
     * Configure the given component with environment variables and system properties if present.
     * Supports settings on the component/endpoint itself and on the endpoint configuration.
     */
    public static void configure(String name, Object component, ReferenceResolver referenceResolver) {
        if (component == null || name == null && CitrusSettings.isEnvVarPropertyBindingEnabled()) {
            return;
        }

        if (System.getenv().keySet().stream().noneMatch(PropertyUtils::isComponentEnvVarSetting) &&
                System.getProperties().keySet().stream().map(Object::toString).noneMatch(PropertyUtils::isComponentSystemPropertySetting)) {
            // no matching envVar settings available
            return;
        }

        try {
            if (CitrusSettings.isComponentPropertyBindingEnabled()) {
                bindProperties(CITRUS_COMPONENT_ENV_PREFIX, name, component, referenceResolver);
            }

            if (CitrusSettings.isEndpointPropertyBindingEnabled() && component instanceof Endpoint endpoint) {
                bindProperties(CITRUS_ENDPOINT_ENV_PREFIX, name, endpoint, referenceResolver);
                bindProperties(CITRUS_ENDPOINT_CONFIG_ENV_PREFIX, name, endpoint.getEndpointConfiguration(), referenceResolver);
            }
        } catch (Exception e) {
            logger.warn("Failed to configure envVar properties on component '{}': {}", name, e.getMessage());
            throw e;
        }
    }

    private static void bindProperties(String prefix, String name, Object component, ReferenceResolver referenceResolver) {
        if (component == null) {
            return;
        }

        if (System.getenv().keySet().stream().noneMatch(key -> isComponentEnvVarSetting(key, prefix, name)) &&
                System.getProperties().keySet().stream().map(Object::toString).noneMatch(key -> isComponentSystemPropertySetting(key, prefix, name))) {
            // no matching envVar setting for this component/endpoint
            return;
        }

        ReflectionHelper.doWithMethods(component.getClass(), method -> {
            if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
                // try normal uppercase and exact match property names e.g. logModifier and LOGMODIFIER
                String propertyName = method.getName().substring(3);
                String envName = "%s%s_%s".formatted(prefix, name, propertyName).toUpperCase(Locale.US);
                String sysPropName = "%s%s.%s%s".formatted(prefix.toLowerCase(Locale.US), name,
                                propertyName.substring(0, 1).toLowerCase(Locale.US), propertyName.substring(1)).replaceAll("_", ".");

                String value = CitrusSettings.getPropertyEnvOrDefault(sysPropName, envName, null);
                if (value == null) {
                    // try to use dash style property names e.g. log-modifier and LOG_MODIFIER
                    envName = "%s%s%s".formatted(prefix, name.replaceAll("([A-Z])", "_$1"), propertyName.replaceAll("([A-Z])", "_$1")).toUpperCase(Locale.US);
                    sysPropName = "%s%s.%s%s".formatted(prefix.toLowerCase(Locale.US), name, propertyName.substring(0, 1).toLowerCase(Locale.US), propertyName.substring(1).replaceAll("([A-Z])", "-$1").toLowerCase(Locale.US))
                            .replaceAll("_", ".");
                    value = CitrusSettings.getPropertyEnvOrDefault(sysPropName, envName, null);
                }

                if (value == null) {
                    // no matching envVar setting found
                    return;
                }

                if (value.startsWith(BEAN_REF_PREFIX)) {
                    // resolve bean reference
                    if (referenceResolver.isResolvable(value.substring(BEAN_REF_PREFIX.length()))) {
                        ReflectionHelper.invokeMethod(method, component,
                                TypeConverter.lookupDefault().convertIfNecessary(
                                        referenceResolver.resolve(value.substring(BEAN_REF_PREFIX.length())), method.getParameterTypes()[0]));
                    } else {
                        throw new CitrusRuntimeException("Failed to resolve property bean reference '%s' - no such bean in registry".formatted(value));
                    }
                } else {
                    ReflectionHelper.invokeMethod(method, component,
                            TypeConverter.lookupDefault().convertIfNecessary(value, method.getParameterTypes()[0]));
                }
            }
        });
    }

    private static boolean isComponentSystemPropertySetting(String key) {
        return key.startsWith(CITRUS_COMPONENT_PROPERTY_PREFIX) || key.startsWith(CITRUS_ENDPOINT_PROPERTY_PREFIX);
    }

    private static boolean isComponentEnvVarSetting(String key) {
        return key.startsWith(CITRUS_COMPONENT_ENV_PREFIX) || key.startsWith(CITRUS_ENDPOINT_ENV_PREFIX);
    }

    private static boolean isComponentSystemPropertySetting(String key, String prefix, String name) {
        return key.startsWith("%s%s".formatted(prefix.toLowerCase(Locale.US), name).replaceAll("_", "."));
    }

    private static boolean isComponentEnvVarSetting(String key, String prefix, String name) {
        return key.startsWith("%s%s".formatted(prefix, name)) || key.startsWith("%s%s".formatted(prefix, name.replaceAll("([A-Z])", "_$1")));
    }
}
