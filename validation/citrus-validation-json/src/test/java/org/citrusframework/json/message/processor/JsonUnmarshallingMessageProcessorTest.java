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

package org.citrusframework.json.message.processor;

import java.util.Collections;
import java.util.Map;

import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class JsonUnmarshallingMessageProcessorTest extends AbstractTestNGUnitTest {

    private final JsonMapper mapper = JsonMapper.shared();

    @Test
    public void testUnmarshalMessage() {
        ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
        when(referenceResolver.resolveAll(ObjectMapper.class)).thenReturn(Collections.singletonMap("mapper", mapper));
        context.setReferenceResolver(referenceResolver);

        Message message = new DefaultMessage("{\"message\":\"Hello Citrus!\"}");
        new JsonUnmarshallingMessageProcessor(Map.class).process(message, context);

        Assert.assertTrue(message.getPayload() instanceof Map);
        Map<String, Object> result = (Map<String, Object>) message.getPayload();
        Assert.assertEquals(result.get("message"), "Hello Citrus!");
    }

    @Test
    public void testUnmarshalMessageWithExplicitMapper() {
        Message message = new DefaultMessage("{\"message\":\"Hello Citrus!\"}");
        new JsonUnmarshallingMessageProcessor(Map.class, mapper).process(message, context);

        Assert.assertTrue(message.getPayload() instanceof Map);
        Map<String, Object> result = (Map<String, Object>) message.getPayload();
        Assert.assertEquals(result.get("message"), "Hello Citrus!");
    }

    @Test
    public void testUnmarshalMessageWithPlaintextType() {
        Message message = new DefaultMessage("{\"message\":\"Hello Citrus!\"}");
        message.setType(MessageType.PLAINTEXT.name());
        new JsonUnmarshallingMessageProcessor(Map.class, mapper).process(message, context);

        Assert.assertTrue(message.getPayload() instanceof Map);
        Map<String, Object> result = (Map<String, Object>) message.getPayload();
        Assert.assertEquals(result.get("message"), "Hello Citrus!");
    }
}
