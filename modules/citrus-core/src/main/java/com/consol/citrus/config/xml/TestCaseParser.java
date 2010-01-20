/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.config.xml;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.xerces.util.DOMUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.TestCaseMetaInfo.Status;


public class TestCaseParser implements BeanDefinitionParser {

    private String testCaseName = "";

    /**
     * (non-Javadoc)
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    @SuppressWarnings("unchecked")
	public final BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder testCaseFactory = BeanDefinitionBuilder.rootBeanDefinition(TestCaseFactory.class);
        BeanDefinitionBuilder testcase = BeanDefinitionBuilder.rootBeanDefinition(TestCase.class);

        testCaseName = element.getAttribute("name");

        testcase.addPropertyValue("name", testCaseName);

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
                throw new RuntimeException(e);
            }
            
            String status = DomUtils.getTextValue(statusElement);
            if(status.equals("DRAFT")) {
                metaInfo.setStatus(Status.DRAFT);
            } else if(status.equals("READY_FOR_REVIEW")) {
                metaInfo.setStatus(Status.READY_FOR_REVIEW);
            } else if(status.equals("FINAL")) {
                metaInfo.setStatus(Status.FINAL);
            } else if(status.equals("DISABLED")) {
                metaInfo.setStatus(Status.DISABLED);
            }

            if (lastUpdatedByElement != null) {
                metaInfo.setLastUpdatedBy(DomUtils.getTextValue(lastUpdatedByElement));
            }

            if (lastUpdatedOnElement != null) {
                try {
                    metaInfo.setLastUpdatedOn(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(DomUtils.getTextValue(lastUpdatedOnElement)));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }

            testcase.addPropertyValue("metaInfo", metaInfo);
        }

        DescriptionElementParser.doParse(element, testcase);

        Element testVariablesElement = DomUtils.getChildElementByTagName(element, "variables");

        if (testVariablesElement != null) {
            Map<String, String> testVariables = new LinkedHashMap<String, String>();
            List<?> variableElements = DomUtils.getChildElementsByTagName(testVariablesElement, "variable");
            for (Iterator<?> iter = variableElements.iterator(); iter.hasNext();) {
                Element variableDefinition = (Element) iter.next();
                testVariables.put(variableDefinition.getAttribute("name"), variableDefinition.getAttribute("value"));
            }
            testcase.addPropertyValue("variableDefinitions", testVariables);
        }

        Map<String, BeanDefinitionParser> actionRegistry = TestActionRegistry.getRegisteredActionParser();

        Element testChainElement = DomUtils.getChildElementByTagName(element, "actions");
        ManagedList testChain = new ManagedList();

        Element action = DOMUtil.getFirstChildElement(testChainElement);
        if (action != null) {
            do {
                BeanDefinitionParser parser = (BeanDefinitionParser)actionRegistry.get(action.getTagName());
                
                if(parser ==  null) {
                	testChain.add(parserContext.getReaderContext().getNamespaceHandlerResolver().resolve(action.getNamespaceURI()).parse(action, parserContext));
                } else {
                	testChain.add(parser.parse(action, parserContext));
                }
            } while ((action = DOMUtil.getNextSiblingElement(action)) != null);
        }

        Element finallyChainElement = DomUtils.getChildElementByTagName(element, "finally");
        ManagedList finallyChain = new ManagedList();

        if (finallyChainElement != null) {
            action = DOMUtil.getFirstChildElement(finallyChainElement);
            if (action != null) {
                do {
                    BeanDefinitionParser parser = (BeanDefinitionParser)actionRegistry.get(action.getTagName());
                    
                    if(parser ==  null) {
                    	finallyChain.add(parserContext.getReaderContext().getNamespaceHandlerResolver().resolve(action.getNamespaceURI()).parse(action, parserContext));
                    } else {
                    	finallyChain.add(parser.parse(action, parserContext));
                    }
                } while ((action = DOMUtil.getNextSiblingElement(action)) != null);
            }
        }

        testCaseFactory.addPropertyValue("testCase", testcase.getBeanDefinition());
        testCaseFactory.addPropertyValue("testChain", testChain);
        testCaseFactory.addPropertyValue("finallyChain", finallyChain);

        if (testCaseName != null) {
            parserContext.getRegistry().registerBeanDefinition(testCaseName, testCaseFactory.getBeanDefinition());
        } else {
            throw new RuntimeException("Test case name is missing");
        }

        return parserContext.getRegistry().getBeanDefinition(testCaseName);
    }
}
