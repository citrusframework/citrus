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

package org.citrusframework.knative.xml;

import io.fabric8.knative.eventing.v1.Broker;
import io.fabric8.knative.eventing.v1.BrokerBuilder;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.knative.actions.eventing.DeleteBrokerAction;
import org.citrusframework.xml.XmlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DeleteBrokerTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadKnativeActions() {
        String namespace = "test";
        Broker broker = new BrokerBuilder()
                .withNewMetadata()
                .withName("my-broker")
                .withNamespace(namespace)
                .endMetadata()
                .build();

        knativeClient.brokers()
                .inNamespace(namespace)
                .resource(broker)
                .create();

        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/knative/xml/delete-broker-test.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "DeleteBrokerTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 1L);
        Assert.assertEquals(result.getTestAction(0).getClass(), DeleteBrokerAction.class);
        Assert.assertTrue(result.getTestResult().isSuccess());

        broker = knativeClient.brokers().inNamespace(namespace).withName("my-broker").get();
        Assert.assertNull(broker);
    }
}
