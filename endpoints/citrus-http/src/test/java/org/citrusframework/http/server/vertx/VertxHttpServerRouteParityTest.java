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

package org.citrusframework.http.server.vertx;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.citrusframework.http.config.annotation.HttpServerConfig;
import org.citrusframework.http.config.annotation.VertxHttpServerConfig;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import jakarta.xml.bind.annotation.XmlAttribute;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that all configuration routes of the Vert.x based Http server provide the same
 * set of options with the same defaults as the Jetty based Http server.
 */
public class VertxHttpServerRouteParityTest extends AbstractTestNGUnitTest {

    /** Location of the http configuration schema */
    private static final String XSD_LOCATION = "org/citrusframework/schema/citrus-http-config.xsd";

    /** Name of the Vert.x server element in the http configuration schema */
    private static final String XSD_ELEMENT = "vertx-server";

    /** The transport neutral options and the Vert.x specific options */
    private static final List<String> EXPECTED_OPTIONS = List.of(
            "port", "securePort", "secured", "contextPath", "timeout", "autoStart", "endpointAdapter",
            "debugLogging", "actor", "messageConverter", "handleCookies", "removeSemicolonPathContent",
            "defaultStatus", "binaryMediaTypes", "useDefaultFilters", "interceptors", "authentication",
            "securedPath", "vertx", "workerPoolSize");

    /** Different spellings of the same option */
    private static final Map<String, String> ALIASES = Map.of(
            "securedConnection", "secured",
            "defaultStatusCode", "defaultStatus");

    @Test
    public void testAnnotationRouteProvidesAllOptions() {
        assertThat(getAnnotationOptions(VertxHttpServerConfig.class))
                .containsExactlyInAnyOrderElementsOf(EXPECTED_OPTIONS);
    }

