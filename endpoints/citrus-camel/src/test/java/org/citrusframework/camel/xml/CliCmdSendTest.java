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
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.actions.CamelCmdSendAction;
import org.citrusframework.camel.actions.CamelRunIntegrationAction;
import org.citrusframework.util.TestUtils;
import org.citrusframework.xml.XmlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CliCmdSendTest extends AbstractXmlActionTest {

    @BeforeAll
    public static void beforeClass() {
        Assumptions.assumeTrue(TestUtils.isNetworkReachable(), "Test skipped because network is not reachable. We are probably running behind a proxy and JBang download is not possible.");
    }

    @Test
    public void shouldLoadCamelActions() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/camel/xml/camel-cli-cmd-send.citrus.it.xml");

        CamelContext citrusCamelContext = new DefaultCamelContext();
        citrusCamelContext.start();

        context.getReferenceResolver().bind(CamelSettings.getContextName(), citrusCamelContext);
        context.getReferenceResolver().bind("camelContext", citrusCamelContext);

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("CamelCliCmdSendTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(7L, result.getActionCount());
        Assertions.assertEquals(CamelRunIntegrationAction.class, result.getTestAction(0).getClass());
        Assertions.assertEquals(CamelCmdSendAction.class, result.getTestAction(1).getClass());
        Assertions.assertEquals("camel:cli:cmd-send", result.getTestAction(1).getName());
    }

}
