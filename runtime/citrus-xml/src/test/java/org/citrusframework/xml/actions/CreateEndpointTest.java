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

package org.citrusframework.xml.actions;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.CreateEndpointAction;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.endpoint.direct.DirectEndpointsBuilder;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.xml.XmlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreateEndpointTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadCreateEndpoint() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/xml/actions/create-endpoint.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("CreateEndpointTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(4L, result.getActionCount());
        Assertions.assertEquals(CreateEndpointAction.class, result.getTestAction(0).getClass());
        Assertions.assertEquals("direct:hello", ((CreateEndpointAction) result.getTestAction(0)).getEndpointUri());
        Assertions.assertEquals("direct?autoClose=true&autoRemove=true&queueName=hello&timeout=2000", ((CreateEndpointAction) result.getTestAction(1)).getEndpointUri());
        Assertions.assertNotNull(((CreateEndpointAction) result.getTestAction(2)).getEndpointBuilder());
        Assertions.assertEquals(DirectEndpointsBuilder.class, ((CreateEndpointAction) result.getTestAction(2)).getEndpointBuilder().getClass());

        DirectEndpointsBuilder endpointBuilder = (DirectEndpointsBuilder) ((CreateEndpointAction) result.getTestAction(2)).getEndpointBuilder();
        endpointBuilder.setReferenceResolver(context.getReferenceResolver());

        DirectEndpoint directEndpoint = (DirectEndpoint) endpointBuilder.build();
        Assertions.assertEquals("foo", directEndpoint.getName());
        Assertions.assertEquals("fooQueue", directEndpoint.getEndpointConfiguration().getQueueName());
        Assertions.assertTrue(context.getReferenceResolver().isResolvable("fooQueue", MessageQueue.class),
                "Expected auto-create-queue to create the message queue in the reference resolver");

        Assertions.assertNotNull(((CreateEndpointAction) result.getTestAction(3)).getEndpointBuilder());
        Assertions.assertEquals(DirectEndpointsBuilder.class, ((CreateEndpointAction) result.getTestAction(3)).getEndpointBuilder().getClass());

        DirectEndpointsBuilder simplifiedBuilder = (DirectEndpointsBuilder) ((CreateEndpointAction) result.getTestAction(3)).getEndpointBuilder();
        simplifiedBuilder.setReferenceResolver(context.getReferenceResolver());

        DirectEndpoint simplifiedEndpoint = (DirectEndpoint) simplifiedBuilder.build();
        Assertions.assertEquals("simplified", simplifiedEndpoint.getName());
        Assertions.assertEquals("simpleQueue", simplifiedEndpoint.getEndpointConfiguration().getQueueName());
        Assertions.assertEquals(5000L, simplifiedEndpoint.getEndpointConfiguration().getTimeout());
    }
}
