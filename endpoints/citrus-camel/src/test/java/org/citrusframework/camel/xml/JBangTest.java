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
import org.citrusframework.DefaultTestCase;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.actions.CamelRunIntegrationAction;
import org.citrusframework.camel.actions.CamelStopIntegrationAction;
import org.citrusframework.camel.actions.CamelVerifyIntegrationAction;
import org.citrusframework.util.TestUtils;
import org.citrusframework.xml.XmlTestLoader;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class JBangTest extends AbstractXmlActionTest {

    @BeforeClass
    public static void beforeClass() {
        if (!TestUtils.isNetworkReachable()) {
            throw new SkipException("Test skipped because network is not reachable. We are probably running behind a proxy and JBang download is not possible.");
        }
    }

    @Test
    public void shouldLoadCamelActions() throws Exception {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/camel/xml/camel-jbang-test.xml");

        CamelContext citrusCamelContext = new DefaultCamelContext();
        citrusCamelContext.start();

        context.getReferenceResolver().bind(CamelSettings.getContextName(), citrusCamelContext);
        context.getReferenceResolver().bind("camelContext", citrusCamelContext);

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "CamelJBangTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);
        Assert.assertEquals(result.getTestAction(0).getClass(), CamelRunIntegrationAction.class);
        Assert.assertEquals(result.getTestAction(0).getName(), "run-integration");

        int actionIndex = 0;

        CamelRunIntegrationAction action = (CamelRunIntegrationAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getIntegrationName(), "hello-xml");

        CamelVerifyIntegrationAction verifyAction = (CamelVerifyIntegrationAction) result.getTestAction(actionIndex);
        Assert.assertEquals(verifyAction.getIntegrationName(), "hello-xml");

        Assert.assertTrue(result instanceof DefaultTestCase);
        Assert.assertEquals(((DefaultTestCase) result).getFinalActions().size(), 1);

        CamelStopIntegrationAction stopAction = (CamelStopIntegrationAction) ((DefaultTestCase) result).getFinalActions().get(0);
        Assert.assertEquals(stopAction.getIntegrationName(), "hello-xml");
    }
}
