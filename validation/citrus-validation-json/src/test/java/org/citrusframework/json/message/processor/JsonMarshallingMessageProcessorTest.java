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

import org.citrusframework.json.actions.dsl.TestRequest;
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

public class JsonMarshallingMessageProcessorTest extends AbstractTestNGUnitTest {

    private final JsonMapper mapper = JsonMapper.shared();

    @Test
    public void testMarshalMessage() {
        ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
        when(referenceResolver.resolveAll(ObjectMapper.class)).thenReturn(Collections.singletonMap("mapper", mapper));
        context.setReferenceResolver(referenceResolver);

        Message message = new DefaultMessage(new TestRequest("Hello Citrus!"));
        new JsonMarshallingMessageProcessor().process(message, context);

        Assert.assertEquals(message.getPayload(String.class), "{\"message\":\"Hello Citrus!\"}");
    }

    @Test
    public void testMarshalMessageWithExplicitMapper() {
        Message message = new DefaultMessage(new TestRequest("Hello Citrus!"));
        new JsonMarshallingMessageProcessor(mapper).process(message, context);

        Assert.assertEquals(message.getPayload(String.class), "{\"message\":\"Hello Citrus!\"}");
    }

    @Test
    public void testMarshalStringPayloadSkipped() {
        Message message = new DefaultMessage("{\"message\":\"already JSON\"}");
        new JsonMarshallingMessageProcessor(mapper).process(message, context);

        Assert.assertEquals(message.getPayload(String.class), "{\"message\":\"already JSON\"}");
    }

    @Test
    public void testMarshalMessageWithPlaintextType() {
        Message message = new DefaultMessage(new TestRequest("Hello Citrus!"));
        message.setType(MessageType.PLAINTEXT.name());
        new JsonMarshallingMessageProcessor(mapper).process(message, context);

        Assert.assertEquals(message.getPayload(String.class), "{\"message\":\"Hello Citrus!\"}");
    }
}
