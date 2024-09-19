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

import io.fabric8.knative.eventing.v1.Trigger;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.knative.actions.eventing.CreateTriggerAction;
import org.citrusframework.xml.XmlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CreateTriggerTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadKnativeActions() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/knative/xml/create-trigger-test.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "CreateTriggerTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);
        Assert.assertEquals(result.getTestAction(0).getClass(), CreateTriggerAction.class);
        Assert.assertTrue(result.getTestResult().isSuccess());

        String namespace = "test";
        Trigger trigger = knativeClient.triggers().inNamespace(namespace).withName("my-trigger-1").get();
        Assert.assertNotNull(trigger);
        Assert.assertEquals(trigger.getMetadata().getLabels().size(), 1);
        Assert.assertEquals(trigger.getMetadata().getLabels().get("app"), "citrus");
        Assert.assertEquals(trigger.getSpec().getBroker(), "my-broker");
        Assert.assertEquals(trigger.getSpec().getSubscriber().getRef().getKind(), "Service");
        Assert.assertEquals(trigger.getSpec().getSubscriber().getRef().getName(), "my-service");

        trigger = knativeClient.triggers().inNamespace(namespace).withName("my-trigger-2").get();
        Assert.assertNotNull(trigger);
        Assert.assertEquals(trigger.getMetadata().getLabels().size(), 1);
        Assert.assertEquals(trigger.getMetadata().getLabels().get("app"), "citrus");
        Assert.assertEquals(trigger.getSpec().getBroker(), "my-broker");
        Assert.assertEquals(trigger.getSpec().getSubscriber().getRef().getKind(), "Service");
        Assert.assertEquals(trigger.getSpec().getSubscriber().getRef().getName(), "my-service");
        Assert.assertEquals(trigger.getSpec().getFilter().getAttributes().size(), 1L);
        Assert.assertEquals(trigger.getSpec().getFilter().getAttributes().get("foo"), "bar");
    }
}
