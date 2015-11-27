/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.config.xml;

import com.consol.citrus.actions.StopTimerAction;
import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parses the Stop-Timer bean definition.
 *
 * @author Martin Maher
 * @since 2.5
 */
public class StopTimerParser implements BeanDefinitionParser {
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        // create new bean builder
        final BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(StopTimerAction.class);

        // see if there is a description
        DescriptionElementParser.doParse(element, builder);

        // add the local name of this element as the name
        builder.addPropertyValue("name", element.getLocalName());

        // optional attribute
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("timerId"), "timerId");

        // finally return the complete builder with its bean definition
        return builder.getBeanDefinition();
    }
}
