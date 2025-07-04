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
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CamelRunInfraActionTest extends AbstractTestNGUnitTest {

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
        "version": "1.0"
      },
      {
        "service": "%s",
        "implementation": "%s",
        "description": "Sample service",
        "alias": [ "my-service" ],
        "aliasImplementation": [],
        "groupId": "org.citrusframework",
        "artifactId": "camel-infra-service",
        "version": "1.0"
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

    @BeforeClass
    public void setupMocks() {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeMethod
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

        Assert.assertEquals(context.getVariable("CITRUS_CAMEL_INFRA_MY_SERVICE_SERVER_URL"), "tcp://my-host:18088");
        Assert.assertEquals(context.getVariable("CITRUS_CAMEL_INFRA_MY_SERVICE_HOST"), "my-host");
        Assert.assertEquals(context.getVariable("CITRUS_CAMEL_INFRA_MY_SERVICE_PORT", Integer.class), 18088);
        Assert.assertEquals(context.getVariable("CITRUS_CAMEL_INFRA_MY_SERVICE_PASSWORD"), "secret");
        Assert.assertFalse(context.getVariable("CITRUS_CAMEL_INFRA_MY_SERVICE_FAULT_TOLERANT", Boolean.class));
        Assert.assertEquals(context.getVariable("CITRUS_CAMEL_INFRA_MY_SERVICE_CONNECTION_PROPERTIES", Map.class), Map.of("foo", "bar", "baz.value", "quux"));
        Assert.assertEquals(context.getVariable("CITRUS_CAMEL_INFRA_MY_SERVICE_FOO"), "bar");
        Assert.assertEquals(context.getVariable("CITRUS_CAMEL_INFRA_MY_SERVICE_BAZ_VALUE"), "quux");

        MyServiceImpl myService = context.getVariable("citrus.camel.infra.my-service", MyServiceImpl.class);
        Assert.assertTrue(myService.initialized.get());
        Assert.assertFalse(myService.shutdown.get());

        InfraService meta = context.getVariable("citrus.camel.infra.my-service:meta", InfraService.class);
        Assert.assertEquals(meta.version(), "1.0");

        Assert.assertEquals(context.getFinalActions().size(), 1);
        Assert.assertEquals(context.getFinalActions().iterator().next().getClass(), CamelStopInfraAction.Builder.class);
    }

    @Test
    public void testRunInfraWithImplementation() {
        CamelRunInfraAction action = new CamelRunInfraAction.Builder()
                .service("service", "two")
                .catalog(catalog)
                .autoRemove(false)
                .build();

        action.execute(context);

        Assert.assertEquals(context.getVariable("CITRUS_CAMEL_INFRA_SERVICE_TWO_SERVER_URL"), "tcp://my-host:18088");
        Assert.assertEquals(context.getVariable("CITRUS_CAMEL_INFRA_SERVICE_TWO_HOST"), "my-host");
        Assert.assertEquals(context.getVariable("CITRUS_CAMEL_INFRA_SERVICE_TWO_PORT", Integer.class), 18088);
        Assert.assertEquals(context.getVariable("CITRUS_CAMEL_INFRA_SERVICE_TWO_PASSWORD"), "secret");
        Assert.assertFalse(context.getVariable("CITRUS_CAMEL_INFRA_SERVICE_TWO_FAULT_TOLERANT", Boolean.class));
        Assert.assertEquals(context.getVariable("CITRUS_CAMEL_INFRA_SERVICE_TWO_CONNECTION_PROPERTIES", Map.class), Map.of("foo", "bar", "baz.value", "quux"));


        MyServiceImpl myService = context.getVariable("citrus.camel.infra.service.two", MyServiceImpl.class);
        Assert.assertTrue(myService.initialized.get());
        Assert.assertFalse(myService.shutdown.get());

        InfraService meta = context.getVariable("citrus.camel.infra.service.two:meta", InfraService.class);
        Assert.assertEquals(meta.version(), "1.0");

        Assert.assertEquals(context.getFinalActions().size(), 0);
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

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "No Camel infra service found for 'unknown'")
    public void testRunInfraNotFound() {
        CamelRunInfraAction action = new CamelRunInfraAction.Builder()
                .service("unknown")
                .catalog(catalog)
                .build();

        action.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "No Camel infra service found for 'service.unknown'")
    public void testRunInfraImplementationNotFound() {
        CamelRunInfraAction action = new CamelRunInfraAction.Builder()
                .service("service", "unknown")
                .catalog(catalog)
                .build();

        action.execute(context);
    }
}
