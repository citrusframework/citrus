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

import io.fabric8.knative.duck.v1.DestinationBuilder;
import io.fabric8.knative.duck.v1.KReferenceBuilder;
import io.fabric8.knative.messaging.v1.Subscription;
import io.fabric8.knative.messaging.v1.SubscriptionBuilder;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.knative.actions.DeleteKnativeResourceAction;
import org.citrusframework.xml.XmlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DeleteResourceTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadKnativeActions() {
        String namespace = "test";
        Subscription subscription = new SubscriptionBuilder()
                .withNewMetadata()
                .withName("my-subscription")
                .withNamespace(namespace)
                .endMetadata()
                .withNewSpec()
                .withChannel(new KReferenceBuilder()
                        .withApiVersion("messaging/v1")
                        .withKind("InMemoryChannel")
                        .withName("my-channel")
                        .build())
                .withSubscriber(new DestinationBuilder()
                        .withNewRef()
                        .withApiVersion("v1")
                        .withKind("Service")
                        .withName("my-service")
                        .endRef()
                        .build())
                .endSpec()
                .build();

        knativeClient.subscriptions()
                .inNamespace(namespace)
                .resource(subscription)
                .create();

        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/knative/xml/delete-resource-test.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "DeleteResourceTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 1L);
        Assert.assertEquals(result.getTestAction(0).getClass(), DeleteKnativeResourceAction.class);
        Assert.assertTrue(result.getTestResult().isSuccess());

        subscription = knativeClient.subscriptions().inNamespace(namespace).withName("my-subscription").get();
        Assert.assertNull(subscription);
    }
}
