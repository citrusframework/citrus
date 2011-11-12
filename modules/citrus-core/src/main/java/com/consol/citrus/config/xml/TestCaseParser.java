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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.*;
import com.consol.citrus.TestCaseMetaInfo.Status;
import com.consol.citrus.config.TestActionRegistry;
import com.consol.citrus.config.TestCaseFactory;
import com.consol.citrus.variable.VariableUtils;

/**
 * Bean definition parser for test case.
 * 
 * @author Christoph Deppisch
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class TestCaseParser implements BeanDefinitionParser {

    /**
     * (non-Javadoc)
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
	public final BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder testCaseFactory = BeanDefinitionBuilder.rootBeanDefinition(TestCaseFactory.class);
        BeanDefinitionBuilder testcase = BeanDefinitionBuilder.rootBeanDefinition(TestCase.class);

        String testName = element.getAttribute("name");
        if (!StringUtils.hasText(testName)) {
            throw new BeanCreationException("Please provide proper test case name");
        }
        
        testcase.addPropertyValue("name", testName);

        parseMetaInfo(testcase, element);
        parseVariableDefinitions(testcase, element);

        DescriptionElementParser.doParse(element, testcase);
        
        Element actionsElement = DomUtils.getChildElementByTagName(element, "actions");
        Element finallyBlockElement = DomUtils.getChildElementByTagName(element, "finally");

        testCaseFactory.addPropertyValue("testCase", testcase.getBeanDefinition());
        testCaseFactory.addPropertyValue("testChain", parseActions(actionsElement, parserContext, TestActionRegistry.getRegisteredActionParser()));
        testCaseFactory.addPropertyValue("finallyChain", parseActions(finallyBlockElement, parserContext, TestActionRegistry.getRegisteredActionParser()));

        parserContext.getRegistry().registerBeanDefinition(testName, testCaseFactory.getBeanDefinition());

        return parserContext.getRegistry().getBeanDefinition(testName);
    }

    /**
     * Parses action elements and adds them to a managed list.
     * @param actionsContainerElement the action container.
     * @param parserContext the current parser context.
     * @return
     */
    private ManagedList<BeanDefinition> parseActions(Element actionsContainerElement, ParserContext parserContext, 
            Map<String, BeanDefinitionParser> actionRegistry) {
        ManagedList<BeanDefinition> actions = new ManagedList<BeanDefinition>();
        
        if (actionsContainerElement != null) {
            List<Element> actionList = DomUtils.getChildElements(actionsContainerElement);
            for (Element action : actionList) {
                BeanDefinitionParser parser = (BeanDefinitionParser)actionRegistry.get(action.getTagName());
                
                if (parser ==  null) {
                    actions.add(parserContext.getReaderContext().getNamespaceHandlerResolver().resolve(action.getNamespaceURI()).parse(action, parserContext));
                } else {
                    actions.add(parser.parse(action, parserContext));
                }
            }
        }
        
        return actions;
    }

    /**
     * Parses all variable definitions and adds those to the bean definition 
     * builder for this test case.
     * @param testcase the target bean definition builder for this test case.
     * @param element the source element.
     */
    private void parseVariableDefinitions(BeanDefinitionBuilder testcase, Element element) {
        Element variablesElement = DomUtils.getChildElementByTagName(element, "variables");
        if (variablesElement != null) {
            Map<String, String> testVariables = new LinkedHashMap<String, String>();
            List<?> variableElements = DomUtils.getChildElementsByTagName(variablesElement, "variable");
            for (Iterator<?> iter = variableElements.iterator(); iter.hasNext();) {
                Element variableDefinition = (Element) iter.next();
                Element variableValueElement = DomUtils.getChildElementByTagName(variableDefinition, "value");
                if (variableValueElement == null) {
                    testVariables.put(variableDefinition.getAttribute("name"), variableDefinition.getAttribute("value"));
                } else {
                    Element variableScript = DomUtils.getChildElementByTagName(variableValueElement, "script");
                    String scriptEngine = variableScript.getAttribute("type");
                    testVariables.put(variableDefinition.getAttribute("name"), VariableUtils.getValueFromScript(scriptEngine, 
                            variableScript.getTextContent()));
                }
            }
            testcase.addPropertyValue("variableDefinitions", testVariables);
        }
    }

    /**
     * Parses meta information and adds it to the test case bean definition builder.
     * @param testcase the target bean definition builder for this test case.
     * @param element the source element.
     */
    private void parseMetaInfo(BeanDefinitionBuilder testcase, Element element) {
        Element metaInfoElement = DomUtils.getChildElementByTagName(element, "meta-info");
        if (metaInfoElement != null) {
            TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();

            Element authorElement = DomUtils.getChildElementByTagName(metaInfoElement, "author");
            Element creationDateElement = DomUtils.getChildElementByTagName(metaInfoElement, "creationdate");
            Element statusElement = DomUtils.getChildElementByTagName(metaInfoElement, "status");
            Element lastUpdatedByElement = DomUtils.getChildElementByTagName(metaInfoElement, "last-updated-by");
            Element lastUpdatedOnElement = DomUtils.getChildElementByTagName(metaInfoElement, "last-updated-on");

            metaInfo.setAuthor(DomUtils.getTextValue(authorElement));
            try {
                metaInfo.setCreationDate(new SimpleDateFormat("yyyy-MM-dd").parse(DomUtils.getTextValue(creationDateElement)));
            } catch (ParseException e) {
                throw new BeanCreationException("Unable to parse creation date", e);
            }
            
            String status = DomUtils.getTextValue(statusElement);
            if (status.equals("DRAFT")) {
                metaInfo.setStatus(Status.DRAFT);
            } else if (status.equals("READY_FOR_REVIEW")) {
                metaInfo.setStatus(Status.READY_FOR_REVIEW);
            } else if (status.equals("FINAL")) {
                metaInfo.setStatus(Status.FINAL);
            } else if (status.equals("DISABLED")) {
                metaInfo.setStatus(Status.DISABLED);
            }

            if (lastUpdatedByElement != null) {
                metaInfo.setLastUpdatedBy(DomUtils.getTextValue(lastUpdatedByElement));
            }

            if (lastUpdatedOnElement != null) {
                try {
                    metaInfo.setLastUpdatedOn(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(DomUtils.getTextValue(lastUpdatedOnElement)));
                } catch (ParseException e) {
                    throw new BeanCreationException("Unable to parse lastupdate date", e);
                }
            }

            testcase.addPropertyValue("metaInfo", metaInfo);
        }
    }
}