    @Test
    public void testXmlRouteProvidesAllOptions() {
        // The Spring XML route does not provide the semicolon path content option (Java and annotation only).
        List<String> expected = EXPECTED_OPTIONS.stream()
                .filter(option -> !option.equals("removeSemicolonPathContent"))
                .toList();

        assertThat(getXmlOptions()).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testBuilderRouteProvidesAllOptions() {
        // The Java builder route does not provide the secured path as a separate option (it is part of the
        // authentication option) and the actor is set with a TestActor object (not a bean name).
        List<String> expected = EXPECTED_OPTIONS.stream()
                .filter(option -> !option.equals("securedPath") && !option.equals("actor"))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        expected.add("name");

        assertThat(getBuilderOptions()).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testNeutralDefaultsAreEqualToTheJettyServer() {
        VertxHttpServer vertxServer = new VertxHttpServerBuilder().build();
        HttpServer httpServer = new HttpServerBuilder().build();

        assertThat(vertxServer.getPort()).isEqualTo(httpServer.getPort());
        assertThat(vertxServer.getSecurePort()).isEqualTo(httpServer.getSecurePort());
        assertThat(vertxServer.getContextPath()).isEqualTo(httpServer.getContextPath());
        assertThat(vertxServer.getDefaultTimeout()).isEqualTo(httpServer.getDefaultTimeout());
        assertThat(vertxServer.getDefaultStatusCode()).isEqualTo(httpServer.getDefaultStatusCode());
        assertThat(vertxServer.isHandleCookies()).isEqualTo(httpServer.isHandleCookies());
        assertThat(vertxServer.isRemoveSemicolonPathContent()).isEqualTo(httpServer.isRemoveSemicolonPathContent());
        assertThat(vertxServer.getBinaryMediaTypes()).isEqualTo(httpServer.getBinaryMediaTypes());
        assertThat(vertxServer.isUseDefaultFilters()).isEqualTo(httpServer.isUseDefaultFilters());
        assertThat(vertxServer.getSecuredPath()).isEqualTo(httpServer.getSecuredPath());
    }

    @Test
    public void testAnnotationDefaultsAreEqualToTheJettyServer() {
        Map<String, Object> vertxDefaults = getAnnotationDefaults(VertxHttpServerConfig.class);
        Map<String, Object> jettyDefaults = getAnnotationDefaults(HttpServerConfig.class);

        // The Vert.x specific options and the options that are Java and annotation only are not
        // part of the Jetty based server configuration.
        for (String option : List.of("port", "securePort", "contextPath", "timeout", "autoStart",
                "endpointAdapter", "debugLogging", "actor", "messageConverter", "handleCookies",
                "defaultStatus", "binaryMediaTypes", "useDefaultFilters", "interceptors",
                "authentication", "securedPath", "secured")) {
            assertThat(jettyDefaults).containsKey(option);
            assertThat(vertxDefaults.get(option)).as(option).isEqualTo(jettyDefaults.get(option));
        }
    }

    /**
     * Gets the options of the Java builder route from the XmlAttribute setters.
     * @return the option names.
     */
    private List<String> getBuilderOptions() {
        List<String> options = new ArrayList<>();

        for (Method method : VertxHttpServerBuilder.class.getMethods()) {
            XmlAttribute xmlAttribute = method.getAnnotation(XmlAttribute.class);
            if (xmlAttribute == null) {
                continue;
            }

            String name = xmlAttribute.name();
            if (name.isEmpty() || "##default".equals(name)) {
                name = method.getName();
                if (name.startsWith("set")) {
                    name = name.substring(3);
                }
                name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
            }

            options.add(normalize(name));
        }

        return options;
    }

    /**
     * Gets the options of the annotation route.
     * @param annotationType the annotation type.
     * @return the option names.
     */
    private List<String> getAnnotationOptions(Class<?> annotationType) {
        List<String> options = new ArrayList<>();

        for (Method method : annotationType.getDeclaredMethods()) {
            options.add(method.getName());
        }

        return options;
    }

    /**
     * Gets the options of the Spring XML route from the configuration schema.
     * @return the option names.
     */
    private List<String> getXmlOptions() {
        List<String> options = new ArrayList<>();

        try (InputStream schema = getClass().getClassLoader().getResourceAsStream(XSD_LOCATION)) {
            assertThat(schema).as(XSD_LOCATION).isNotNull();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            Document document = factory.newDocumentBuilder()
                    .parse(new InputSource(schema));

            NodeList elements = document.getElementsByTagNameNS("*", "element");
            Element serverElement = null;

            for (int i = 0; i < elements.getLength(); i++) {
                Element element = (Element) elements.item(i);
                Node name = element.getAttributes().getNamedItem("name");
                if (name != null && XSD_ELEMENT.equals(name.getNodeValue())) {
                    serverElement = element;
                    break;
                }
            }

            assertThat(serverElement).as(XSD_ELEMENT).isNotNull();

            NodeList attributes = serverElement.getElementsByTagNameNS("*", "attribute");
            for (int i = 0; i < attributes.getLength(); i++) {
                Node name = attributes.item(i).getAttributes().getNamedItem("name");
                if (name != null && !"id".equals(name.getNodeValue())) {
                    options.add(normalize(name.getNodeValue()));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to read the http configuration schema", e);
        }

        return options;
    }

    /**
     * Gets the default values of the annotation attributes.
     * @param annotationType the annotation type.
     * @return the default values by option name.
     */
    private Map<String, Object> getAnnotationDefaults(Class<?> annotationType) {
        Map<String, Object> defaults = new LinkedHashMap<>();

        for (Method method : annotationType.getDeclaredMethods()) {
            defaults.put(method.getName(), method.getDefaultValue());
        }

        return defaults;
    }

    /**
     * Normalizes the option name to camel case and applies the known aliases.
     * @param name the option name.
     * @return the normalized option name.
     */
    private String normalize(String name) {
        StringBuilder camelCase = new StringBuilder();
        boolean upperCase = false;

        for (char character : name.toCharArray()) {
            if (character == '-') {
                upperCase = true;
            } else if (upperCase) {
                camelCase.append(Character.toUpperCase(character));
                upperCase = false;
            } else {
                camelCase.append(character);
            }
        }

        String normalized = camelCase.toString();
        return ALIASES.getOrDefault(normalized, normalized);
    }
}
