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

package org.citrusframework.camel.yaml.infra;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.apache.camel.CamelContext;
import org.apache.camel.catalog.CamelCatalog;
import org.apache.camel.impl.DefaultCamelContext;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.actions.infra.CamelRunInfraAction;
import org.citrusframework.camel.actions.infra.CamelStopInfraAction;
import org.citrusframework.camel.actions.infra.InfraService;
import org.citrusframework.camel.yaml.AbstractYamlActionTest;
import org.citrusframework.yaml.YamlTestLoader;
import org.mockito.Mock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CamelRunInfraTest extends AbstractYamlActionTest {

    private final String infraServices = """
    [
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

    @BeforeEach
    public void init() {
        when(catalog.loadResource("test-infra", "metadata.json")).thenReturn(new ByteArrayInputStream(infraServices.getBytes(StandardCharsets.UTF_8)))
                .thenReturn(new ByteArrayInputStream(infraServices.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void shouldLoadCamelActions() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/camel/yaml/infra/camel-run-infra.citrus.it.yaml");

        CamelContext citrusCamelContext = new DefaultCamelContext();
        citrusCamelContext.start();

        context.getReferenceResolver().bind(CamelSettings.getContextName(), citrusCamelContext);
        context.getReferenceResolver().bind("camelContext", citrusCamelContext);

        context.getReferenceResolver().bind("camelCatalog", catalog);

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("CamelRunInfraTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(2L, result.getActionCount());
        Assertions.assertEquals(CamelRunInfraAction.class, result.getTestAction(0).getClass());
        Assertions.assertEquals("camel:infra:run", result.getTestAction(0).getName());

        Assertions.assertEquals("my-host", context.getVariable("CITRUS_CAMEL_INFRA_MY_SERVICE_HOST"));
        Assertions.assertEquals(18088, context.getVariable("CITRUS_CAMEL_INFRA_MY_SERVICE_PORT", Integer.class));

        Assertions.assertEquals("my-host", context.getVariable("CITRUS_CAMEL_INFRA_SERVICE_TWO_HOST"));
        Assertions.assertEquals(18088, context.getVariable("CITRUS_CAMEL_INFRA_SERVICE_TWO_PORT", Integer.class));

        MyServiceImpl myService = context.getVariable("citrus.camel.infra.my-service", MyServiceImpl.class);
        Assertions.assertTrue(myService.initialized.get());
        Assertions.assertTrue(myService.shutdown.get());

        InfraService meta = context.getVariable("citrus.camel.infra.my-service:meta", InfraService.class);
        Assertions.assertEquals("1.0", meta.version());

        myService = context.getVariable("citrus.camel.infra.service.two", MyServiceImpl.class);
        Assertions.assertTrue(myService.initialized.get());
        Assertions.assertFalse(myService.shutdown.get());

        meta = context.getVariable("citrus.camel.infra.service.two:meta", InfraService.class);
        Assertions.assertEquals("1.0", meta.version());

        Assertions.assertEquals(1, context.getFinalActions().size());
        Assertions.assertEquals(CamelStopInfraAction.Builder.class, context.getFinalActions().iterator().next().getClass());

    }
}
