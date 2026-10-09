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

package org.citrusframework.camel.config.xml;

import java.util.List;

import org.citrusframework.camel.actions.AbstractCamelRouteAction;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @since 2.4
 */
public abstract class AbstractCamelRouteActionParser extends AbstractCamelActionParser {

    @Override
    protected abstract Class<? extends AbstractCamelRouteActionFactoryBean<?, ?>> getBeanDefinitionClass();

    @Override
    protected abstract void parse(BeanDefinitionBuilder beanDefinition, Element element, ParserContext parserContext);

    /**
     * Test action factory bean.
     */
    public static abstract class AbstractCamelRouteActionFactoryBean<T extends AbstractCamelRouteAction, B extends AbstractCamelRouteAction.Builder<?, ?>> extends AbstractCamelActionFactoryBean<T, B> {

        /**
         * Sets the Camel routes.
         * @param routeIds
         */
        public void setRouteIds(List<String> routeIds) {
            getBuilder().routeIds(routeIds);
        }

    }
}
