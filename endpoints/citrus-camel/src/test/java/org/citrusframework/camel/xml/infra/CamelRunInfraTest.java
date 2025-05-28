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

package org.citrusframework.camel.xml.infra;

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
import org.citrusframework.camel.xml.AbstractXmlActionTest;
import org.citrusframework.xml.XmlTestLoader;
import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

public class CamelRunInfraTest extends AbstractXmlActionTest {

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

    @BeforeMethod
    public void init() {
        when(catalog.loadResource("test-infra", "metadata.json")).thenReturn(new ByteArrayInputStream(infraServices.getBytes(StandardCharsets.UTF_8)))
                .thenReturn(new ByteArrayInputStream(infraServices.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void shouldLoadCamelActions() throws Exception {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/camel/xml/infra/camel-run-infra-test.xml");

        CamelContext citrusCamelContext = new DefaultCamelContext();
        citrusCamelContext.start();

        context.getReferenceResolver().bind(CamelSettings.getContextName(), citrusCamelContext);
        context.getReferenceResolver().bind("camelContext", citrusCamelContext);

        context.getReferenceResolver().bind("camelCatalog", catalog);

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "CamelRunInfraTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);
        Assert.assertEquals(result.getTestAction(0).getClass(), CamelRunInfraAction.class);
        Assert.assertEquals(result.getTestAction(0).getName(), "camel-run-infra");

        Assert.assertEquals(context.getVariable("CITRUS_CAMEL_INFRA_MY_SERVICE_HOST"), "my-host");
        Assert.assertEquals(context.getVariable("CITRUS_CAMEL_INFRA_MY_SERVICE_PORT", Integer.class), 18088);

        Assert.assertEquals(context.getVariable("CITRUS_CAMEL_INFRA_SERVICE_TWO_HOST"), "my-host");
        Assert.assertEquals(context.getVariable("CITRUS_CAMEL_INFRA_SERVICE_TWO_PORT", Integer.class), 18088);

        MyServiceImpl myService = context.getVariable("citrus.camel.infra.my-service", MyServiceImpl.class);
        Assert.assertTrue(myService.initialized.get());
        Assert.assertTrue(myService.shutdown.get());

        InfraService meta = context.getVariable("citrus.camel.infra.my-service:meta", InfraService.class);
        Assert.assertEquals(meta.version(), "1.0");

        myService = context.getVariable("citrus.camel.infra.service.two", MyServiceImpl.class);
        Assert.assertTrue(myService.initialized.get());
        Assert.assertFalse(myService.shutdown.get());

        meta = context.getVariable("citrus.camel.infra.service.two:meta", InfraService.class);
        Assert.assertEquals(meta.version(), "1.0");

        Assert.assertEquals(context.getFinalActions().size(), 1);
        Assert.assertEquals(context.getFinalActions().iterator().next().getClass(), CamelStopInfraAction.Builder.class);

    }
}
