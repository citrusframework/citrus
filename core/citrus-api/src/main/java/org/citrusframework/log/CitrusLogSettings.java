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
import org.citrusframework.config.CitrusConfigProperties;
import org.citrusframework.config.CitrusConfigProperty;

import static java.lang.Boolean.parseBoolean;
import static java.util.stream.Collectors.toSet;

@CitrusConfigProperties(prefix = "citrus.logger", description = "Citrus logger and log masking settings.")
public final class CitrusLogSettings {

    private static final String CITRUS_LOGGER_PROPERTY_PREFIX = "citrus.logger.";
    private static final String CITRUS_LOGGER_ENV_PREFIX = "CITRUS_LOG_";

    /**
     * Default logger modifier keywords
     */
    @CitrusConfigProperty(description = "Comma-separated keywords to mask in log output.", defaultValue = "password,secret,secretKey")
    public static final String LOG_MASK_KEYWORDS_PROPERTY = CITRUS_LOGGER_PROPERTY_PREFIX + "mask.keywords";
    public static final String LOG_MASK_KEYWORDS_ENV = CITRUS_LOGGER_ENV_PREFIX + "MASK_KEYWORDS";
    public static final String LOG_MASK_KEYWORDS_DEFAULT = "password,secret,secretKey";

    /**
     * Flag to enable/disable logger modifier
     */
    @CitrusConfigProperty(description = "Enable the logger modifier for masking sensitive data.", type = "java.lang.Boolean", defaultValue = "true")
    public static final String LOG_MODIFIER_PROPERTY = CITRUS_LOGGER_PROPERTY_PREFIX + "modifier";
    public static final String LOG_MODIFIER_ENV = CITRUS_LOGGER_ENV_PREFIX + "MODIFIER";
    public static final String LOG_MODIFIER_DEFAULT = Boolean.TRUE.toString();

    /**
     * Default logger modifier mask value
     */
    @CitrusConfigProperty(description = "Replacement value used for masked keywords in log output.", defaultValue = "****")
    public static final String LOG_MASK_VALUE_PROPERTY = CITRUS_LOGGER_PROPERTY_PREFIX + "mask.value";
    public static final String LOG_MASK_VALUE_ENV = CITRUS_LOGGER_ENV_PREFIX + "MASK_VALUE";
    public static final String LOG_MASK_VALUE_DEFAULT = "****";

    /**
     * Flag to enable/disable log mask for XML payload
     */
    @CitrusConfigProperty(description = "Enable log masking for XML payload.", type = "java.lang.Boolean", defaultValue = "true")
    public static final String LOG_MASK_XML_PROPERTY = CITRUS_LOGGER_PROPERTY_PREFIX + "mask.xml";
    public static final String LOG_MASK_XML_ENV = CITRUS_LOGGER_ENV_PREFIX + "MASK_XML";
    public static final String LOG_MASK_XML_DEFAULT = Boolean.TRUE.toString();

    /**
     * Flag to enable/disable log mask for Json payload
     */
    @CitrusConfigProperty(description = "Enable log masking for JSON payload.", type = "java.lang.Boolean", defaultValue = "true")
    public static final String LOG_MASK_JSON_PROPERTY = CITRUS_LOGGER_PROPERTY_PREFIX + "mask.json";
    public static final String LOG_MASK_JSON_ENV = CITRUS_LOGGER_ENV_PREFIX + "MASK_JSON";
    public static final String LOG_MASK_JSON_DEFAULT = Boolean.TRUE.toString();

    /**
     * Flag to enable/disable log mask for YAML payload
     */
    @CitrusConfigProperty(description = "Enable log masking for YAML payload.", type = "java.lang.Boolean", defaultValue = "true")
    public static final String LOG_MASK_YAML_PROPERTY = CITRUS_LOGGER_PROPERTY_PREFIX + "mask.yaml";
    public static final String LOG_MASK_YAML_ENV = CITRUS_LOGGER_ENV_PREFIX + "MASK_YAML";
    public static final String LOG_MASK_YAML_DEFAULT = Boolean.TRUE.toString();

