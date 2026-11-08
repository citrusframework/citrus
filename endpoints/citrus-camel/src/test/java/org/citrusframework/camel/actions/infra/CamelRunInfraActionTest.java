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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.camel.catalog.CamelCatalog;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.log.LogModifier;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CamelRunInfraActionTest extends org.citrusframework.junit.jupiter.AbstractUnitTest {

    private final String infraServices = """
    [
      {
        "service": "org.citrusframework.camel.FooService",
        "implementation": "org.citrusframework.camel.FooServiceImpl",
        "description": "Sample service",
        "alias": [ "foo-service" ],
        "aliasImplementation": [ "bar" ],
        "groupId": "org.citrusframework",
        "artifactId": "camel-infra-foo",
        "version": "1.0",
        "serviceVersion": "1.0"
      },
      {
        "service": "%s",
        "implementation": "%s",
        "description": "Sample service",
        "alias": [ "my-service" ],
        "aliasImplementation": [],
        "groupId": "org.citrusframework",
        "artifactId": "camel-infra-service",
        "version": "1.0",
        "serviceVersion": "1.0"
      },
      {
        "service": "%s",
        "implementation": "%s",
        "description": "Sample service with implementation",
        "alias": [ "service" ],
        "aliasImplementation": [ "one", "two", "three" ],
        "groupId": "org.citrusframework",
        "artifactId": "camel-infra-service",
        "version": "1.0"
      }
    ]
    """.formatted(MyService.class.getName(), MyServiceImpl.class.getName(), MyService.class.getName(), MyServiceImpl.class.getName());

    @Mock
    private CamelCatalog catalog;

    @BeforeEach
    public void setupMocks() {
    }

    @BeforeEach
    public void init() {
        when(catalog.loadResource("test-infra", "metadata.json")).thenReturn(new ByteArrayInputStream(infraServices.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testRunInfra() {
        CamelRunInfraAction action = new CamelRunInfraAction.Builder()
                .service("my-service")
                .catalog(catalog)
                .build();

        action.execute(context);

        Assertions.assertEquals("tcp://my-host:18088", context.getVariable("CITRUS_CAMEL_INFRA_MY_SERVICE_SERVER_URL"));
        Assertions.assertEquals("my-host", context.getVariable("CITRUS_CAMEL_INFRA_MY_SERVICE_HOST"));
        Assertions.assertEquals(18088, context.getVariable("CITRUS_CAMEL_INFRA_MY_SERVICE_PORT", Integer.class));
        Assertions.assertEquals("secret", context.getVariable("CITRUS_CAMEL_INFRA_MY_SERVICE_PASSWORD"));
        Assertions.assertFalse(context.getVariable("CITRUS_CAMEL_INFRA_MY_SERVICE_FAULT_TOLERANT", Boolean.class));
        Assertions.assertEquals(Map.of("foo", "bar", "baz.value", "quux"), context.getVariable("CITRUS_CAMEL_INFRA_MY_SERVICE_CONNECTION_PROPERTIES", Map.class));
        Assertions.assertEquals("bar", context.getVariable("CITRUS_CAMEL_INFRA_MY_SERVICE_FOO"));
        Assertions.assertEquals("quux", context.getVariable("CITRUS_CAMEL_INFRA_MY_SERVICE_BAZ_VALUE"));

        MyServiceImpl myService = context.getVariable("citrus.camel.infra.my-service", MyServiceImpl.class);
        Assertions.assertTrue(myService.initialized.get());
        Assertions.assertFalse(myService.shutdown.get());

        InfraService meta = context.getVariable("citrus.camel.infra.my-service:meta", InfraService.class);
        Assertions.assertEquals("1.0", meta.version());

        Assertions.assertEquals(1, context.getFinalActions().size());
        Assertions.assertEquals(CamelStopInfraAction.Builder.class, context.getFinalActions().iterator().next().getClass());
    }

    @Test
    public void testRunInfraWithImplementation() {
        CamelRunInfraAction action = new CamelRunInfraAction.Builder()
                .service("service", "two")
                .catalog(catalog)
                .autoRemove(false)
                .build();

        action.execute(context);

        Assertions.assertEquals("tcp://my-host:18088", context.getVariable("CITRUS_CAMEL_INFRA_SERVICE_TWO_SERVER_URL"));
        Assertions.assertEquals("my-host", context.getVariable("CITRUS_CAMEL_INFRA_SERVICE_TWO_HOST"));
        Assertions.assertEquals(18088, context.getVariable("CITRUS_CAMEL_INFRA_SERVICE_TWO_PORT", Integer.class));
        Assertions.assertEquals("secret", context.getVariable("CITRUS_CAMEL_INFRA_SERVICE_TWO_PASSWORD"));
        Assertions.assertFalse(context.getVariable("CITRUS_CAMEL_INFRA_SERVICE_TWO_FAULT_TOLERANT", Boolean.class));
        Assertions.assertEquals(Map.of("foo", "bar", "baz.value", "quux"), context.getVariable("CITRUS_CAMEL_INFRA_SERVICE_TWO_CONNECTION_PROPERTIES", Map.class));


        MyServiceImpl myService = context.getVariable("citrus.camel.infra.service.two", MyServiceImpl.class);
        Assertions.assertTrue(myService.initialized.get());
        Assertions.assertFalse(myService.shutdown.get());

        InfraService meta = context.getVariable("citrus.camel.infra.service.two:meta", InfraService.class);
        Assertions.assertEquals("1.0", meta.version());

        Assertions.assertEquals(0, context.getFinalActions().size());
    }

    @Test
    public void testRunInfraMaskSensitiveProperty() {
        CamelRunInfraAction action = new CamelRunInfraAction.Builder()
                .service("my-service")
                .catalog(catalog)
                .build();

        LogModifier modifier = Mockito.mock(LogModifier.class);
        context.setLogModifier(modifier);

        action.execute(context);

        verify(modifier).mask("CITRUS_CAMEL_INFRA_MY_SERVICE_PASSWORD='secret'");
    }

    @Test
    public void testRunInfraNotFound() {

        CitrusRuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(CitrusRuntimeException.class, () -> {

            CamelRunInfraAction action = new CamelRunInfraAction.Builder()
                    .service("unknown")
                    .catalog(catalog)
                    .build();

            action.execute(context);
    
        });

        org.junit.jupiter.api.Assertions.assertTrue(thrown.getMessage().matches("Failed to resolve Camel infra service for 'unknown'"), "Unexpected exception message: " + thrown.getMessage());
    }

    @Test
    public void testRunInfraImplementationNotFound() {

        CitrusRuntimeException thrown = org.junit.jupiter.api.Assertions.assertThrows(CitrusRuntimeException.class, () -> {

            CamelRunInfraAction action = new CamelRunInfraAction.Builder()
                    .service("service", "unknown")
                    .catalog(catalog)
                    .build();

            action.execute(context);
    
        });

        org.junit.jupiter.api.Assertions.assertTrue(thrown.getMessage().matches("Failed to resolve Camel infra service for 'service.unknown'"), "Unexpected exception message: " + thrown.getMessage());
    }
}
