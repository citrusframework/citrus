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
import org.citrusframework.camel.actions.StopCamelContextAction;
import org.citrusframework.xml.XmlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class StopContextTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadCamelActions() throws Exception {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/camel/xml/camel-stop-context.citrus.it.xml");

        CamelContext defaultContext = new DefaultCamelContext();
        defaultContext.start();
        context.getReferenceResolver().bind("defaultContext", defaultContext);

        CamelContext timeoutContext = new DefaultCamelContext();
        timeoutContext.start();
        context.getReferenceResolver().bind("timeoutContext", timeoutContext);

        CamelContext immediateContext = new DefaultCamelContext();
        immediateContext.start();
        context.getReferenceResolver().bind("immediateContext", immediateContext);

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "CamelStopContextTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 3L);

        StopCamelContextAction action = (StopCamelContextAction) result.getTestAction(0);
        Assert.assertEquals(action.getClass(), StopCamelContextAction.class);
        Assert.assertEquals(action.getName(), "camel:stop-context");
        Assert.assertEquals(action.getContextName(), "defaultContext");
        Assert.assertEquals(action.getTimeout(), -1L);
        Assert.assertFalse(action.isImmediate());

        action = (StopCamelContextAction) result.getTestAction(1);
        Assert.assertEquals(action.getContextName(), "timeoutContext");
        Assert.assertEquals(action.getTimeout(), 60L);
        Assert.assertFalse(action.isImmediate());

        action = (StopCamelContextAction) result.getTestAction(2);
        Assert.assertEquals(action.getContextName(), "immediateContext");
        Assert.assertTrue(action.isImmediate());
    }
}
