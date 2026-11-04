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

import io.fabric8.knative.messaging.v1.Channel;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.knative.actions.messaging.CreateChannelAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreateChannelTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadKnativeActions() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/knative/yaml/create-channel.citrus.it.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("CreateChannelTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertEquals(CreateChannelAction.class, result.getTestAction(0).getClass());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        String namespace = "test";
        Channel channel = knativeClient.channels().inNamespace(namespace).withName("my-channel").get();
        Assertions.assertNotNull(channel);
        Assertions.assertEquals(1, channel.getMetadata().getLabels().size());
        Assertions.assertEquals("citrus", channel.getMetadata().getLabels().get("app"));
    }
}