    /**
     * Flag to enable/disable log mask for key value pairs payload
     */
    @CitrusConfigProperty(description = "Enable log masking for key-value pairs payload.", type = "java.lang.Boolean", defaultValue = "true")
    public static final String LOG_MASK_KEY_VALUE_PROPERTY = CITRUS_LOGGER_PROPERTY_PREFIX + "mask.key.value";
    public static final String LOG_MASK_KEY_VALUE_ENV = CITRUS_LOGGER_ENV_PREFIX + "MASK_KEY_VALUE";
    public static final String LOG_MASK_KEY_VALUE_DEFAULT = Boolean.TRUE.toString();

    /**
     * Flag to enable/disable log mask for form url encoded payload
     */
    @CitrusConfigProperty(description = "Enable log masking for form URL encoded payload.", type = "java.lang.Boolean", defaultValue = "true")
    public static final String LOG_MASK_FORM_URL_ENCODED_PROPERTY = CITRUS_LOGGER_PROPERTY_PREFIX + "mask.form.url.encoded";
    public static final String LOG_MASK_FORM_URL_ENCODED_ENV = CITRUS_LOGGER_ENV_PREFIX + "MASK_FORM_URL_ENCODED";
    public static final String LOG_MASK_FORM_URL_ENCODED_DEFAULT = Boolean.TRUE.toString();

    /**
     * Flag to enable/disable message content (headers + body) in log output
     */
    @CitrusConfigProperty(description = "Enable message content (headers and body) in log output.", type = "java.lang.Boolean", defaultValue = "false")
    public static final String LOG_PRINT_MESSAGE_CONTENT_PROPERTY = CITRUS_LOGGER_PROPERTY_PREFIX + "print.message.content";
    public static final String LOG_PRINT_MESSAGE_CONTENT_ENV = CITRUS_LOGGER_ENV_PREFIX + "PRINT_MESSAGE_CONTENT";
    public static final String LOG_PRINT_MESSAGE_CONTENT_DEFAULT = Boolean.FALSE.toString();

    /**
     * Flag to enable/disable inbound message content (headers + body) in log output
     */
    @CitrusConfigProperty(description = "Enable inbound message content (headers and body) in log output.", type = "java.lang.Boolean")
    public static final String LOG_PRINT_INBOUND_MESSAGE_CONTENT_PROPERTY = CITRUS_LOGGER_PROPERTY_PREFIX + "print.inbound.message.content";
    public static final String LOG_PRINT_INBOUND_MESSAGE_CONTENT_ENV = CITRUS_LOGGER_ENV_PREFIX + "PRINT_INBOUND_MESSAGE_CONTENT";

    /**
     * Flag to enable/disable outbound message content (headers + body) in log output
     */
    @CitrusConfigProperty(description = "Enable outbound message content (headers and body) in log output.", type = "java.lang.Boolean")
    public static final String LOG_PRINT_OUTBOUND_MESSAGE_CONTENT_PROPERTY = CITRUS_LOGGER_PROPERTY_PREFIX + "print.outbound.message.content";
    public static final String LOG_PRINT_OUTBOUND_MESSAGE_CONTENT_ENV = CITRUS_LOGGER_ENV_PREFIX + "PRINT_OUTBOUND_MESSAGE_CONTENT";

    /**
     * Layout mode defines how message content gets printed to the log output (summary, verbose, compact, body)
     */
    @CitrusConfigProperty(description = "Layout mode for message content in log output (summary, verbose, compact, body).", defaultValue = "verbose")
    public static final String LOG_PRINT_MESSAGE_LAYOUT_PROPERTY = CITRUS_LOGGER_PROPERTY_PREFIX + "print.message.layout";
    public static final String LOG_PRINT_MESSAGE_LAYOUT_ENV = CITRUS_LOGGER_ENV_PREFIX + "PRINT_MESSAGE_LAYOUT";
    public static final String LOG_PRINT_MESSAGE_LAYOUT_DEFAULT = "verbose";

