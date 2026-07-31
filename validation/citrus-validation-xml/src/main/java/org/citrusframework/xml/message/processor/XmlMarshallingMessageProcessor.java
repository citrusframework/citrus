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

import java.util.Map;

import org.citrusframework.api.xml.Marshaller;
import org.citrusframework.api.xml.StringResult;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.AbstractMessageProcessor;
import org.citrusframework.message.Message;
import org.citrusframework.message.processor.xml.XmlMarshallingMessageProcessorBuilder;

/**
 * Message processor that marshals a Java object payload to an XML string
 * using a Citrus Marshaller.
 *
 * @since 4.6
 */
public class XmlMarshallingMessageProcessor extends AbstractMessageProcessor {

    private final Marshaller marshaller;

    public XmlMarshallingMessageProcessor() {
        this(null);
    }

    public XmlMarshallingMessageProcessor(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    @Override
    public void processMessage(Message message, TestContext context) {
        if (message.getPayload() == null || message.getPayload() instanceof String) {
            return;
        }

        Marshaller xmlMarshaller = resolveMarshaller(context);
        StringResult result = new StringResult();

        try {
            xmlMarshaller.marshal(message.getPayload(), result);
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to marshal message payload to XML", e);
        }

        message.setPayload(result.toString());
    }

    private Marshaller resolveMarshaller(TestContext context) {
        if (marshaller != null) {
            return marshaller;
        }

        Map<String, Marshaller> marshallerMap = context.getReferenceResolver().resolveAll(Marshaller.class);
        if (marshallerMap.size() == 1) {
            return marshallerMap.values().iterator().next();
        } else {
            throw new CitrusRuntimeException(String.format("Unable to auto detect XML marshaller - " +
                    "found %d matching marshaller instances in reference resolver", marshallerMap.size()));
        }
    }

    public static class Builder implements XmlMarshallingMessageProcessorBuilder<XmlMarshallingMessageProcessor, Builder> {

        private Marshaller marshaller;

        @Override
        public Builder marshaller(Object marshaller) {
            if (marshaller instanceof Marshaller xmlMarshaller) {
                this.marshaller = xmlMarshaller;
            }

            return this;
        }

        public Builder marshaller(Marshaller marshaller) {
            this.marshaller = marshaller;
            return this;
        }

        @Override
        public XmlMarshallingMessageProcessor build() {
            return new XmlMarshallingMessageProcessor(marshaller);
        }
    }
}
