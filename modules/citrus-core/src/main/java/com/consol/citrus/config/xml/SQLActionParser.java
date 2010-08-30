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

import java.util.*;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.actions.ExecuteSQLAction;
import com.consol.citrus.actions.ExecuteSQLQueryAction;
import com.consol.citrus.util.FileUtils;

/**
 * Bean definition parser for sql action in test case.
 * 
 * @author Christoph Deppisch
 */
public class SQLActionParser implements BeanDefinitionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String dataSource = element.getAttribute("datasource");
        
        BeanDefinitionBuilder beanDefinition;

        List<?> validateElements = DomUtils.getChildElementsByTagName(element, "validate");
        List<?> extractElements = DomUtils.getChildElementsByTagName(element, "extract");
        
        if (!validateElements.isEmpty() || !extractElements.isEmpty()) {
            beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(ExecuteSQLQueryAction.class);
            beanDefinition.addPropertyValue("name", "sqlQuery:" + dataSource);
            
            Map<String, String> validateValues = new HashMap<String, String>();
            for (Iterator<?> iter = validateElements.iterator(); iter.hasNext();) {
                Element validate = (Element) iter.next();
                validateValues.put(validate.getAttribute("column"), validate.getAttribute("value"));
            }
            
            beanDefinition.addPropertyValue("validationElements", validateValues);
            
            Map<String, String> extractToVariables = new HashMap<String, String>();
            for (Iterator<?> iter = extractElements.iterator(); iter.hasNext();) {
                Element validate = (Element) iter.next();
                extractToVariables.put(validate.getAttribute("column"), validate.getAttribute("variable"));
            }
            
            beanDefinition.addPropertyValue("extractToVariablesMap", extractToVariables);
        } else {
            beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(ExecuteSQLAction.class);
            beanDefinition.addPropertyValue("name", "sqlUpdate:" + dataSource);
            
            String ignoreErrors = element.getAttribute("ignore-errors");
            if (ignoreErrors != null && ignoreErrors.equals("true")) {
                beanDefinition.addPropertyValue("ignoreErrors", true);
            }
        }
        
        beanDefinition.addPropertyReference("dataSource", dataSource);
        
        DescriptionElementParser.doParse(element, beanDefinition);

        List<String> statements = new ArrayList<String>();
        List<?> stmtElements = DomUtils.getChildElementsByTagName(element, "statement");
        for (Iterator<?> iter = stmtElements.iterator(); iter.hasNext();) {
            Element stmt = (Element) iter.next();
            statements.add(DomUtils.getTextValue(stmt));
        }
        beanDefinition.addPropertyValue("statements", statements);

        Element sqlResourceElement = DomUtils.getChildElementByTagName(element, "resource");
        if (sqlResourceElement != null) {
            beanDefinition.addPropertyValue("sqlResource", 
                    FileUtils.getResourceFromFilePath(sqlResourceElement.getAttribute("file")));
        }

        return beanDefinition.getBeanDefinition();
    }
}
