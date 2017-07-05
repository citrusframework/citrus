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

package com.consol.citrus.camel.actions;

import com.consol.citrus.Citrus;
import com.consol.citrus.camel.endpoint.CamelSyncEndpoint;
import com.consol.citrus.camel.endpoint.CamelSyncEndpointConfiguration;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.validation.ValidationUtils;
import com.consol.citrus.variable.VariableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class CamelControlBusAction extends AbstractCamelRouteAction {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CamelControlBusAction.class);

    /** The control bus action */
    private String action;

    /** The target Camel route */
    private String routeId;

    /** Language type */
    private String languageType = "simple";

    /** Language expression */
    private String languageExpression = "";

    /** The expected control bus response */
    private String result;

    /**
     * Default constructor.
     */
    public CamelControlBusAction() {
        setName("controlbus");
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
        camelEndpoint.createProducer().send(new DefaultMessage(VariableUtils.isVariableName(languageExpression) ? Citrus.VARIABLE_PREFIX + expression + Citrus.VARIABLE_SUFFIX : expression), context);

        Message response = camelEndpoint.createConsumer().receive(context);

        if (StringUtils.hasText(result)) {
            String expectedResult = context.replaceDynamicContentInString(result);

            if (log.isDebugEnabled()) {
                log.debug(String.format("Validating Camel controlbus response = '%s'", expectedResult));
            }

            ValidationUtils.validateValues(response.getPayload(String.class), expectedResult, "camelControlBusResult", context);
            log.info("Validation of Camel controlbus response successful - All values OK");
        }
    }

    /**
     * Sets the Camel control bus action.
     * @param action
     */
    public CamelControlBusAction setAction(String action) {
        this.action = action;
        return this;
    }

    /**
     * Gets the Camel control bus action.
     * @return
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the target Camel route id.
     * @param routeId
     */
    public CamelControlBusAction setRouteId(String routeId) {
        this.routeId = routeId;
        return this;
    }

    /**
     * Gets the target Camel route id.
     * @return
     */
    public String getRouteId() {
        return routeId;
    }

    /**
     * Sets the expected Camel control bus result.
     * @param result
     */
    public CamelControlBusAction setResult(String result) {
        this.result = result;
        return this;
    }

    /**
     * Gets the expected Camel control bus result.
     * @return
     */
    public String getResult() {
        return result;
    }

    /**
     * Sets the language type.
     * @param languageType
     */
    public void setLanguageType(String languageType) {
        this.languageType = languageType;
    }

    /**
     * Gets the language type.
     * @return
     */
    public String getLanguageType() {
        return languageType;
    }

    /**
     * Sets the language expression.
     * @param languageExpression
     */
    public void setLanguageExpression(String languageExpression) {
        this.languageExpression = languageExpression;
    }

    /**
     * Gets the language expression.
     * @return
     */
    public String getLanguageExpression() {
        return languageExpression;
    }
}
