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

package org.citrusframework.camel.actions.infra;

import java.util.Map;

import org.apache.camel.catalog.DefaultCamelCatalog;
import org.citrusframework.camel.UnitTestSupport;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class CamelComponentPropertyResolverTest extends UnitTestSupport {

    private static final String KAFKA_ROUTE = """
            from("kafka:topic")
                .to("log:info");
            """;

    @Test
    public void shouldResolveKafkaBootstrapServers() {
        context.setVariable("CITRUS_CAMEL_INFRA_KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");

        Map<String, String> properties = CamelComponentPropertyResolver.resolveProperties(
                KAFKA_ROUTE, new DefaultCamelCatalog(), context);

        assertEquals(properties.size(), 1);
        assertEquals(properties.get("camel.component.kafka.brokers"), "localhost:9092");
    }

    @Test
    public void shouldResolvePulsarServiceUrl() {
        context.setVariable("CITRUS_CAMEL_INFRA_PULSAR_PULSAR_BROKER_URL", "pulsar://localhost:6650");

        Map<String, String> properties = CamelComponentPropertyResolver.resolveProperties(
                "from(\"pulsar:topic\")", new DefaultCamelCatalog(), context);

        assertEquals(properties.get("camel.component.pulsar.serviceUrl"), "pulsar://localhost:6650");
    }

    @Test
    public void shouldResolveNatsServers() {
        context.setVariable("CITRUS_CAMEL_INFRA_NATS_SERVICE_ADDRESS", "localhost:4222");

        Map<String, String> properties = CamelComponentPropertyResolver.resolveProperties(
                "from(\"nats:topic\")", new DefaultCamelCatalog(), context);

        assertEquals(properties.get("camel.component.nats.servers"), "localhost:4222");
    }

    @Test
    public void shouldResolveElasticsearchHost() {
        context.setVariable("CITRUS_CAMEL_INFRA_ELASTICSEARCH_ELASTIC_SEARCH_HOST", "localhost");

        Map<String, String> properties = CamelComponentPropertyResolver.resolveProperties(
                "from(\"elasticsearch:demo?operation=search\")", new DefaultCamelCatalog(), context);

        assertEquals(properties.get("camel.component.elasticsearch.hostAddresses"), "localhost");
    }

    @Test
    public void shouldIgnoreUnknownComponents() {
        context.setVariable("CITRUS_CAMEL_INFRA_KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");

        Map<String, String> properties = CamelComponentPropertyResolver.resolveProperties(
                "from(\"unknown-component:topic\")", new DefaultCamelCatalog(), context);

        assertTrue(properties.isEmpty());
    }

    @Test
    public void shouldIgnoreUnknownInfraProperties() {
        context.setVariable("CITRUS_CAMEL_INFRA_KAFKA_UNKNOWN_PROPERTY", "value");

        Map<String, String> properties = CamelComponentPropertyResolver.resolveProperties(
                KAFKA_ROUTE, new DefaultCamelCatalog(), context);

        assertTrue(properties.isEmpty());
    }

    @Test
    public void shouldIgnoreEmptyVariableValues() {
        context.setVariable("CITRUS_CAMEL_INFRA_KAFKA_BOOTSTRAP_SERVERS", "");

        Map<String, String> properties = CamelComponentPropertyResolver.resolveProperties(
                KAFKA_ROUTE, new DefaultCamelCatalog(), context);

        assertTrue(properties.isEmpty());
    }

    @Test
    public void shouldResolveEmptyPropertiesForMissingSource() {
        assertTrue(CamelComponentPropertyResolver.resolveProperties(null, new DefaultCamelCatalog(), context).isEmpty());
        assertTrue(CamelComponentPropertyResolver.resolveProperties("", new DefaultCamelCatalog(), context).isEmpty());
        assertTrue(CamelComponentPropertyResolver.resolveProperties("   ", new DefaultCamelCatalog(), context).isEmpty());
    }

    @Test
    public void shouldIgnoreObjectComponentOptions() {
        context.setVariable("CITRUS_CAMEL_INFRA_KAFKA_CONFIGURATION", "some-string");

        Map<String, String> properties = CamelComponentPropertyResolver.resolveProperties(
                KAFKA_ROUTE, new DefaultCamelCatalog(), context);

        assertTrue(properties.isEmpty());
    }

    @Test
    public void shouldIgnoreObjectComponentOptionsOnElasticsearchClient() {
        context.setVariable("CITRUS_CAMEL_INFRA_ELASTICSEARCH_CLIENT", "some-string");

        Map<String, String> properties = CamelComponentPropertyResolver.resolveProperties(
                "from(\"elasticsearch:demo?operation=search\")", new DefaultCamelCatalog(), context);

        assertTrue(properties.isEmpty());
    }

    @Test
    public void shouldResolveImplementationQualifiedInfraProperties() {
        context.setVariable("CITRUS_CAMEL_INFRA_KAFKA_STRIMZI_BOOTSTRAP_SERVERS", "localhost:9092");

        Map<String, String> properties = CamelComponentPropertyResolver.resolveProperties(
                KAFKA_ROUTE, new DefaultCamelCatalog(), context);

        assertEquals(properties.get("camel.component.kafka.brokers"), "localhost:9092");
    }

    @Test
    public void shouldIgnoreAbsoluteUrls() {
        context.setVariable("CITRUS_CAMEL_INFRA_HTTP_PROXY_HOST", "localhost");

        Map<String, String> properties = CamelComponentPropertyResolver.resolveProperties(
                "from(\"http://example.com/api\")", new DefaultCamelCatalog(), context);

        assertTrue(properties.isEmpty());
    }
}
