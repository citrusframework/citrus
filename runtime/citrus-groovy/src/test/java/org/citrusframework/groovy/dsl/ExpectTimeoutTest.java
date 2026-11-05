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

package org.citrusframework.groovy.dsl;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.ReceiveTimeoutAction;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.groovy.GroovyTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 2.3
 */
public class ExpectTimeoutTest extends AbstractGroovyActionDslTest {

    @Test
    public void testReceiveTimeoutBuilder() {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/groovy/dsl/expect-timeout.citrus.test.groovy");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("ExpectTimeoutTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(4L, result.getActionCount());
        Assertions.assertEquals(ReceiveTimeoutAction.class, result.getTestAction(0).getClass());

        int actionIndex = 0;

        ReceiveTimeoutAction action = (ReceiveTimeoutAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(1000L, action.getTimeout());
        Assertions.assertNull(action.getEndpointUri());
        Assertions.assertEquals(context.getReferenceResolver().resolve("helloEndpoint", DirectEndpoint.class), action.getEndpoint());
        Assertions.assertNull(action.getMessageSelector());

        action = (ReceiveTimeoutAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals("direct:helloQueue", action.getEndpointUri());

        action = (ReceiveTimeoutAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(500L, action.getTimeout());
        Assertions.assertEquals("helloEndpoint", action.getEndpointUri());
        Assertions.assertEquals("operation='Test'", action.getMessageSelector());
        Assertions.assertEquals(0L, action.getMessageSelectorMap().size());

        action = (ReceiveTimeoutAction) result.getTestAction(actionIndex);
        Assertions.assertEquals(500L, action.getTimeout());
        Assertions.assertEquals("helloEndpoint", action.getEndpointUri());
        Assertions.assertNull(action.getMessageSelector());
        Assertions.assertEquals(1L, action.getMessageSelectorMap().size());
        Assertions.assertEquals("Test", action.getMessageSelectorMap().get("operation"));
    }
}
