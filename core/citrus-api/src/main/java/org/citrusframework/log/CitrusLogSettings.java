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

package org.citrusframework.log;

import java.util.Set;
import java.util.stream.Stream;

import org.citrusframework.CitrusSettings;

import static java.lang.Boolean.parseBoolean;
import static java.util.stream.Collectors.toSet;

public final class CitrusLogSettings {

    private static final String CITRUS_LOGGER_PROPERTY_PREFIX = "citrus.logger.";
    private static final String CITRUS_LOGGER_ENV_PREFIX = "CITRUS_LOG_";

    /**
     * Default logger modifier keywords
     */
    public static final String LOG_MASK_KEYWORDS_PROPERTY = CITRUS_LOGGER_PROPERTY_PREFIX + "mask.keywords";
    public static final String LOG_MASK_KEYWORDS_ENV = CITRUS_LOGGER_ENV_PREFIX + "MASK_KEYWORDS";
    public static final String LOG_MASK_KEYWORDS_DEFAULT = "password,secret,secretKey";

    /**
     * Flag to enable/disable logger modifier
     */
    public static final String LOG_MODIFIER_PROPERTY = CITRUS_LOGGER_PROPERTY_PREFIX + "modifier";
    public static final String LOG_MODIFIER_ENV = CITRUS_LOGGER_ENV_PREFIX + "MODIFIER";
    public static final String LOG_MODIFIER_DEFAULT = Boolean.TRUE.toString();

    /**
     * Default logger modifier mask value
     */
    public static final String LOG_MASK_VALUE_PROPERTY = CITRUS_LOGGER_PROPERTY_PREFIX + "mask.value";
    public static final String LOG_MASK_VALUE_ENV = CITRUS_LOGGER_ENV_PREFIX + "MASK_VALUE";
    public static final String LOG_MASK_VALUE_DEFAULT = "****";

    /**
     * Flag to enable/disable log mask for XML payload
     */
    public static final String LOG_MASK_XML_PROPERTY = CITRUS_LOGGER_PROPERTY_PREFIX + "mask.xml";
    public static final String LOG_MASK_XML_ENV = CITRUS_LOGGER_ENV_PREFIX + "MASK_XML";
    public static final String LOG_MASK_XML_DEFAULT = Boolean.TRUE.toString();

    /**
     * Flag to enable/disable log mask for Json payload
     */
    public static final String LOG_MASK_JSON_PROPERTY = CITRUS_LOGGER_PROPERTY_PREFIX + "mask.json";
    public static final String LOG_MASK_JSON_ENV = CITRUS_LOGGER_ENV_PREFIX + "MASK_JSON";
    public static final String LOG_MASK_JSON_DEFAULT = Boolean.TRUE.toString();

    /**
     * Flag to enable/disable log mask for YAML payload
     */
    public static final String LOG_MASK_YAML_PROPERTY = CITRUS_LOGGER_PROPERTY_PREFIX + "mask.yaml";
    public static final String LOG_MASK_YAML_ENV = CITRUS_LOGGER_ENV_PREFIX + "MASK_YAML";
    public static final String LOG_MASK_YAML_DEFAULT = Boolean.TRUE.toString();

    /**
     * Flag to enable/disable log mask for key value pairs payload
     */
    public static final String LOG_MASK_KEY_VALUE_PROPERTY = CITRUS_LOGGER_PROPERTY_PREFIX + "mask.key.value";
    public static final String LOG_MASK_KEY_VALUE_ENV = CITRUS_LOGGER_ENV_PREFIX + "MASK_KEY_VALUE";
    public static final String LOG_MASK_KEY_VALUE_DEFAULT = Boolean.TRUE.toString();

    /**
     * Flag to enable/disable log mask for form url encoded payload
     */
    public static final String LOG_MASK_FORM_URL_ENCODED_PROPERTY = CITRUS_LOGGER_PROPERTY_PREFIX + "mask.form.url.encoded";
    public static final String LOG_MASK_FORM_URL_ENCODED_ENV = CITRUS_LOGGER_ENV_PREFIX + "MASK_FORM_URL_ENCODED";
    public static final String LOG_MASK_FORM_URL_ENCODED_DEFAULT = Boolean.TRUE.toString();

    private CitrusLogSettings() {
        //prevent instantiation of utility class
    }

    /**
     * Get logger mask keywords.
     */
    public static Set<String> getLogMaskKeywords() {
        return Stream.of(CitrusSettings.getPropertyEnvOrDefault(
                        LOG_MASK_KEYWORDS_PROPERTY,
                        LOG_MASK_KEYWORDS_ENV,
                        LOG_MASK_KEYWORDS_DEFAULT)
                        .split(","))
                .map(String::trim)
                .collect(toSet());
    }

    /**
     * Gets the logger modifier enabled/disabled setting.
     */
    public static boolean isLogModifierEnabled() {
        return parseBoolean(CitrusSettings.getPropertyEnvOrDefault(
                LOG_MODIFIER_PROPERTY,
                LOG_MODIFIER_ENV,
                LOG_MODIFIER_DEFAULT));
    }

    /**
     * Get logger mask value.
     */
    public static String getLogMaskValue() {
        return CitrusSettings.getPropertyEnvOrDefault(
                LOG_MASK_VALUE_PROPERTY,
                LOG_MASK_VALUE_ENV,
                LOG_MASK_VALUE_DEFAULT);
    }

    /**
     * Gets the mask XML enabled/disabled setting.
     */
    public static boolean isMaskXmlEnabled() {
        return parseBoolean(CitrusSettings.getPropertyEnvOrDefault(
                LOG_MASK_XML_PROPERTY,
                LOG_MASK_XML_ENV,
                LOG_MASK_XML_DEFAULT));
    }

    /**
     * Gets the mask Json enabled/disabled setting.
     */
    public static boolean isMaskJsonEnabled() {
        return parseBoolean(CitrusSettings.getPropertyEnvOrDefault(
                LOG_MASK_JSON_PROPERTY,
                LOG_MASK_JSON_ENV,
                LOG_MASK_JSON_DEFAULT));
    }

    /**
     * Gets the mask YAML enabled/disabled setting.
     */
    public static boolean isMaskYamlEnabled() {
        return parseBoolean(CitrusSettings.getPropertyEnvOrDefault(
                LOG_MASK_YAML_PROPERTY,
                LOG_MASK_YAML_ENV,
                LOG_MASK_YAML_DEFAULT));
    }

    /**
     * Gets the mask kay value pairs enabled/disabled setting.
     */
    public static boolean isMaskKeyValueEnabled() {
        return parseBoolean(CitrusSettings.getPropertyEnvOrDefault(
                LOG_MASK_KEY_VALUE_PROPERTY,
                LOG_MASK_KEY_VALUE_ENV,
                LOG_MASK_KEY_VALUE_DEFAULT));
    }

    /**
     * Gets the mask form url encoded enabled/disabled setting.
     */
    public static boolean isMaskFormUrlEncodedEnabled() {
        return parseBoolean(CitrusSettings.getPropertyEnvOrDefault(
                LOG_MASK_FORM_URL_ENCODED_PROPERTY,
                LOG_MASK_FORM_URL_ENCODED_ENV,
                LOG_MASK_FORM_URL_ENCODED_DEFAULT));
    }
}
