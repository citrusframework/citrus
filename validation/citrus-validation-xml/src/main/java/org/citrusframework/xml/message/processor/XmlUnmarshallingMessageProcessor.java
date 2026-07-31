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

import java.io.File;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.citrusframework.api.xml.StringSource;
import org.citrusframework.api.xml.Unmarshaller;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.AbstractMessageProcessor;
import org.citrusframework.message.Message;
import org.citrusframework.message.processor.xml.XmlUnmarshallingMessageProcessorBuilder;
import org.w3c.dom.Document;

/**
 * Message processor that unmarshals an XML payload to a Java object
 * using a Citrus Unmarshaller.
 *
 * @since 4.6
 */
public class XmlUnmarshallingMessageProcessor extends AbstractMessageProcessor {

    private final Unmarshaller unmarshaller;

    public XmlUnmarshallingMessageProcessor() {
        this(null);
    }

    public XmlUnmarshallingMessageProcessor(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    @Override
    public void processMessage(Message message, TestContext context) {
        Unmarshaller xmlUnmarshaller = resolveUnmarshaller(context);

        try {
            message.setPayload(xmlUnmarshaller.unmarshal(getPayloadSource(message.getPayload())));
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to unmarshal XML message payload", e);
        }
    }

    private Source getPayloadSource(Object payload) {
        if (payload instanceof String stringPayload) {
            return new StringSource(stringPayload);
        } else if (payload instanceof File file) {
            return new StreamSource(file);
        } else if (payload instanceof Document document) {
            return new DOMSource(document);
        } else if (payload instanceof Source source) {
            return source;
        }

        throw new CitrusRuntimeException("Failed to create payload source for unmarshalling message");
    }

    private Unmarshaller resolveUnmarshaller(TestContext context) {
        if (unmarshaller != null) {
            return unmarshaller;
        }

        Map<String, Unmarshaller> unmarshallerMap = context.getReferenceResolver().resolveAll(Unmarshaller.class);
        if (unmarshallerMap.size() == 1) {
            return unmarshallerMap.values().iterator().next();
        } else {
            throw new CitrusRuntimeException(String.format("Unable to auto detect XML unmarshaller - " +
                    "found %d matching unmarshaller instances in reference resolver", unmarshallerMap.size()));
        }
    }

    public static class Builder implements XmlUnmarshallingMessageProcessorBuilder<XmlUnmarshallingMessageProcessor, Builder> {

        private Unmarshaller unmarshaller;

        @Override
        public Builder unmarshaller(Object unmarshaller) {
            if (unmarshaller instanceof Unmarshaller xmlUnmarshaller) {
                this.unmarshaller = xmlUnmarshaller;
            }

            return this;
        }

        public Builder unmarshaller(Unmarshaller unmarshaller) {
            this.unmarshaller = unmarshaller;
            return this;
        }

        @Override
        public XmlUnmarshallingMessageProcessor build() {
            return new XmlUnmarshallingMessageProcessor(unmarshaller);
        }
    }
}
