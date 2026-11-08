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

package org.citrusframework.camel.yaml;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.camel.actions.StopCamelContextAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StopContextTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadCamelActions() throws Exception {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/camel/yaml/camel-stop-context.citrus.it.yaml");

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
        Assertions.assertEquals("CamelStopContextTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(3L, result.getActionCount());

        StopCamelContextAction action = (StopCamelContextAction) result.getTestAction(0);
        Assertions.assertEquals(StopCamelContextAction.class, action.getClass());
        Assertions.assertEquals("camel:stop-context", action.getName());
        Assertions.assertEquals("defaultContext", action.getContextName());
        Assertions.assertEquals(-1L, action.getTimeout());
        Assertions.assertFalse(action.isImmediate());

        action = (StopCamelContextAction) result.getTestAction(1);
        Assertions.assertEquals("timeoutContext", action.getContextName());
        Assertions.assertEquals(60L, action.getTimeout());
        Assertions.assertFalse(action.isImmediate());

        action = (StopCamelContextAction) result.getTestAction(2);
        Assertions.assertEquals("immediateContext", action.getContextName());
        Assertions.assertTrue(action.isImmediate());
    }
}
