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
import io.fabric8.knative.pkg.apis.ConditionBuilder;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.knative.actions.eventing.VerifyBrokerAction;
import org.citrusframework.xml.XmlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VerifyBrokerTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadKnativeActions() {
        String namespace = "test";
        Broker broker = new BrokerBuilder()
                .withNewMetadata()
                .withName("my-broker")
                .withNamespace(namespace)
                .endMetadata()
                .withNewStatus()
                .withConditions(new ConditionBuilder()
                        .withType("Ready")
                        .withStatus("true")
                        .build())
                .endStatus()
                .build();

        knativeClient.brokers()
                .inNamespace(namespace)
                .resource(broker)
                .create();

        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/knative/xml/verify-broker.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("VerifyBrokerTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertEquals(VerifyBrokerAction.class, result.getTestAction(0).getClass());
        Assertions.assertTrue(result.getTestResult().isSuccess());
    }

    @Test
    public void shouldLoadKnativeActionsWithCondition() {
        String namespace = "test";
        Broker broker = new BrokerBuilder()
                .withNewMetadata()
                .withName("my-addressable-broker")
                .withNamespace(namespace)
                .endMetadata()
                .withNewStatus()
                .withConditions(new ConditionBuilder()
                        .withType("Addressable")
                        .withStatus("true")
                        .build())
                .endStatus()
                .build();

        knativeClient.brokers()
                .inNamespace(namespace)
                .resource(broker)
                .create();

        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/knative/xml/verify-broker-condition.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("VerifyBrokerConditionTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertEquals(VerifyBrokerAction.class, result.getTestAction(0).getClass());
        Assertions.assertTrue(result.getTestResult().isSuccess());
    }
}
