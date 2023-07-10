/*
 * Copyright 2006-2015 the original author or authors.
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
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ExpectTimeoutTest extends AbstractGroovyActionDslTest {

    @Test
    public void testReceiveTimeoutBuilder() {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/groovy/dsl/expect-timeout.test.groovy");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "ExpectTimeoutTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 4L);
        Assert.assertEquals(result.getTestAction(0).getClass(), ReceiveTimeoutAction.class);

        int actionIndex = 0;

        ReceiveTimeoutAction action = (ReceiveTimeoutAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getTimeout(), 1000L);
        Assert.assertNull(action.getEndpointUri());
        Assert.assertEquals(action.getEndpoint(), context.getReferenceResolver().resolve("helloEndpoint", DirectEndpoint.class));
        Assert.assertNull(action.getMessageSelector());

        action = (ReceiveTimeoutAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getEndpointUri(), "direct:helloQueue");

        action = (ReceiveTimeoutAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getTimeout(), 500L);
        Assert.assertEquals(action.getEndpointUri(), "helloEndpoint");
        Assert.assertEquals(action.getMessageSelector(), "operation='Test'");
        Assert.assertEquals(action.getMessageSelectorMap().size(), 0L);

        action = (ReceiveTimeoutAction) result.getTestAction(actionIndex);
        Assert.assertEquals(action.getTimeout(), 500L);
        Assert.assertEquals(action.getEndpointUri(), "helloEndpoint");
        Assert.assertNull(action.getMessageSelector());
        Assert.assertEquals(action.getMessageSelectorMap().size(), 1L);
        Assert.assertEquals(action.getMessageSelectorMap().get("operation"), "Test");
    }
}
