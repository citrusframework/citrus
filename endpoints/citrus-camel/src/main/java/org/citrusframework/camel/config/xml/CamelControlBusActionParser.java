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

package org.citrusframework.camel.config.xml;

import org.citrusframework.camel.actions.CamelControlBusAction;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for starting Camel routes action in test case.
 *
 * @author Christoph Deppisch
 * @since 2.4
 */
public class CamelControlBusActionParser extends AbstractCamelRouteActionParser {

    @Override
    public void parse(BeanDefinitionBuilder beanDefinition, Element element, ParserContext parserContext) {
        Element routeElement = DomUtils.getChildElementByTagName(element, "route");
        if (routeElement != null) {
            beanDefinition.addPropertyValue("routeId", routeElement.getAttribute("id"));
            beanDefinition.addPropertyValue("action", routeElement.getAttribute("action"));
        }

        Element languageElement = DomUtils.getChildElementByTagName(element, "language");
        if (languageElement != null) {
            beanDefinition.addPropertyValue("languageType", languageElement.getAttribute("type"));
            beanDefinition.addPropertyValue("languageExpression", DomUtils.getTextValue(languageElement));
        }

        Element resultElement = DomUtils.getChildElementByTagName(element, "result");
        if (resultElement != null) {
            beanDefinition.addPropertyValue("result", DomUtils.getTextValue(resultElement));
        }
    }

    @Override
    protected Class<CamelControlBusActionFactoryBean> getBeanDefinitionClass() {
        return CamelControlBusActionFactoryBean.class;
    }

    /**
     * Test action factory bean.
     */
    public static class CamelControlBusActionFactoryBean extends AbstractCamelRouteActionFactoryBean<CamelControlBusAction, CamelControlBusAction.Builder> {

        private final CamelControlBusAction.Builder builder = new CamelControlBusAction.Builder();

        private String action;
        private String routeId;
        private String languageType = "simple";
        private String languageExpression = "";

        /**
         * Sets the Camel control bus action.
         * @param action
         */
        public void setAction(String action) {
            this.action = action;
        }

        /**
         * Sets the target Camel route id.
         * @param routeId
         */
        public void setRouteId(String routeId) {
            this.routeId = routeId;
        }

        /**
         * Sets the expected Camel control bus result.
         * @param result
         */
        public void setResult(String result) {
            builder.result(result);
        }

        /**
         * Sets the language type.
         * @param languageType
         */
        public void setLanguageType(String languageType) {
            this.languageType = languageType;
        }

        /**
         * Sets the language expression.
         * @param languageExpression
         */
        public void setLanguageExpression(String languageExpression) {
            this.languageExpression = languageExpression;
        }

        @Override
        public CamelControlBusAction getObject() throws Exception {
            builder.route(routeId, action);
            builder.language(languageType, languageExpression);

            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return CamelControlBusAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public CamelControlBusAction.Builder getBuilder() {
            return builder;
        }
    }
}
