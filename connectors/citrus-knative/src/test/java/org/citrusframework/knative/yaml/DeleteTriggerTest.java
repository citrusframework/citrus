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
import org.testng.Assert;
import org.testng.annotations.Test;

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

        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/knative/yaml/delete-trigger-test.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "DeleteTriggerTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 1L);
        Assert.assertEquals(result.getTestAction(0).getClass(), DeleteTriggerAction.class);
        Assert.assertTrue(result.getTestResult().isSuccess());

        trigger = knativeClient.triggers().inNamespace(namespace).withName("my-trigger").get();
        Assert.assertNull(trigger);
    }
}
