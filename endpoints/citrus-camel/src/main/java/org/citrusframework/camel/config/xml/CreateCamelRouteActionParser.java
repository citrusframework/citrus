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

import org.citrusframework.camel.actions.CreateCamelRouteAction;
import org.citrusframework.config.xml.PayloadElementParser;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for creating Camel routes action in test case.
 *
 * @author Christoph Deppisch
 * @since 2.4
 */
public class CreateCamelRouteActionParser extends AbstractCamelRouteActionParser {

    @Override
    public void parse(BeanDefinitionBuilder beanDefinition, Element element, ParserContext parserContext) {
        beanDefinition.addPropertyValue("routeContext",
                PayloadElementParser.parseMessagePayload(DomUtils.getChildElementByTagName(element, "routeContext")));
    }

    @Override
    protected Class<CreateCamelRouteActionFactoryBean> getBeanDefinitionClass() {
        return CreateCamelRouteActionFactoryBean.class;
    }

    /**
     * Test action factory bean.
     */
    public static class CreateCamelRouteActionFactoryBean extends AbstractCamelRouteActionFactoryBean<CreateCamelRouteAction, CreateCamelRouteAction.Builder> {

        private final CreateCamelRouteAction.Builder builder = new CreateCamelRouteAction.Builder();

        /**
         * Sets the routeContext.
         *
         * @param routeContext
         */
        public void setRouteContext(String routeContext) {
            builder.routeContext(routeContext);
        }

        @Override
        public CreateCamelRouteAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return CreateCamelRouteAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public CreateCamelRouteAction.Builder getBuilder() {
            return builder;
        }
    }
}
