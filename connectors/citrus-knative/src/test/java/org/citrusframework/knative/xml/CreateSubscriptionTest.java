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

import io.fabric8.knative.messaging.v1.Subscription;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.knative.actions.messaging.CreateSubscriptionAction;
import org.citrusframework.xml.XmlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreateSubscriptionTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadKnativeActions() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/knative/xml/create-subscription.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("CreateSubscriptionTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertEquals(CreateSubscriptionAction.class, result.getTestAction(0).getClass());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        String namespace = "test";
        Subscription subscription = knativeClient.subscriptions().inNamespace(namespace).withName("my-subscription").get();
        Assertions.assertNotNull(subscription);
        Assertions.assertEquals(1, subscription.getMetadata().getLabels().size());
        Assertions.assertEquals("citrus", subscription.getMetadata().getLabels().get("app"));
        Assertions.assertEquals("my-channel", subscription.getSpec().getChannel().getName());
        Assertions.assertEquals("InMemoryChannel", subscription.getSpec().getChannel().getKind());
        Assertions.assertEquals("my-service", subscription.getSpec().getSubscriber().getRef().getName());
    }
}
