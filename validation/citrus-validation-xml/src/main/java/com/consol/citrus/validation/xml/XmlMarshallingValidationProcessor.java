/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.validation.xml;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageProcessor;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;
import com.consol.citrus.validation.AbstractValidationProcessor;
import com.consol.citrus.validation.GenericValidationProcessor;
import com.consol.citrus.xml.StringSource;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.Assert;
import org.w3c.dom.Document;

/**
 * Validation callback automatically unmarshalling message payload so we work with
 * Java objects for validation.
 *
 * @author Christoph Deppisch
 */
public abstract class XmlMarshallingValidationProcessor<T> extends AbstractValidationProcessor<T> {

    /** Unmarshaller */
    private Unmarshaller unmarshaller;

    /**
     * Default constructor.
     */
    public XmlMarshallingValidationProcessor() {
        super();
    }

    /**
     * Default constructor with unmarshaller.
     */
    public XmlMarshallingValidationProcessor(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    @Override
    public void validate(Message message, TestContext context) {
        validate(unmarshalMessage(message), message.getHeaders(), context);
    }

    @SuppressWarnings("unchecked")
    private T unmarshalMessage(Message message) {
        if (unmarshaller == null) {
            Assert.notNull(referenceResolver, "Marshalling validation callback requires marshaller instance " +
            		"or proper reference resolver with nested bean definition of type marshaller");

            unmarshaller = referenceResolver.resolve(Unmarshaller.class);
        }

        try {
            return (T) unmarshaller.unmarshal(getPayloadSource(message.getPayload()));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to unmarshal message payload", e);
        }
    }

    /**
     * Creates the payload source for unmarshalling.
     * @param payload
     * @return
     */
    private Source getPayloadSource(Object payload) {
        Source source = null;

        if (payload instanceof String) {
            source = new StringSource((String) payload);
        } else if (payload instanceof File) {
            source = new StreamSource((File) payload);
        } else if (payload instanceof Document) {
            source = new DOMSource((Document) payload);
        } else if (payload instanceof Source) {
            source = (Source) payload;
        }

        if (source == null) {
            throw new CitrusRuntimeException("Failed to create payload source for unmarshalling message");
        }

        return source;
    }

    /**
     * Fluent builder.
     * @param <T>
     */
    public static final class Builder<T> implements MessageProcessor.Builder<XmlMarshallingValidationProcessor<T>, Builder<T>>, ReferenceResolverAware {

        private final Class<T> type;
        private Unmarshaller unmarshaller;
        private GenericValidationProcessor<T> validationProcessor;

        private ReferenceResolver referenceResolver;

        public Builder(Class<T> type) {
            this.type = type;
        }

        public static <T> Builder<T> validate(Class<T> type) {
            return new Builder<>(type);
        }

        public Builder<T> validator(GenericValidationProcessor<T> validationProcessor) {
            this.validationProcessor = validationProcessor;
            return this;
        }

        public Builder<T> unmarshaller(Unmarshaller unmarshaller) {
            this.unmarshaller = unmarshaller;
            return this;
        }

        public Builder<T> withReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return this;
        }

        @Override
        public XmlMarshallingValidationProcessor<T> build() {
            if (unmarshaller == null) {
                if (referenceResolver != null) {
                    unmarshaller = referenceResolver.resolve(Unmarshaller.class);
                } else {
                    throw new CitrusRuntimeException("Missing XML unmarshaller - " +
                            "please set proper unmarshaller or reference resolver");
                }
            }

            if (validationProcessor == null) {
                throw new CitrusRuntimeException("Missing validation processor - " +
                        "please add proper validation logic");
            }

            return new XmlMarshallingValidationProcessor<T>() {
                @Override
                public void validate(T payload, Map<String, Object> headers, TestContext context) {
                    validationProcessor.validate(payload, headers, context);
                }
            };
        }

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
        }
    }
}
