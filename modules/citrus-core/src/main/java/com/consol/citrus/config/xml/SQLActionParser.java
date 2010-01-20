/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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

public class SQLActionParser implements BeanDefinitionParser {

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
            String filePath = sqlResourceElement.getAttribute("file");
            if (filePath.startsWith("classpath:")) {
                beanDefinition.addPropertyValue("sqlResource", new ClassPathResource(filePath.substring("classpath:".length())));
            } else if (filePath.startsWith("file:")) {
                beanDefinition.addPropertyValue("sqlResource", new FileSystemResource(filePath.substring("file:".length())));
            } else {
                beanDefinition.addPropertyValue("sqlResource", new FileSystemResource(filePath));
            }
        }

        return beanDefinition.getBeanDefinition();
    }
}
