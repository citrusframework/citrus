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

import java.util.UUID;

import org.citrusframework.camel.dsl.CamelContextAware;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.OutputDefinition;
import org.apache.camel.model.RouteDefinition;

/**
 * Message processor builds new route from given processor definition and delegates to the Apache Camel route.
 * Sets the message header and body from the processed Camel exchange.
 *
 * @author Christoph Deppisch
 */
public class CamelRouteProcessor extends CamelMessageProcessor {

    /**
     * Constructor initializing camel context and processor.
     */
    public CamelRouteProcessor(Builder builder) {
        super(builder.camelContext, new RouteProcessor(builder.routeId, builder.routeBuilder));

        try {
            builder.camelContext.addRoutes(builder.routeBuilder);
        } catch (Exception e) {
            throw new CitrusRuntimeException(String.format("Failed to create route definitions in context '%s'", builder.camelContext.getName()), e);
        }
    }

    /**
     * Fluent builder.
     */
    public static class Builder extends OutputDefinition<Builder>
            implements MessageProcessor.Builder<CamelRouteProcessor, Builder>, ReferenceResolverAware, CamelContextAware<Builder> {

        private String routeId = "citrus-" + UUID.randomUUID();
        private RouteBuilder routeBuilder;

        public static Builder route() {
            return new Builder();
        }

        public static Builder route(RouteBuilder routeBuilder) {
            Builder builder = new Builder();
            builder.routeBuilder = routeBuilder;
            return builder;
        }

        protected CamelContext camelContext;
        protected ReferenceResolver referenceResolver;

        @Override
        public Builder routeId(String id) {
            this.routeId = id;
            return super.routeId(id);
        }

        @Override
        public Builder camelContext(CamelContext camelContext) {
            this.camelContext = camelContext;
            return this;
        }

        public Builder withReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return this;
        }

        @Override
        public final CamelRouteProcessor build() {
            if (camelContext == null) {
                if (referenceResolver != null) {
                    camelContext = referenceResolver.resolve(CamelContext.class);
                } else {
                    throw new CitrusRuntimeException("Missing proper Camel context for message processor - " +
                            "either set explicit context or provide a reference resolver");
                }
            }

            if (routeBuilder == null) {
                routeBuilder = new RouteBuilder(camelContext) {
                    @Override
                    public void configure() throws Exception {
                        RouteDefinition routeDefinition = from("direct:" + routeId)
                                .routeId(routeId);

                        routeDefinition.setOutputs(Builder.this.getOutputs());
                    }
                };
            }

            return new CamelRouteProcessor(this);
        }

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
        }
    }

    /**
     * Processor send exchange to given route builder using conventional direct endpoint.
     */
    private static class RouteProcessor implements Processor {
        private final String routeId;
        private final RouteBuilder routeBuilder;

        public RouteProcessor(String routeId, RouteBuilder routeBuilder) {
            this.routeId = routeId;
            this.routeBuilder = routeBuilder;
        }

        @Override
        public void process(Exchange exchange) throws Exception {
            try {
                routeBuilder.getContext().createProducerTemplate()
                        .send("direct:" + routeId, exchange);
            } finally {
                routeBuilder.getContext().removeRoute(routeId);
            }
        }
    }
}
