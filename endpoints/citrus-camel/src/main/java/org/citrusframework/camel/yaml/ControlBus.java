/*
 * Copyright 2021 the original author or authors.
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

package org.citrusframework.camel.yaml;

import org.citrusframework.camel.actions.CamelControlBusAction;

/**
 * @author Christoph Deppisch
 */
public class ControlBus implements CamelRouteActionBuilderWrapper<CamelControlBusAction.Builder> {

    private final CamelControlBusAction.Builder builder = new CamelControlBusAction.Builder();

    private CamelControlBusAction.Builder.ControlBusRouteActionBuilder routeActionBuilder;

    public void setDescription(String value) {
        builder.description(value);
    }

    public void setRoute(String id) {
        routeActionBuilder = builder.route(id);
    }

    public void setAction(String action) {
        if (routeActionBuilder != null) {
            routeActionBuilder.action(action);
        }
    }

    public void setResult(String result) {
        builder.result(result);
    }

    public void setSimple(String expression) {
        builder.simple(expression);
    }

    public void setLanguage(Language language) {
        builder.language(language.getName(), language.getExpression());
    }

    @Override
    public CamelControlBusAction.Builder getBuilder() {
        return builder;
    }

    public static class Language {

        private String name;
        private String expression;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setExpression(String expression) {
            this.expression = expression;
        }

        public String getExpression() {
            return expression;
        }
    }
}
