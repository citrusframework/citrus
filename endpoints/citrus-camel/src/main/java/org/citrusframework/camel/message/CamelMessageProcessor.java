/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.camel.message;

import java.util.Map;

import org.citrusframework.camel.dsl.CamelContextAware;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.support.DefaultExchange;

/**
 * Message processor delegates to Apache Camel processor and sets the message header and body from the processed Camel
 * exchange.
 *
 * @author Christoph Deppisch
 */
public class CamelMessageProcessor implements MessageProcessor {

    private final CamelContext camelContext;
    private final Processor processor;

    /**
     * Constructor initializing camel context and processor.
     * @param camelContext
     * @param processor
     */
    public CamelMessageProcessor(CamelContext camelContext, Processor processor) {
        this.camelContext = camelContext;
        this.processor = processor;
    }

    @Override
    public void process(Message message, TestContext context) {
        Exchange exchange;

        if (message.getPayload() instanceof Exchange) {
            exchange = message.getPayload(Exchange.class);
        } else {
            exchange = new DefaultExchange(camelContext);
            exchange.setPattern(ExchangePattern.InOut);
            for (Map.Entry<String, Object> header : message.getHeaders().entrySet()) {
                exchange.getMessage().setHeader(header.getKey(), header.getValue());
            }
            exchange.getMessage().setBody(message.getPayload());
        }

        try {
            processor.process(exchange);
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to process message with Apache Camel processor", e);
        }

        if (exchange.getException() != null) {
            throw new CitrusRuntimeException("Failed to process message with Apache Camel processor", exchange.getException());
        }

        if (!(message.getPayload() instanceof Exchange)) {
            message.setPayload(exchange.getMessage().getBody());
            exchange.getMessage().getHeaders().entrySet()
                    .stream()
                    .filter(entry -> !entry.getKey().equals(MessageHeaders.ID))
                    .forEach(entry -> message.setHeader(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * Fluent builder.
     */
    public static class Builder extends CamelMessageProcessorBuilder<CamelMessageProcessor, Builder> {
        private Processor processor;

        public static Builder process(Processor processor) {
            Builder builder = new Builder();
            builder.processor = processor;
            return builder;
        }

        @Override
        public CamelMessageProcessor doBuild() {
            return new CamelMessageProcessor(camelContext, processor);
        }
    }

    public abstract static class CamelMessageProcessorBuilder<T extends CamelMessageProcessor, B extends CamelMessageProcessorBuilder<T, B>>
            implements MessageProcessor.Builder<T, B>, ReferenceResolverAware, CamelContextAware<B> {

        protected CamelContext camelContext;
        protected ReferenceResolver referenceResolver;

        private final B self;

        public CamelMessageProcessorBuilder() {
            self = (B) this;
        }

        @Override
        public B camelContext(CamelContext camelContext) {
            this.camelContext = camelContext;
            return self;
        }

        public B withReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return self;
        }

        @Override
        public final T build() {
            if (camelContext == null) {
                if (referenceResolver != null) {
                    camelContext = referenceResolver.resolve(CamelContext.class);
                } else {
                    throw new CitrusRuntimeException("Missing proper Camel context for message processor - " +
                            "either set explicit context or provide a reference resolver");
                }
            }

            return doBuild();
        }

        /**
         * Subclasses must build message processor.
         * @return
         */
        public abstract T doBuild();

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
        }
    }
}
