/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.config.xml;

import com.consol.citrus.config.TestActionRegistry;
import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.container.Assert;
import org.apache.xerces.util.DOMUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * Bean definition parser for assert action in test case.
 * 
 * @author Christoph Deppisch
 */
public class AssertParser implements BeanDefinitionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(Assert.class);

        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("exception"), "exception");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("message"), "message");

        DescriptionElementParser.doParse(element, builder);

        Map<String, BeanDefinitionParser> actionRegistry = TestActionRegistry.getRegisteredActionParser();

        Element action = DOMUtil.getFirstChildElement(DomUtils.getChildElementByTagName(element, "when"));
        if (action != null) {
            BeanDefinitionParser parser = actionRegistry.get(action.getTagName());
            
            if (parser !=  null) {
                builder.addPropertyValue("action", parser.parse(action, parserContext));
            } else {
                builder.addPropertyValue("action", parserContext.getReaderContext().getNamespaceHandlerResolver().resolve(action.getNamespaceURI()).parse(action, parserContext));
            }
        }

        builder.addPropertyValue("name", element.getLocalName());

        return builder.getBeanDefinition();
    }
}
