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

package org.citrusframework.actions.dsl;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.CreateEndpointAction;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.CreateEndpointAction.Builder.createEndpoint;

public class CreateEndpointTestActionBuilderTest extends UnitTestSupport {

    @Test
    public void testCreateEndpointBuilder() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(createEndpoint().uri("direct:hello").endpointName("sayHello"));
        builder.$(createEndpoint().type("direct")
                .property("queueName", "hello")
                .property("timeout", "2000"));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), CreateEndpointAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), CreateEndpointAction.class);

        CreateEndpointAction action = (CreateEndpointAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "create-endpoint");
        Assert.assertEquals(action.getEndpointUri(), "direct:hello");

        action = (CreateEndpointAction)test.getActions().get(1);
        Assert.assertEquals(action.getName(), "create-endpoint");
        Assert.assertEquals(action.getEndpointUri(), "direct?queueName=hello&timeout=2000");
    }
}
