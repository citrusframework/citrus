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

import jakarta.xml.bind.Marshaller;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.base.xml.Jaxb2Marshaller;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.xml.actons.dsl.TestRequest;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

public class XmlMarshallingMessageProcessorTest extends AbstractTestNGUnitTest {

    private final Jaxb2Marshaller marshaller = new Jaxb2Marshaller(TestRequest.class);

    @BeforeClass
    public void prepareMarshaller() {
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
    }

    @Test
    public void testMarshalMessage() {
        ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
        when(referenceResolver.resolveAll(org.citrusframework.api.xml.Marshaller.class))
                .thenReturn(Collections.singletonMap("marshaller", marshaller));
        context.setReferenceResolver(referenceResolver);

        Message message = new DefaultMessage(new TestRequest("Hello Citrus!"));
        new XmlMarshallingMessageProcessor().process(message, context);

        Assert.assertEquals(message.getPayload(String.class),
                "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
    }

    @Test
    public void testMarshalMessageWithExplicitMarshaller() {
        Message message = new DefaultMessage(new TestRequest("Hello Citrus!"));
        new XmlMarshallingMessageProcessor(marshaller).process(message, context);

        Assert.assertEquals(message.getPayload(String.class),
                "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
    }

    @Test
    public void testMarshalStringPayloadSkipped() {
        Message message = new DefaultMessage("<TestRequest><Message>already XML</Message></TestRequest>");
        new XmlMarshallingMessageProcessor(marshaller).process(message, context);

        Assert.assertEquals(message.getPayload(String.class),
                "<TestRequest><Message>already XML</Message></TestRequest>");
    }

    @Test
    public void testMarshalMessageWithPlaintextType() {
        Message message = new DefaultMessage(new TestRequest("Hello Citrus!"));
        message.setType(MessageType.PLAINTEXT.name());
        new XmlMarshallingMessageProcessor(marshaller).process(message, context);

        Assert.assertEquals(message.getPayload(String.class),
                "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
    }
}
