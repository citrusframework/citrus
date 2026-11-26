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

package org.citrusframework.camel.xml;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.citrusframework.base.DefaultTestCase;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.actions.CamelRunIntegrationAction;
import org.citrusframework.camel.actions.CamelStopIntegrationAction;
import org.citrusframework.camel.actions.CamelUnwatchIntegrationAction;
import org.citrusframework.camel.actions.CamelWatchIntegrationAction;
import org.citrusframework.util.TestUtils;
import org.citrusframework.xml.XmlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CliWatchTest extends AbstractXmlActionTest {

    @BeforeAll
    public static void beforeClass() {
        Assumptions.assumeTrue(TestUtils.isNetworkReachable(), "Test skipped because network is not reachable. We are probably running behind a proxy and JBang download is not possible.");
    }

    @Test
    public void shouldLoadCamelWatchActions() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/camel/xml/camel-cli-watch.citrus.it.xml");

        CamelContext citrusCamelContext = new DefaultCamelContext();
        citrusCamelContext.start();

        context.getReferenceResolver().bind(CamelSettings.getContextName(), citrusCamelContext);
        context.getReferenceResolver().bind("camelContext", citrusCamelContext);

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("CamelCliWatchTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(3L, result.getActionCount());

        int actionIndex = 0;

        CamelRunIntegrationAction runAction = (CamelRunIntegrationAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(CamelRunIntegrationAction.class, runAction.getClass());
        Assertions.assertEquals("camel:cli:run", runAction.getName());
        Assertions.assertEquals("hello-xml", runAction.getIntegrationName());

        CamelWatchIntegrationAction watchAction = (CamelWatchIntegrationAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(CamelWatchIntegrationAction.class, watchAction.getClass());
        Assertions.assertEquals("camel:cli:watch", watchAction.getName());
        Assertions.assertEquals("hello-xml", watchAction.getIntegrationName());

        CamelUnwatchIntegrationAction unwatchAction = (CamelUnwatchIntegrationAction) result.getTestAction(actionIndex);
        Assertions.assertEquals(CamelUnwatchIntegrationAction.class, unwatchAction.getClass());
        Assertions.assertEquals("camel:cli:unwatch", unwatchAction.getName());
        Assertions.assertEquals("hello-xml", unwatchAction.getIntegrationName());

        Assertions.assertTrue(result instanceof DefaultTestCase);
        Assertions.assertEquals(1, ((DefaultTestCase) result).getFinalActions().size());

        CamelStopIntegrationAction stopAction = (CamelStopIntegrationAction) ((DefaultTestCase) result).getFinalActions().get(0);
        Assertions.assertEquals("hello-xml", stopAction.getIntegrationName());
    }
}
