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

package org.citrusframework.camel.yaml;

import java.io.IOException;
import java.io.InputStream;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.YamlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CamelMessageProcessorTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadCamelActions() throws IOException {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/camel/yaml/camel-message-processor.citrus.it.yaml");

        CamelContext citrusCamelContext = new DefaultCamelContext();
        citrusCamelContext.start();

        context.getReferenceResolver().bind(CamelSettings.getContextName(), citrusCamelContext);
        context.getReferenceResolver().bind("camelContext", citrusCamelContext);

        testLoader.load();

        MessageQueue messageQueue = context.getReferenceResolver().resolve("messages", MessageQueue.class);

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("CamelMessageProcessorTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(5L, result.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, result.getTestAction(0).getClass());

        Message receivedMessage = messageQueue.receive();
        Assertions.assertNotNull(receivedMessage);
        Assertions.assertTrue(receivedMessage.getPayload() instanceof InputStream);
        Assertions.assertEquals(FileUtils.readToString(receivedMessage.getPayload(InputStream.class)).trim(), "SGVsbG8gZnJvbSBDaXRydXMh");

        receivedMessage = messageQueue.receive();
        Assertions.assertNotNull(receivedMessage);
        Assertions.assertTrue(receivedMessage.getPayload() instanceof InputStream);
        Assertions.assertEquals(StringUtils.normalizeWhitespace(
                FileUtils.readToString(receivedMessage.getPayload(InputStream.class)), false, true), """
                SGVs
                bG8g
                ZnJv
                bSBD
                aXRy
                dXMh
                """);

        receivedMessage = messageQueue.receive();
        Assertions.assertNotNull(receivedMessage);
        Assertions.assertTrue(receivedMessage.getPayload() instanceof InputStream);
        Assertions.assertEquals(FileUtils.readToString(receivedMessage.getPayload(InputStream.class)), "Hello from Citrus!");

        receivedMessage = messageQueue.receive();
        Assertions.assertNotNull(receivedMessage);
        Assertions.assertTrue(receivedMessage.getPayload() instanceof InputStream);
        Assertions.assertEquals(FileUtils.readToString(receivedMessage.getPayload(InputStream.class)), "Hello from Citrus!");

        receivedMessage = messageQueue.receive();
        Assertions.assertNotNull(receivedMessage);
        Assertions.assertEquals("Citrus rocks!", receivedMessage.getPayload(String.class));
    }
}
