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

package com.consol.citrus.camel.dsl;

import java.util.function.Function;

import com.consol.citrus.camel.actions.CamelControlBusAction;
import com.consol.citrus.camel.actions.CamelRouteActionBuilder;
import com.consol.citrus.camel.message.CamelDataFormatMessageProcessor;
import com.consol.citrus.camel.message.CamelMessageProcessor;
import com.consol.citrus.camel.message.CamelRouteProcessor;
import com.consol.citrus.camel.message.CamelTransformMessageProcessor;
import com.consol.citrus.camel.message.format.DataFormatClauseSupport;
import com.consol.citrus.endpoint.EndpointUriBuilder;
import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.builder.ExpressionClauseSupport;
import org.apache.camel.model.OutputDefinition;
import org.apache.camel.model.ProcessorDefinition;

/**
 * Support class combining all available Apache Camel Java DSL capabilities.
 * @author Christoph Deppisch
 */
public class CamelSupport {

    private CamelContext camelContext;

    /**
     * Static entrance for all Camel related Java DSL functionalities.
     * @return
     */
    public static CamelSupport camel() {
        return new CamelSupport();
    }

    /**
     * Static entrance for all Camel related Java DSL functionalities.
     * @return
     */
    public static CamelSupport camel(CamelContext camelContext) {
        return new CamelSupport()
                .camelContext(camelContext);
    }

    /**
     * Static entrance for all Camel related Java DSL functionalities.
     * @return
     */
    public CamelSupport camelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
        return this;
    }

    /**
     * Constructs proper endpoint uri from endpoint uri builder.
     * @return
     */
    public String endpoint(EndpointUriBuilder builder) {
        return "camel:" + builder.getUri();
    }

    /**
     * Entry point for the Camel endpoint builder DSL.
     * @return
     */
    public EndpointBuilderFactorySupport endpoints() {
        return new EndpointBuilderFactorySupport();
    }

    /**
     * Creates new control bus tet action builder and sets the Camel context.
     * @return
     */
    public CamelControlBusAction.Builder controlBus() {
        return CamelControlBusAction.Builder.controlBus()
                .context(camelContext);
    }

    /**
     * Message processor delegating to given Apache Camel processor.
     * @param processor
     * @return
     */
    public CamelMessageProcessor.Builder process(Processor processor) {
        return CamelMessageProcessor.Builder.process(processor)
                .camelContext(camelContext);
    }

    /**
     * Perform actions on a Camel route such as start/stop/create and process.
     * @return
     */
    public CamelRouteActionBuilder route() {
        return new CamelRouteActionBuilder()
                .context(camelContext);
    }

    /**
     * Route processor delegating to given Apache Camel processor.
     * @return
     */
    public CamelRouteProcessor.Builder route(
            Function<OutputDefinition<CamelRouteProcessor.Builder>, ProcessorDefinition<?>> configurer) {
        CamelRouteProcessor.Builder builder = CamelRouteProcessor.Builder.route()
                .camelContext(camelContext);

        configurer.apply(builder);

        return builder;
    }

    /**
     * Message processor transforming message with given expression.
     * @return
     */
    public ExpressionClauseSupport<CamelTransformMessageProcessor.Builder> transform() {
        return CamelTransformMessageProcessor.Builder.transform(camelContext);
    }

    /**
     * Transform message and convert body to given type using the Camel message converter implementation.
     * @return
     */
    public CamelTransformMessageProcessor.Builder convertBodyTo(Class<?> type) {
        return CamelTransformMessageProcessor.Builder.transform(camelContext).body(type);
    }

    /**
     * Message processor marshalling message body with given data format.
     * @return
     */
    public DataFormatClauseSupport<CamelDataFormatMessageProcessor.Builder> marshal() {
        return CamelDataFormatMessageProcessor.Builder.marshal().camelContext(camelContext);
    }

    /**
     * Message processor unmarshalling message body with given data format.
     * @return
     */
    public DataFormatClauseSupport<CamelDataFormatMessageProcessor.Builder> unmarshal() {
        return CamelDataFormatMessageProcessor.Builder.unmarshal().camelContext(camelContext);
    }
}
