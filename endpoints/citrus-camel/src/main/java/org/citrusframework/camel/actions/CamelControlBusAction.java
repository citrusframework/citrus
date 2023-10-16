/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.camel.actions;

import org.apache.camel.ServiceStatus;
import org.citrusframework.CitrusSettings;
import org.citrusframework.camel.endpoint.CamelSyncEndpoint;
import org.citrusframework.camel.endpoint.CamelSyncEndpointConfiguration;
import org.citrusframework.context.TestContext;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.ValidationUtils;
import org.citrusframework.variable.VariableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class CamelControlBusAction extends AbstractCamelRouteAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CamelControlBusAction.class);

    /** The control bus action */
    private final String action;

    /** The target Camel route */
    private final String routeId;

    /** Language type */
    private final String languageType;

    /** Language expression */
    private final String languageExpression;

    /** The expected control bus response */
    private final String result;

    /**
     * Default constructor.
     */
    public CamelControlBusAction(Builder builder) {
        super("controlbus", builder);

        this.action = builder.action;
        this.routeId = builder.routeId;
        this.languageType = builder.languageType;
        this.languageExpression = builder.languageExpression;
        this.result = builder.result;
    }

    @Override
    public void doExecute(TestContext context) {
        CamelSyncEndpointConfiguration endpointConfiguration = new CamelSyncEndpointConfiguration();

        if (StringUtils.hasText(languageExpression)) {
            endpointConfiguration.setEndpointUri(String.format("controlbus:language:%s", context.replaceDynamicContentInString(languageType)));
        } else {
            endpointConfiguration.setEndpointUri(String.format("controlbus:route?routeId=%s&action=%s",
                    context.replaceDynamicContentInString(routeId), context.replaceDynamicContentInString(action)));
        }

        endpointConfiguration.setCamelContext(camelContext);

        CamelSyncEndpoint camelEndpoint = new CamelSyncEndpoint(endpointConfiguration);

        String expression = context.replaceDynamicContentInString(VariableUtils.cutOffVariablesPrefix(languageExpression));
        camelEndpoint.createProducer().send(new DefaultMessage(VariableUtils.isVariableName(languageExpression) ? CitrusSettings.VARIABLE_PREFIX + expression + CitrusSettings.VARIABLE_SUFFIX : expression), context);

        Message response = camelEndpoint.createConsumer().receive(context);

        if (StringUtils.hasText(result)) {
            String expectedResult = context.replaceDynamicContentInString(result);

            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Validating Camel controlbus response = '%s'", expectedResult));
            }

            ValidationUtils.validateValues(response.getPayload(String.class), expectedResult, "camelControlBusResult", context);
            logger.info("Validation of Camel controlbus response successful - All values OK");
        }
    }

    /**
     * Gets the Camel control bus action.
     * @return
     */
    public String getAction() {
        return action;
    }

    /**
     * Gets the target Camel route id.
     * @return
     */
    public String getRouteId() {
        return routeId;
    }

    /**
     * Gets the expected Camel control bus result.
     * @return
     */
    public String getResult() {
        return result;
    }

    /**
     * Gets the language type.
     * @return
     */
    public String getLanguageType() {
        return languageType;
    }

    /**
     * Gets the language expression.
     * @return
     */
    public String getLanguageExpression() {
        return languageExpression;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractCamelRouteAction.Builder<CamelControlBusAction, CamelControlBusAction.Builder> {

        private String action;
        private String routeId;
        private String languageType = "simple";
        private String languageExpression = "";
        private String result;

        /**
         * Static entry method for the fluent API.
         * @return
         */
        public static Builder controlBus() {
            return new Builder();
        }

        /**
         * Sets route action to execute.
         * @param id
         */
        public ControlBusRouteActionBuilder route(String id) {
            this.routeId = id;
            return new ControlBusRouteActionBuilder(this);
        }

        /**
         * Sets route action to execute.
         * @param id
         * @param action
         */
        public Builder route(String id, String action) {
            this.routeId = id;
            this.action = action;
            return this;
        }

        /**
         * Sets a simple language expression to execute.
         * @param expression
         * @return
         */
        public Builder simple(String expression) {
            language("simple", expression);
            return this;
        }

        /**
         * Sets a language expression to execute.
         * @param language
         * @param expression
         * @return
         */
        public Builder language(String language, String expression) {
            this.languageType = language;
            this.languageExpression = expression;

            return this;
        }

        /**
         * Sets the expected result.
         * @param status
         * @return
         */
        public Builder result(ServiceStatus status) {
            this.result = status.name();
            return this;
        }

        /**
         * Sets the expected result.
         * @param result
         * @return
         */
        public Builder result(String result) {
            this.result = result;
            return this;
        }

        @Override
        public CamelControlBusAction doBuild() {
            return new CamelControlBusAction(this);
        }

        /**
         * Route action builder
         */
        public static class ControlBusRouteActionBuilder {

            private final Builder parent;

            public ControlBusRouteActionBuilder(Builder builder) {
                this.parent = builder;
            }

            /**
             * Performs generic action on the given route.
             * @param action
             * @return
             */
            public Builder action(String action) {
                parent.action = action;
                return parent;
            }

            /**
             * Start given route.
             * @return
             */
            public Builder start() {
                parent.action = "start";
                return parent;
            }

            /**
             * Stop given route.
             * @return
             */
            public Builder stop() {
                parent.action = "stop";
                return parent;
            }

            /**
             * Retrieve status of given route.
             * @return
             */
            public Builder status() {
                parent.action = "status";
                return parent;
            }
        }
    }
}
