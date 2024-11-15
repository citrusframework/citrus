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

package org.citrusframework.camel.dsl;

import java.util.function.Function;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.builder.DataFormatClause;
import org.apache.camel.builder.ExpressionClauseSupport;
import org.apache.camel.model.OutputDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.actions.CamelActionBuilder;
import org.citrusframework.camel.actions.CamelContextActionBuilder;
import org.citrusframework.camel.actions.CamelControlBusAction;
import org.citrusframework.camel.actions.CamelExchangeActionBuilder;
import org.citrusframework.camel.actions.CamelJBangActionBuilder;
import org.citrusframework.camel.actions.CamelRouteActionBuilder;
import org.citrusframework.camel.actions.CreateCamelComponentAction;
import org.citrusframework.camel.endpoint.CamelEndpoint;
import org.citrusframework.camel.endpoint.CamelEndpointBuilder;
import org.citrusframework.camel.endpoint.CamelEndpointConfiguration;
import org.citrusframework.camel.endpoint.CamelSyncEndpoint;
import org.citrusframework.camel.endpoint.CamelSyncEndpointConfiguration;
import org.citrusframework.camel.message.CamelDataFormatMessageProcessor;
import org.citrusframework.camel.message.CamelMessageProcessor;
import org.citrusframework.camel.message.CamelRouteProcessor;
import org.citrusframework.camel.message.CamelTransformMessageProcessor;
import org.citrusframework.endpoint.EndpointUriBuilder;

/**
 * Support class combining all available Apache Camel Java DSL capabilities.
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
     * Sets Camel context.
     * @return
     */
    public CamelSupport camelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
        return this;
    }

    /**
     * Entrance for Camel context related Java DSL functionalities.
     * @return
     */
    public CamelContextActionBuilder camelContext() {
        return new CamelContextActionBuilder();
    }

    /**
     * Sends message using Camel endpointUris.
     * @return
     */
    public CamelExchangeActionBuilder<SendMessageAction.Builder> send() {
        return new CamelActionBuilder().send();
    }

    /**
     * Receives message using Camel endpointUris.
     * @return
     */
    public CamelExchangeActionBuilder<ReceiveMessageAction.Builder> receive() {
        return new CamelActionBuilder().receive();
    }

    /**
     * Constructs proper endpoint uri from endpoint uri builder.
     * @return
     */
    public CamelEndpoint endpoint(EndpointUriBuilder builder) {
        return new CamelEndpointBuilder()
                .camelContext(camelContext)
                .endpoint(builder)
                .build();
    }

    /**
     * Constructs proper endpoint uri from endpoint uri builder.
     * @return
     */
    public CamelEndpoint endpoint(String endpointUri) {
        return endpoint(endpointUri, false);
    }

    /**
     * Constructs proper endpoint uri from endpoint uri builder.
     * @return
     */
    public CamelEndpoint endpoint(String endpointUri, boolean inOut) {
        if (inOut) {
            CamelSyncEndpointConfiguration endpointConfiguration = new CamelSyncEndpointConfiguration();
            endpointConfiguration.setCamelContext(camelContext);
            endpointConfiguration.setEndpointUri(endpointUri);
            endpointConfiguration.setTimeout(CamelSettings.getTimeout());

            return new CamelSyncEndpoint(endpointConfiguration);
        } else {
            CamelEndpointConfiguration endpointConfiguration = new CamelEndpointConfiguration();
            endpointConfiguration.setCamelContext(camelContext);
            endpointConfiguration.setEndpointUri(endpointUri);
            endpointConfiguration.setTimeout(CamelSettings.getTimeout());

            return new CamelEndpoint(endpointConfiguration);
        }
    }

    /**
     * Constructs proper endpoint uri from endpoint uri builder.
     * @return
     */
    public String endpointUri(EndpointUriBuilder builder) {
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
     * Binds given component to the Camel context.
     * @return
     */
    public CreateCamelComponentAction.Builder bind(String name, Object component) {
        return new CamelActionBuilder()
                .camelContext(camelContext)
                .bind(name, component);
    }

    /**
     * Binds a component to the Camel context.
     * @return
     */
    public CreateCamelComponentAction.Builder bind() {
        return new CamelActionBuilder()
                .camelContext(camelContext)
                .bind();
    }

    /**
     * Creates new control bus test action builder and sets the Camel context.
     * @return
     */
    public CamelControlBusAction.Builder controlBus() {
        return new CamelActionBuilder()
                .camelContext(camelContext)
                .controlBus();
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
        return new CamelActionBuilder()
                .camelContext(camelContext)
                .route();
    }

    /**
     * Perform actions with Camel JBang.
     * @return
     */
    public CamelJBangActionBuilder jbang() {
        return new CamelActionBuilder()
                .camelContext(camelContext)
                .jbang();
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
    public DataFormatClause<CamelDataFormatMessageProcessor.Builder.InlineProcessDefinition> marshal() {
        return CamelDataFormatMessageProcessor.Builder.marshal(camelContext);
    }

    /**
     * Message processor unmarshalling message body with given data format.
     * @return
     */
    public DataFormatClause<CamelDataFormatMessageProcessor.Builder.InlineProcessDefinition> unmarshal() {
        return CamelDataFormatMessageProcessor.Builder.unmarshal(camelContext);
    }
}
