/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.xml;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.common.InitializingPhase;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMImplementationList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSResourceResolver;
import org.w3c.dom.ls.LSSerializer;

/**
 * Class is loaded with Spring application context in Citrus. When loaded automatically initializes XML utilities
 * with this XML processing configuration. Configuration is pushed to XML utility classes after properties are set.
 *
 * @author Christoph Deppisch
 * @since 2.6.2
 */
public class XmlConfigurer implements InitializingPhase {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(XmlConfigurer.class);

    /** DOM implementation */
    private final DOMImplementationRegistry registry;
    private final DOMImplementationLS domImpl;

    /** Default parser and serializer settings */
    private Map<String, Object> parseSettings = new HashMap<>();
    private Map<String, Object> serializeSettings = new HashMap<>();

    public static final String SPLIT_CDATA_SECTIONS = "split-cdata-sections";
    public static final String FORMAT_PRETTY_PRINT = "format-pretty-print";
    public static final String ELEMENT_CONTENT_WHITESPACE = "element-content-whitespace";
    public static final String CDATA_SECTIONS = "cdata-sections";
    public static final String VALIDATE_IF_SCHEMA = "validate-if-schema";
    public static final String RESOURCE_RESOLVER = "resource-resolver";
    public static final String XML_DECLARATION = "xml-declaration";

    public XmlConfigurer() {
        try {
            registry = DOMImplementationRegistry.newInstance();

            if (logger.isDebugEnabled()) {
                DOMImplementationList domImplList = registry.getDOMImplementationList("LS");
                for (int i = 0; i < domImplList.getLength(); i++) {
                    logger.debug("Found DOMImplementationLS: " + domImplList.item(i));
                }
            }

            domImpl = (DOMImplementationLS) registry.getDOMImplementation("LS");

            if (logger.isDebugEnabled()) {
                logger.debug("Using DOMImplementationLS: " + domImpl.getClass().getName());
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException(e);
        }

        setDefaultParseSettings();
        setDefaultSerializeSettings();
    }

    /**
     * Creates basic LSParser instance and sets common
     * properties and configuration parameters.
     * @return
     */
    public LSParser createLSParser() {
        LSParser parser = domImpl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
        configureParser(parser);

        return parser;
    }

    /**
     * Set parser configuration based on this configurers settings.
     * @param parser
     */
    protected void configureParser(LSParser parser) {
        for (Map.Entry<String, Object> setting : parseSettings.entrySet()) {
            setParserConfigParameter(parser, setting.getKey(), setting.getValue());
        }
    }

    /**
     * Creates basic LSSerializer instance and sets common
     * properties and configuration parameters.
     * @return
     */
    public LSSerializer createLSSerializer() {
        LSSerializer serializer = domImpl.createLSSerializer();
        configureSerializer(serializer);

        return serializer;
    }

    /**
     * Set serializer configuration based on this configurers settings.
     * @param serializer
     */
    protected void configureSerializer(LSSerializer serializer) {
        for (Map.Entry<String, Object> setting : serializeSettings.entrySet()) {
            setSerializerConfigParameter(serializer, setting.getKey(), setting.getValue());
        }
    }

    /**
     * Creates LSInput from dom implementation.
     * @return
     */
    public LSInput createLSInput() {
        return domImpl.createLSInput();
    }

    /**
     * Creates LSOutput from dom implementation.
     * @return
     */
    public LSOutput createLSOutput() {
        return domImpl.createLSOutput();
    }


    /**
     * Creates LSResourceResolver from dom implementation.
     * @return
     */
    public LSResourceResolver createLSResourceResolver() {
        return new LSResolverImpl(domImpl);
    }

    /**
     * Sets the default parse settings.
     */
    private void setDefaultParseSettings() {
        if (!parseSettings.containsKey(CDATA_SECTIONS)) {
            parseSettings.put(CDATA_SECTIONS, true);
        }

        if (!parseSettings.containsKey(SPLIT_CDATA_SECTIONS)) {
            parseSettings.put(SPLIT_CDATA_SECTIONS, false);
        }

        if (!parseSettings.containsKey(VALIDATE_IF_SCHEMA)) {
            parseSettings.put(VALIDATE_IF_SCHEMA, true);
        }

        if (!parseSettings.containsKey(RESOURCE_RESOLVER)) {
            parseSettings.put(RESOURCE_RESOLVER, createLSResourceResolver());
        }

        if (!parseSettings.containsKey(ELEMENT_CONTENT_WHITESPACE)) {
            parseSettings.put(ELEMENT_CONTENT_WHITESPACE, false);
        }
    }

    /**
     * Sets the default serialize settings.
     */
    private void setDefaultSerializeSettings() {
        if (!serializeSettings.containsKey(ELEMENT_CONTENT_WHITESPACE)) {
            serializeSettings.put(ELEMENT_CONTENT_WHITESPACE, true);
        }

        if (!serializeSettings.containsKey(SPLIT_CDATA_SECTIONS)) {
            serializeSettings.put(SPLIT_CDATA_SECTIONS, false);
        }

        if (!serializeSettings.containsKey(FORMAT_PRETTY_PRINT)) {
            serializeSettings.put(FORMAT_PRETTY_PRINT, true);
        }

        if (!serializeSettings.containsKey(XML_DECLARATION)) {
            serializeSettings.put(XML_DECLARATION, true);
        }
    }

    /**
     * Sets a config parameter on LSParser instance if settable. Otherwise logging unset parameter.
     * @param serializer
     * @param parameterName
     * @param value
     */
    public void setSerializerConfigParameter(LSSerializer serializer, String parameterName, Object value) {
        if (serializer.getDomConfig().canSetParameter(parameterName, value)) {
            serializer.getDomConfig().setParameter(parameterName, value);
        } else {
            logParameterNotSet(parameterName, "LSSerializer");
        }
    }

    /**
     * Sets a config parameter on LSParser instance if settable. Otherwise, logging unset parameter.
     * @param parser
     * @param parameterName
     * @param value
     */
    public void setParserConfigParameter(LSParser parser, String parameterName, Object value) {
        if (parser.getDomConfig().canSetParameter(parameterName, value)) {
            parser.getDomConfig().setParameter(parameterName, value);
        } else {
            logParameterNotSet(parameterName, "LSParser");
        }
    }

    /**
     * Logging that parameter was not set on component.
     * @param parameterName
     */
    private void logParameterNotSet(String parameterName, String componentName) {
        logger.warn("Unable to set '" + parameterName + "' parameter on " + componentName);
    }

    /**
     * Sets the parseSettings property.
     *
     * @param parseSettings
     */
    public void setParseSettings(Map<String, Object> parseSettings) {
        this.parseSettings = parseSettings;
    }

    /**
     * Sets the serializeSettings property.
     *
     * @param serializeSettings
     */
    public void setSerializeSettings(Map<String, Object> serializeSettings) {
        this.serializeSettings = serializeSettings;
    }

    @Override
    public void initialize() {
        setDefaultParseSettings();
        setDefaultSerializeSettings();

        XMLUtils.initialize(this);
    }
}
