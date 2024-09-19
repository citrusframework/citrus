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

package org.citrusframework.knative.yaml;

import io.fabric8.knative.messaging.v1.Subscription;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.knative.actions.messaging.CreateSubscriptionAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CreateSubscriptionTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadKnativeActions() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/knative/yaml/create-subscription-test.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "CreateSubscriptionTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 1L);
        Assert.assertEquals(result.getTestAction(0).getClass(), CreateSubscriptionAction.class);
        Assert.assertTrue(result.getTestResult().isSuccess());

        String namespace = "test";
        Subscription subscription = knativeClient.subscriptions().inNamespace(namespace).withName("my-subscription").get();
        Assert.assertNotNull(subscription);
        Assert.assertEquals(subscription.getMetadata().getLabels().size(), 1);
        Assert.assertEquals(subscription.getMetadata().getLabels().get("app"), "citrus");
        Assert.assertEquals(subscription.getSpec().getChannel().getName(), "my-channel");
        Assert.assertEquals(subscription.getSpec().getChannel().getKind(), "InMemoryChannel");
        Assert.assertEquals(subscription.getSpec().getSubscriber().getRef().getName(), "my-service");
    }
}