    /**
     * Maximum length of message body in log output before truncation
     */
    @CitrusConfigProperty(description = "Maximum length of message body in log output before truncation.", type = "java.lang.Long", defaultValue = "2048")
    public static final String LOG_MESSAGE_PAYLOAD_MAX_LENGTH_PROPERTY = CITRUS_LOGGER_PROPERTY_PREFIX + "message.payload.max.length";
    public static final String LOG_MESSAGE_PAYLOAD_MAX_LENGTH_ENV = CITRUS_LOGGER_ENV_PREFIX + "MESSAGE_PAYLOAD_MAX_LENGTH";
    public static final String LOG_MESSAGE_PAYLOAD_MAX_LENGTH_DEFAULT = "2048";

    /**
     * ANSI color mode for log output: auto, always, never
     */
    @CitrusConfigProperty(description = "ANSI color mode for log output (auto, always, never).", defaultValue = "auto")
    public static final String LOG_COLOR_PROPERTY = CITRUS_LOGGER_PROPERTY_PREFIX + "color";
    public static final String LOG_COLOR_ENV = CITRUS_LOGGER_ENV_PREFIX + "COLOR";
    public static final String LOG_COLOR_DEFAULT = "auto";

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

    /**
     * Gets the print message content enabled/disabled setting.
     */
    public static boolean isPrintMessageContentEnabled() {
        return parseBoolean(CitrusSettings.getPropertyEnvOrDefault(
                LOG_PRINT_MESSAGE_CONTENT_PROPERTY,
                LOG_PRINT_MESSAGE_CONTENT_ENV,
                LOG_PRINT_MESSAGE_CONTENT_DEFAULT));
    }

    /**
     * Gets the print inbound message content enabled/disabled setting.
     */
    public static boolean isPrintInboundMessageContentEnabled() {
        return parseBoolean(CitrusSettings.getPropertyEnvOrDefault(
                LOG_PRINT_INBOUND_MESSAGE_CONTENT_PROPERTY,
                LOG_PRINT_INBOUND_MESSAGE_CONTENT_ENV,
                String.valueOf(isPrintMessageContentEnabled())));
    }

    /**
     * Gets the print inbound message content enabled/disabled setting.
     */
    public static boolean isPrintOutboundMessageContentEnabled() {
        return parseBoolean(CitrusSettings.getPropertyEnvOrDefault(
                LOG_PRINT_OUTBOUND_MESSAGE_CONTENT_PROPERTY,
                LOG_PRINT_OUTBOUND_MESSAGE_CONTENT_ENV,
                String.valueOf(isPrintMessageContentEnabled())));
    }

    /**
     * Gets the maximum message body length for log output.
     */
    public static int getMessagePayloadMaxLength() {
        return Integer.parseInt(CitrusSettings.getPropertyEnvOrDefault(
                LOG_MESSAGE_PAYLOAD_MAX_LENGTH_PROPERTY,
                LOG_MESSAGE_PAYLOAD_MAX_LENGTH_ENV,
                LOG_MESSAGE_PAYLOAD_MAX_LENGTH_DEFAULT));
    }

    /**
     * Gets layout mode (full, body, compact) for the message printer.
     */
    public static String getPrintMessageLayout() {
        return CitrusSettings.getPropertyEnvOrDefault(
                LOG_PRINT_MESSAGE_LAYOUT_PROPERTY,
                LOG_PRINT_MESSAGE_LAYOUT_ENV,
                LOG_PRINT_MESSAGE_LAYOUT_DEFAULT);
    }

    /**
     * Gets the ANSI color mode (auto, always, never).
     */
    public static String getColorMode() {
        return CitrusSettings.getPropertyEnvOrDefault(
                LOG_COLOR_PROPERTY,
                LOG_COLOR_ENV,
                LOG_COLOR_DEFAULT);
    }
}
