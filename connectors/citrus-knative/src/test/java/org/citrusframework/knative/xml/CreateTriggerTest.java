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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreateTriggerTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadKnativeActions() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/knative/xml/create-trigger.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("CreateTriggerTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(2L, result.getActionCount());
        Assertions.assertEquals(CreateTriggerAction.class, result.getTestAction(0).getClass());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        String namespace = "test";
        Trigger trigger = knativeClient.triggers().inNamespace(namespace).withName("my-trigger-1").get();
        Assertions.assertNotNull(trigger);
        Assertions.assertEquals(1, trigger.getMetadata().getLabels().size());
        Assertions.assertEquals("citrus", trigger.getMetadata().getLabels().get("app"));
        Assertions.assertEquals("my-broker", trigger.getSpec().getBroker());
        Assertions.assertEquals("Service", trigger.getSpec().getSubscriber().getRef().getKind());
        Assertions.assertEquals("my-service", trigger.getSpec().getSubscriber().getRef().getName());

        trigger = knativeClient.triggers().inNamespace(namespace).withName("my-trigger-2").get();
        Assertions.assertNotNull(trigger);
        Assertions.assertEquals(1, trigger.getMetadata().getLabels().size());
        Assertions.assertEquals("citrus", trigger.getMetadata().getLabels().get("app"));
        Assertions.assertEquals("my-broker", trigger.getSpec().getBroker());
        Assertions.assertEquals("Service", trigger.getSpec().getSubscriber().getRef().getKind());
        Assertions.assertEquals("my-service", trigger.getSpec().getSubscriber().getRef().getName());
        Assertions.assertEquals(1L, trigger.getSpec().getFilter().getAttributes().size());
        Assertions.assertEquals("bar", trigger.getSpec().getFilter().getAttributes().get("foo"));
    }
}
