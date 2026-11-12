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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.citrusframework.jbang.cli.CitrusJBangMain.Settings.CAMEL_VERSION_DEFAULT;

public class YamlCodeAnalyzerTest {

    @Test
    public void shouldAnalyzeCode() throws IOException {
        YamlCodeAnalyzer analyzer = new YamlCodeAnalyzer();
        CodeAnalyzer.ScanResult scanResult = analyzer.scan(Resources.create("sample.citrus.it.yaml"));

        Assertions.assertEquals("sample.citrus.it.yaml", scanResult.name());

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

        Assertions.assertEquals(6, scanResult.actions().length);
        String[] foundActions = Arrays.stream(scanResult.actions()).sorted().toArray(String[]::new);
        Assertions.assertEquals("camel:cli:run", foundActions[0]);
        Assertions.assertEquals("camel:infra:run", foundActions[1]);
        Assertions.assertEquals("createEndpoint", foundActions[2]);
        Assertions.assertEquals("print", foundActions[3]);
        Assertions.assertEquals("receive", foundActions[4]);
        Assertions.assertEquals("send", foundActions[5]);

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
    public void shouldAnalyzeCodeWithEndpointsAfterActions() throws IOException {
        YamlCodeAnalyzer analyzer = new YamlCodeAnalyzer();
        CodeAnalyzer.ScanResult scanResult = analyzer.scan(Resources.create("sample-endpoints-after-actions.citrus.it.yaml"));

        Assertions.assertEquals("sample-endpoints-after-actions.citrus.it.yaml", scanResult.name());

        Assertions.assertEquals(7, scanResult.modules().length);
        String[] foundModules = Arrays.stream(scanResult.modules()).sorted().toArray(String[]::new);
        Assertions.assertEquals("citrus-base", foundModules[0]);
        Assertions.assertEquals("citrus-camel", foundModules[1]);
        Assertions.assertEquals("citrus-http", foundModules[2]);
        Assertions.assertEquals("citrus-jbang-connector", foundModules[3]);
        Assertions.assertEquals("citrus-jms", foundModules[4]);
        Assertions.assertEquals("citrus-kafka", foundModules[5]);
        Assertions.assertEquals("citrus-testcontainers", foundModules[6]);

        Assertions.assertEquals(4, scanResult.endpoints().length);
        String[] foundEndpoints = Arrays.stream(scanResult.endpoints()).sorted().toArray(String[]::new);
        Assertions.assertEquals("camel", foundEndpoints[0]);
        Assertions.assertEquals("http", foundEndpoints[1]);
        Assertions.assertEquals("jms", foundEndpoints[2]);
        Assertions.assertEquals("kafka", foundEndpoints[3]);
    }

    static Object[][] multilineEndpointVariants() {
        return new Object[][] {
                { ">", "folded" },
                { ">-", "folded strip" },
                { ">+", "folded keep" },
                { "|", "literal" },
                { "|-", "literal strip" },
                { "|+", "literal keep" },
        };
    }

    @ParameterizedTest
    @MethodSource("multilineEndpointVariants")
    public void shouldExtractCamelEndpointFromMultilineScalar(String indicator, String description) {
        YamlCodeAnalyzer analyzer = new YamlCodeAnalyzer();

        String code = """
                actions:
                  - send:
                      endpoint: %s
                        camel:paho-mqtt5:myTopic?brokerUrl=tcp://localhost:12883
                      message:
                        body:
                          data: Hi
                """.formatted(indicator);

        Set<String> components = analyzer.extractCamelEndpointComponents(code);
        Assertions.assertTrue(components.contains("paho-mqtt5"), "Failed to extract Camel endpoint with %s indicator".formatted(description));
    }

    @Test
    public void shouldExtractCamelEndpointFromInlineValue() {
        YamlCodeAnalyzer analyzer = new YamlCodeAnalyzer();

        String code = """
                actions:
                  - send:
                      endpoint: "camel:direct:news"
                      message:
                        body:
                          data: Hi
                """;

        Set<String> components = analyzer.extractCamelEndpointComponents(code);
        Assertions.assertTrue(components.contains("direct"));
    }

    @Test
    public void shouldDetectEndpointFromCreateEndpointWithTypedKey() {
        YamlCodeAnalyzer analyzer = new YamlCodeAnalyzer();

        String code = """
                actions:
                  - createEndpoint:
                      kafka:
                        asynchronous:
                          name: myKafkaEndpoint
                          topic: my-topic
                """;

        Set<String> modules = new HashSet<>();
        Set<String> endpoints = analyzer.scanEndpoints(code, modules);
        Assertions.assertTrue(endpoints.contains("kafka"));
        Assertions.assertTrue(modules.contains("citrus-kafka"));
    }

    @Test
    public void shouldDetectEndpointFromCreateEndpointWithTypeAttribute() {
        YamlCodeAnalyzer analyzer = new YamlCodeAnalyzer();

        String code = """
                actions:
                  - createEndpoint:
                      type: "http"
                      properties:
                        requestUrl: "http://localhost:8080"
                """;

        Set<String> modules = new HashSet<>();
        Set<String> endpoints = analyzer.scanEndpoints(code, modules);
        Assertions.assertTrue(endpoints.contains("http"));
        Assertions.assertTrue(modules.contains("citrus-http"));
    }

    @Test
    public void shouldDetectEndpointFromCreateEndpointWithUri() {
        YamlCodeAnalyzer analyzer = new YamlCodeAnalyzer();

        String code = """
                actions:
                  - createEndpoint:
                      name: myJmsEndpoint
                      uri: "jms:my-queue"
                """;

        Set<String> modules = new HashSet<>();
        Set<String> endpoints = analyzer.scanEndpoints(code, modules);
        Assertions.assertTrue(endpoints.contains("jms"));
        Assertions.assertTrue(modules.contains("citrus-jms"));
    }

    @Test
    public void shouldDetectEndpointsAfterActions() {
        YamlCodeAnalyzer analyzer = new YamlCodeAnalyzer();

        String code = """
                name: my-test
                actions:
                  - send:
                      endpoint: http://localhost:8080/test
                      message:
                        body:
                          data: Howdy
                endpoints:
                  - jms:
                      asynchronous:
                        name: foo
                        destination: my-queue
                """;

        Set<String> modules = new HashSet<>();
        Set<String> endpoints = analyzer.scanEndpoints(code, modules);
        Assertions.assertTrue(endpoints.contains("jms"));
        Assertions.assertTrue(modules.contains("citrus-jms"));
    }

    @Test
    public void shouldDetectEndpointsAtEndOfFile() {
        YamlCodeAnalyzer analyzer = new YamlCodeAnalyzer();

        String code = """
                name: my-test
                endpoints:
                  - kafka:
                      asynchronous:
                        name: myKafka
                        topic: my-topic
                """;

        Set<String> modules = new HashSet<>();
        Set<String> endpoints = analyzer.scanEndpoints(code, modules);
        Assertions.assertTrue(endpoints.contains("kafka"));
        Assertions.assertTrue(modules.contains("citrus-kafka"));
    }

    @Test
    public void shouldDetectEndpointsBeforeActions() {
        YamlCodeAnalyzer analyzer = new YamlCodeAnalyzer();

        String code = """
                name: my-test
                endpoints:
                  - jms:
                      asynchronous:
                        name: foo
                        destination: my-queue
                actions:
                  - send:
                      endpoint: http://localhost:8080/test
                      message:
                        body:
                          data: Howdy
                """;

        Set<String> modules = new HashSet<>();
        Set<String> endpoints = analyzer.scanEndpoints(code, modules);
        Assertions.assertTrue(endpoints.contains("jms"));
        Assertions.assertTrue(modules.contains("citrus-jms"));
    }

    @Test
    public void shouldExtractCamelEndpointFromUnquotedInlineValue() {
        YamlCodeAnalyzer analyzer = new YamlCodeAnalyzer();

        String code = """
                actions:
                  - send:
                      endpoint: camel:seda:news
                      message:
                        body:
                          data: Hi
                """;

        Set<String> components = analyzer.extractCamelEndpointComponents(code);
        Assertions.assertTrue(components.contains("seda"));
    }

}
