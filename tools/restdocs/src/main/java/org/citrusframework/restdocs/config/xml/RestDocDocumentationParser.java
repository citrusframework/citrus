/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.restdocs.config.xml;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.*;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class RestDocDocumentationParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ListFactoryBean.class);

        ManagedList<RuntimeBeanReference> interceptors = new ManagedList<>();

        String id = element.getAttribute(ID_ATTRIBUTE);
        interceptors.add(new RuntimeBeanReference(id + "Configurer"));
        interceptors.add(new RuntimeBeanReference(id + "Interceptor"));

        builder.addPropertyValue("sourceList", interceptors);

        BeanDefinitionParserUtils.registerBean(id + "Configurer", new RestDocConfigurerParser().parseInternal(element, parserContext), parserContext, shouldFireEvents());
        BeanDefinitionParserUtils.registerBean(id + "Interceptor", new RestDocClientInterceptorParser().parseInternal(element, parserContext), parserContext, shouldFireEvents());

        return builder.getBeanDefinition();
    }
}
