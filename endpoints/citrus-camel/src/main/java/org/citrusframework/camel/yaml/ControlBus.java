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

package org.citrusframework.camel.yaml;

import org.citrusframework.camel.actions.CamelControlBusAction;

public class ControlBus implements CamelActionBuilderWrapper<CamelControlBusAction.Builder> {

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
