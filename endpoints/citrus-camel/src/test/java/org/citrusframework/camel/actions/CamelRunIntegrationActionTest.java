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

package org.citrusframework.camel.actions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.spi.Resources;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class CamelRunIntegrationActionTest {

    private final List<Path> tempDirectories = new ArrayList<>();

    @AfterMethod
    public void cleanup() throws IOException {
        for (Path tempDirectory : tempDirectories) {
            if (Files.exists(tempDirectory)) {
                try (var paths = Files.walk(tempDirectory)) {
                    paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            // ignore - cleanup is best effort
                        }
                    });
                }
            }
        }
    }

    private Path createTempDirectory(String prefix) throws IOException {
        Path tempDirectory = Files.createTempDirectory(prefix);
        tempDirectories.add(tempDirectory);
        return tempDirectory;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldLetInlineSystemPropertiesOverrideFile() {
        CamelRunIntegrationAction action = new CamelRunIntegrationAction.Builder()
                .integration("test", "from('timer:tick').log('hello')")
                .withSystemProperties(Resources.create(
                        "classpath:org/citrusframework/camel/actions/system.properties"))
                .withSystemProperty("server.port", "9090")
                .withSystemProperty("greeting", "Hello inline")
                .build();

        Map<String, String> props = (Map<String, String>) getField(action, "systemProperties");

        assertEquals(props.get("server.port"), "9090");
        assertEquals(props.get("greeting"), "Hello inline");
        assertEquals(props.get("server.host"), "localhost");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldLetInlineSystemPropertiesOverrideFileRegardlessOfOrder() {
        CamelRunIntegrationAction action = new CamelRunIntegrationAction.Builder()
                .integration("test", "from('timer:tick').log('hello')")
                .withSystemProperty("server.port", "9090")
                .withSystemProperty("greeting", "Hello inline")
                .withSystemProperties(Resources.create(
                        "classpath:org/citrusframework/camel/actions/system.properties"))
                .build();

        Map<String, String> props = (Map<String, String>) getField(action, "systemProperties");

        assertEquals(props.get("server.port"), "9090");
        assertEquals(props.get("greeting"), "Hello inline");
        assertEquals(props.get("server.host"), "localhost");
    }

    @Test
    public void shouldAutoSetPropertiesByDefault() {
        CamelRunIntegrationAction action = new CamelRunIntegrationAction.Builder()
                .integration("test", "from('timer:tick').log('hello')")
                .build();

        assertEquals(getField(action, "autoSetProperties"), true);
    }

    @Test
    public void shouldDisableAutoSetProperties() {
        CamelRunIntegrationAction action = new CamelRunIntegrationAction.Builder()
                .integration("test", "from('timer:tick').log('hello')")
                .autoSetProperties(false)
                .build();

        assertEquals(getField(action, "autoSetProperties"), false);
    }

    @Test
    public void shouldNotOverwriteUserDefinedSystemProperties() {
        Map<String, String> systemProperties = new HashMap<>();
        systemProperties.put("camel.component.kafka.brokers", "user-defined:9092");

        CamelRunIntegrationAction.applyAutoSetComponentProperties(
                systemProperties,
                Map.of(),
                Map.of("camel.component.kafka.brokers", "auto-set:9092"));

        assertEquals(systemProperties.get("camel.component.kafka.brokers"), "user-defined:9092");
    }

    @Test
    public void shouldNotOverwriteUserDefinedApplicationProperties() {
        Map<String, String> systemProperties = new HashMap<>();

        CamelRunIntegrationAction.applyAutoSetComponentProperties(
                systemProperties,
                Map.of("camel.component.kafka.brokers", "application.properties:9092"),
                Map.of("camel.component.kafka.brokers", "auto-set:9092"));

        assertFalse(systemProperties.containsKey("camel.component.kafka.brokers"));
    }

    @Test
    public void shouldAddAutoSetProperties() {
        Map<String, String> systemProperties = new HashMap<>();

        CamelRunIntegrationAction.applyAutoSetComponentProperties(
                systemProperties,
                Map.of(),
                Map.of("camel.component.kafka.brokers", "auto-set:9092"));

        assertEquals(systemProperties.get("camel.component.kafka.brokers"), "auto-set:9092");
    }

    @Test
    public void shouldReadApplicationPropertiesOfIntegration() throws IOException {
        Path workingDir = createTempDirectory("citrus-camel-run");
        Files.writeString(workingDir.resolve("application.properties"),
                "camel.component.kafka.brokers=application.properties:9092\ncamel.main.name=test\n");

        Map<String, String> userDefinedProperties = CamelRunIntegrationAction.loadApplicationProperties(workingDir);

        assertEquals(userDefinedProperties.get("camel.component.kafka.brokers"), "application.properties:9092");
        assertEquals(userDefinedProperties.get("camel.main.name"), "test");
    }

    @Test
    public void shouldReadNoApplicationPropertiesIfMissing() throws IOException {
        Path workingDir = createTempDirectory("citrus-camel-run");

        assertTrue(CamelRunIntegrationAction.loadApplicationProperties(workingDir).isEmpty());
    }

    @Test
    public void shouldAutoSetComponentPropertiesFromInfraServiceVariables() throws IOException {
        CamelRunIntegrationAction action = new CamelRunIntegrationAction.Builder()
                .integration("test", "from(\"kafka:topic\").to(\"log:info\")")
                .build();

        TestContext context = TestContextFactory.newInstance().getObject();
        context.setVariable("CITRUS_CAMEL_INFRA_KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");

        Map<String, String> systemProperties = new HashMap<>();
        action.autoSetComponentProperties(context, systemProperties, createTempDirectory("citrus-camel-run"));

        assertEquals(systemProperties.get("camel.component.kafka.brokers"), "localhost:9092");
    }
}
