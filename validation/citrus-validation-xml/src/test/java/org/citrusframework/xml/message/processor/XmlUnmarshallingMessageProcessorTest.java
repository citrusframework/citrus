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

package org.citrusframework.xml.message.processor;

import java.util.Collections;

import org.citrusframework.api.xml.Unmarshaller;
import org.citrusframework.base.xml.Jaxb2Marshaller;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.xml.actons.dsl.TestRequest;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

public class XmlUnmarshallingMessageProcessorTest extends AbstractTestNGUnitTest {

    private final Jaxb2Marshaller unmarshaller = new Jaxb2Marshaller(TestRequest.class);

    @Test
    public void testUnmarshalMessage() {
        ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
        when(referenceResolver.resolveAll(Unmarshaller.class))
                .thenReturn(Collections.singletonMap("unmarshaller", unmarshaller));
        context.setReferenceResolver(referenceResolver);

        Message message = new DefaultMessage("<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
        new XmlUnmarshallingMessageProcessor().process(message, context);

        Assert.assertTrue(message.getPayload() instanceof TestRequest);
        Assert.assertEquals(((TestRequest) message.getPayload()).getMessage(), "Hello Citrus!");
    }

    @Test
    public void testUnmarshalMessageWithExplicitUnmarshaller() {
        Message message = new DefaultMessage("<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
        new XmlUnmarshallingMessageProcessor(unmarshaller).process(message, context);

        Assert.assertTrue(message.getPayload() instanceof TestRequest);
        Assert.assertEquals(((TestRequest) message.getPayload()).getMessage(), "Hello Citrus!");
    }

    @Test
    public void testUnmarshalMessageWithPlaintextType() {
        Message message = new DefaultMessage("<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
        message.setType(MessageType.PLAINTEXT.name());
        new XmlUnmarshallingMessageProcessor(unmarshaller).process(message, context);

        Assert.assertTrue(message.getPayload() instanceof TestRequest);
        Assert.assertEquals(((TestRequest) message.getPayload()).getMessage(), "Hello Citrus!");
    }
}
