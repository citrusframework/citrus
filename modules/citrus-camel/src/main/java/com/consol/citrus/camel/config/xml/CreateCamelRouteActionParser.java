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

package com.consol.citrus.camel.config.xml;

import com.consol.citrus.camel.actions.CreateCamelRouteAction;
import com.consol.citrus.config.xml.PayloadElementParser;
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
    protected Class<?> getBeanDefinitionClass() {
        return CreateCamelRouteAction.class;
    }
}
