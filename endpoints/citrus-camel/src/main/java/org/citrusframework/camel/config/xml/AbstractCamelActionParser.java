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

import org.apache.camel.CamelContext;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.actions.AbstractCamelAction;
import org.citrusframework.spring.config.util.BeanDefinitionParserUtils;
import org.citrusframework.spring.config.xml.AbstractTestActionFactoryBean;
import org.citrusframework.spring.config.xml.DescriptionElementParser;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public abstract class AbstractCamelActionParser implements BeanDefinitionParser {

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(getBeanDefinitionClass());

        DescriptionElementParser.doParse(element, beanDefinition);

        BeanDefinitionParserUtils.setPropertyReference(beanDefinition,
                element.getAttribute("camel-context"), "camelContext", CamelSettings.getContextName());
        parse(beanDefinition, element, parserContext);

        return beanDefinition.getBeanDefinition();
    }

    protected abstract Class<? extends AbstractCamelActionFactoryBean<?, ?>> getBeanDefinitionClass();

    protected abstract void parse(BeanDefinitionBuilder beanDefinition, Element element, ParserContext parserContext);

    public static abstract class AbstractCamelActionFactoryBean<T extends AbstractCamelAction, B extends AbstractCamelAction.Builder<?, ?>> extends AbstractTestActionFactoryBean<T, B> {

        public void setCamelContext(CamelContext camelContext) {
            getBuilder().context(camelContext);
        }
    }
}
