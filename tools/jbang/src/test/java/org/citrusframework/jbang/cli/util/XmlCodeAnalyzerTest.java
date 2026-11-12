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

package org.citrusframework.jbang.cli.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.citrusframework.spi.Resources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.citrusframework.jbang.cli.CitrusJBangMain.Settings.CAMEL_VERSION_DEFAULT;

public class XmlCodeAnalyzerTest {

    @Test
    public void shouldAnalyzeCode() throws IOException {
        XmlCodeAnalyzer analyzer = new XmlCodeAnalyzer();
        CodeAnalyzer.ScanResult scanResult = analyzer.scan(Resources.create("sample.citrus.it.xml"));

        Assertions.assertEquals("sample.citrus.it.xml", scanResult.name());

        Assertions.assertEquals(7, scanResult.modules().length);
        String[] foundModules = Arrays.stream(scanResult.modules()).sorted().toArray(String[]::new);
        Assertions.assertEquals("citrus-base", foundModules[0]);
        Assertions.assertEquals("citrus-camel", foundModules[1]);
        Assertions.assertEquals("citrus-http", foundModules[2]);
        Assertions.assertEquals("citrus-jbang-connector", foundModules[3]);
        Assertions.assertEquals("citrus-jms", foundModules[4]);
        Assertions.assertEquals("citrus-kafka", foundModules[5]);
        Assertions.assertEquals("citrus-testcontainers", foundModules[6]);

        Assertions.assertEquals(4, scanResult.dependencies().length);
        String[] foundDeps = Arrays.stream(scanResult.dependencies()).sorted().toArray(String[]::new);
        Assertions.assertEquals("org.apache.camel:camel-aws2-s3:${camel.version}", foundDeps[0]);
        Assertions.assertEquals("org.apache.camel:camel-paho-mqtt5:" + CAMEL_VERSION_DEFAULT, foundDeps[1]);
        Assertions.assertEquals("org.apache.camel:camel-test-infra-aws-v2:" + CAMEL_VERSION_DEFAULT, foundDeps[2]);
        Assertions.assertEquals("org.apache.camel:camel-test-infra-kafka:" + CAMEL_VERSION_DEFAULT, foundDeps[3]);

        Assertions.assertEquals(5, scanResult.actions().length);
        String[] foundActions = Arrays.stream(scanResult.actions()).sorted().toArray(String[]::new);
        Assertions.assertEquals("camel:cli:run", foundActions[0]);
        Assertions.assertEquals("camel:infra:run", foundActions[1]);
        Assertions.assertEquals("print", foundActions[2]);
        Assertions.assertEquals("receive", foundActions[3]);
        Assertions.assertEquals("send", foundActions[4]);

        Assertions.assertEquals(1, scanResult.containers().length);
        Assertions.assertEquals("iterate", scanResult.containers()[0]);

        Assertions.assertEquals(5, scanResult.endpoints().length);
        String[] foundEndpoints = Arrays.stream(scanResult.endpoints()).sorted().toArray(String[]::new);
        Assertions.assertEquals("camel", foundEndpoints[0]);
        Assertions.assertEquals("direct", foundEndpoints[1]);
        Assertions.assertEquals("http", foundEndpoints[2]);
        Assertions.assertEquals("jms", foundEndpoints[3]);
        Assertions.assertEquals("kafka", foundEndpoints[4]);

        Assertions.assertEquals(1, scanResult.functions().length);
        Assertions.assertEquals("citrus:randomNumber", scanResult.functions()[0]);

        Assertions.assertEquals(2, scanResult.validationMatcher().length);
        String[] foundValidationMatcher = Arrays.stream(scanResult.validationMatcher()).sorted().toArray(String[]::new);
        Assertions.assertEquals("ignore", foundValidationMatcher[0]);
        Assertions.assertEquals("isNumber", foundValidationMatcher[1]);
    }

    @Test
    public void shouldDetectEndpointFromCreateEndpointWithTypeAttribute() {
        XmlCodeAnalyzer analyzer = new XmlCodeAnalyzer();

        String code = """
                <actions>
                  <create-endpoint type="kafka" auto-close="true">
                    <properties>
                      <property name="topic" value="my-topic"/>
                    </properties>
                  </create-endpoint>
                </actions>
                """;

        Set<String> modules = new HashSet<>();
        Set<String> endpoints = analyzer.scanEndpoints(code, modules);
        Assertions.assertTrue(endpoints.contains("kafka"));
        Assertions.assertTrue(modules.contains("citrus-kafka"));
    }

    @Test
    public void shouldDetectEndpointFromCreateEndpointWithChildElement() {
        XmlCodeAnalyzer analyzer = new XmlCodeAnalyzer();

        String code = """
                <actions>
                  <create-endpoint name="myHttpEndpoint">
                    <http>
                      <client requestUrl="http://localhost:8080"/>
                    </http>
                  </create-endpoint>
                </actions>
                """;

        Set<String> modules = new HashSet<>();
        Set<String> endpoints = analyzer.scanEndpoints(code, modules);
        Assertions.assertTrue(endpoints.contains("http"));
        Assertions.assertTrue(modules.contains("citrus-http"));
    }

    @Test
    public void shouldDetectEndpointFromCreateEndpointWithUri() {
        XmlCodeAnalyzer analyzer = new XmlCodeAnalyzer();

        String code = """
                <actions>
                  <create-endpoint name="myJmsEndpoint" uri="jms:my-queue"/>
                </actions>
                """;

        Set<String> modules = new HashSet<>();
        Set<String> endpoints = analyzer.scanEndpoints(code, modules);
        Assertions.assertTrue(endpoints.contains("jms"));
        Assertions.assertTrue(modules.contains("citrus-jms"));
    }

}
