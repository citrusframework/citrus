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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.camel.actions.CamelControlBusAction;
import org.apache.camel.ServiceStatus;
import org.apache.camel.builder.SimpleBuilder;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class CamelControlBusActionBuilder extends AbstractTestActionBuilder<CamelControlBusAction> {

    /**
     * Default constructor with test action.
     *
     * @param action
     */
    public CamelControlBusActionBuilder(CamelControlBusAction action) {
        super(action);
    }

    /**
     * Sets route action to execute.
     * @param id
     * @param action
     */
    public CamelControlBusActionBuilder route(String id, String action) {
        super.action.setRouteId(id);
        super.action.setAction(action);
        return this;
    }

    /**
     * Sets a simple language expression to execute.
     * @param simpleExpression
     * @return
     */
    public CamelControlBusActionBuilder language(SimpleBuilder simpleExpression) {
        language("simple", simpleExpression.getText());
        return this;
    }

    /**
     * Sets a language expression to execute.
     * @param language
     * @param expression
     * @return
     */
    public CamelControlBusActionBuilder language(String language, String expression) {
        action.setLanguageType(language);
        action.setLanguageExpression(expression);

        return this;
    }

    /**
     * Sets the expected result.
     * @param status
     * @return
     */
    public CamelControlBusActionBuilder result(ServiceStatus status) {
        action.setResult(status.name());
        return this;
    }

    /**
     * Sets the expected result.
     * @param result
     * @return
     */
    public CamelControlBusActionBuilder result(String result) {
        action.setResult(result);
        return this;
    }
}
