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

package org.citrusframework.yaml.actions;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.CreateEndpointAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.endpoint.direct.DirectSyncEndpoint;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.yaml.YamlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreateEndpointTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadCreateEndpoint() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/yaml/actions/create-endpoint.citrus.it.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("CreateEndpointTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(6L, result.getActionCount());
        Assertions.assertEquals(CreateEndpointAction.class, result.getTestAction(0).getClass());
        Assertions.assertEquals("direct:hello", ((CreateEndpointAction) result.getTestAction(0)).getEndpointUri());
        Assertions.assertEquals("direct?autoClose=true&autoRemove=true&queueName=hello&timeout=2000", ((CreateEndpointAction) result.getTestAction(1)).getEndpointUri());

        Assertions.assertNotNull(((CreateEndpointAction) result.getTestAction(2)).getEndpointBuilder());
        Endpoint endpoint = ((CreateEndpointAction) result.getTestAction(2)).getEndpointBuilder().build();
        Assertions.assertEquals(DirectEndpoint.class, endpoint.getClass());
        Assertions.assertEquals("sayGoodBye", endpoint.getName());
        Assertions.assertEquals("goodbye", ((DirectEndpoint) endpoint).getEndpointConfiguration().getQueueName());
        Assertions.assertEquals(4000L, ((DirectEndpoint) endpoint).getEndpointConfiguration().getTimeout());

        Assertions.assertNotNull(((CreateEndpointAction) result.getTestAction(3)).getEndpointBuilder());
        endpoint = ((CreateEndpointAction) result.getTestAction(3)).getEndpointBuilder().build();
        Assertions.assertEquals(DirectSyncEndpoint.class, endpoint.getClass());
        Assertions.assertEquals("howdy", endpoint.getName());
        Assertions.assertEquals("howdy", ((DirectSyncEndpoint) endpoint).getEndpointConfiguration().getQueueName());
        Assertions.assertEquals(3000L, ((DirectSyncEndpoint) endpoint).getEndpointConfiguration().getTimeout());

        Assertions.assertTrue(context.getReferenceResolver().isResolvable("howdy", MessageQueue.class));

        Assertions.assertNotNull(((CreateEndpointAction) result.getTestAction(4)).getEndpointBuilder());
        endpoint = ((CreateEndpointAction) result.getTestAction(4)).getEndpointBuilder().build();
        Assertions.assertEquals(DirectEndpoint.class, endpoint.getClass());
        Assertions.assertEquals("simplified", endpoint.getName());
        Assertions.assertEquals("simpleQueue", ((DirectEndpoint) endpoint).getEndpointConfiguration().getQueueName());
        Assertions.assertEquals(5000L, ((DirectEndpoint) endpoint).getEndpointConfiguration().getTimeout());

        Assertions.assertNotNull(((CreateEndpointAction) result.getTestAction(5)).getEndpointBuilder());
        endpoint = ((CreateEndpointAction) result.getTestAction(5)).getEndpointBuilder().build();
        Assertions.assertEquals(DirectSyncEndpoint.class, endpoint.getClass());
        Assertions.assertEquals("simplifiedSync", endpoint.getName());
        Assertions.assertEquals(2000, ((DirectSyncEndpoint) endpoint).getEndpointConfiguration().getPollingInterval());
    }
}
