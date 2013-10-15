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

import com.consol.citrus.actions.TraceVariablesAction;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Bean definition parser for trace-variables action in test case.
 * 
 * @author Christoph Deppisch
 */
public class TraceVariablesActionParser implements BeanDefinitionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(TraceVariablesAction.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        List<String> variableNames = new ArrayList<String>();
        List<?> variableElements = DomUtils.getChildElementsByTagName(element, "variable");
        for (Iterator<?> iter = variableElements.iterator(); iter.hasNext();) {
            Element variable = (Element) iter.next();
            variableNames.add(variable.getAttribute("name"));
        }
        beanDefinition.addPropertyValue("variableNames", variableNames);

        return beanDefinition.getBeanDefinition();
    }
}
