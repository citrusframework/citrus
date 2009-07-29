package com.consol.citrus.schema;

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


public class TestCaseParser implements BeanDefinitionParser {

    String testCaseName = "";

    /**
     * (non-Javadoc)
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public final BeanDefinition parse(Element element, ParserContext parseContext) {
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
            Element lastUpdatedByElement = DomUtils.getChildElementByTagName(metaInfoElement, "lastUpdatedBy");
            Element lastUpdatedOnElement = DomUtils.getChildElementByTagName(metaInfoElement, "lastUpdatedOn");

            metaInfo.setAuthor(DomUtils.getTextValue(authorElement));
            try {
                metaInfo.setCreationDate(new SimpleDateFormat("yyyy-MM-dd").parse(DomUtils.getTextValue(creationDateElement)));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            metaInfo.setStatus(DomUtils.getTextValue(statusElement));

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
            Map testVariables = new LinkedHashMap();
            List variableElements = DomUtils.getChildElementsByTagName(testVariablesElement, "variable");
            for (Iterator iter = variableElements.iterator(); iter.hasNext();) {
                Element variableDefinition = (Element) iter.next();
                testVariables.put(variableDefinition.getAttribute("name"), variableDefinition.getAttribute("value"));
            }
            testcase.addPropertyValue("variableDefinitions", testVariables);
        }

        Map actionRegistry = TestActionRegistry.getRegisteredActionParser();

        Element testChainElement = DomUtils.getChildElementByTagName(element, "actions");
        ManagedList testChain = new ManagedList();

        Element action = DOMUtil.getFirstChildElement(testChainElement);
        if (action != null) {
            do {
                BeanDefinitionParser parser = (BeanDefinitionParser)actionRegistry.get(action.getTagName());
                BeanDefinition beanDefinition = parser.parse(action, parseContext);

                testChain.add(beanDefinition);
            } while ((action = DOMUtil.getNextSiblingElement(action)) != null);
        }

        Element finallyChainElement = DomUtils.getChildElementByTagName(element, "cleanup");
        ManagedList finallyChain = new ManagedList();

        if (finallyChainElement != null) {
            action = DOMUtil.getFirstChildElement(finallyChainElement);
            if (action != null) {
                do {
                    BeanDefinitionParser parser = (BeanDefinitionParser)actionRegistry.get(action.getTagName());
                    BeanDefinition beanDefinition = parser.parse(action, parseContext);

                    finallyChain.add(beanDefinition);
                } while ((action = DOMUtil.getNextSiblingElement(action)) != null);
            }
        }

        testCaseFactory.addPropertyValue("testCase", testcase.getBeanDefinition());
        testCaseFactory.addPropertyValue("testChain", testChain);
        testCaseFactory.addPropertyValue("finallyChain", finallyChain);

        if (testCaseName != null) {
            parseContext.getRegistry().registerBeanDefinition(testCaseName, testCaseFactory.getBeanDefinition());
        } else {
            throw new RuntimeException("Test case name is missing");
        }

        return parseContext.getRegistry().getBeanDefinition(testCaseName);
    }
}
