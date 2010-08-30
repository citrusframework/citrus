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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.actions.ExecutePLSQLAction;
import com.consol.citrus.util.FileUtils;

/**
 * Bean definition parser for plsql action in test case.
 * 
 * @author Christoph Deppisch
 */
public class ExecutePLSQLActionParser implements BeanDefinitionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String dataSource = element.getAttribute("datasource");
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(ExecutePLSQLAction.class);
        
        beanDefinition.addPropertyValue("name", "psql:" + dataSource);
        beanDefinition.addPropertyReference("dataSource", dataSource);

        DescriptionElementParser.doParse(element, beanDefinition);

        Element scriptElement = DomUtils.getChildElementByTagName(element, "script");
        if (scriptElement != null) {
            beanDefinition.addPropertyValue("script", DomUtils.getTextValue(scriptElement).trim());
        }

        Element sqlResourceElement = DomUtils.getChildElementByTagName(element, "resource");
        if (sqlResourceElement != null) {
            beanDefinition.addPropertyValue("sqlResource", 
                    FileUtils.getResourceFromFilePath(sqlResourceElement.getAttribute("file")));
        }

        String ignoreErrors = element.getAttribute("ignore-errors");
        if (ignoreErrors != null && ignoreErrors.equals("true")) {
            beanDefinition.addPropertyValue("ignoreErrors", true);
        }

        return beanDefinition.getBeanDefinition();
    }
}
