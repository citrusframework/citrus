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

import io.fabric8.knative.eventing.v1.Trigger;
import io.fabric8.knative.eventing.v1.TriggerBuilder;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.knative.actions.eventing.DeleteTriggerAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DeleteTriggerTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadKnativeActions() {
        String namespace = "test";
        Trigger trigger = new TriggerBuilder()
                .withNewMetadata()
                .withName("my-trigger")
                .withNamespace(namespace)
                .endMetadata()
                .build();

        knativeClient.triggers()
                .inNamespace(namespace)
                .resource(trigger)
                .create();

        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/knative/yaml/delete-trigger.citrus.it.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("DeleteTriggerTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertEquals(DeleteTriggerAction.class, result.getTestAction(0).getClass());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        trigger = knativeClient.triggers().inNamespace(namespace).withName("my-trigger").get();
        Assertions.assertNull(trigger);
    }
}
