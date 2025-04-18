/*
 * Copyright the original author or authors.
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

package org.citrusframework.config.xml;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.TestCase;
import org.citrusframework.config.CitrusNamespaceParserRegistry;
import org.citrusframework.config.TestCaseFactory;
import org.citrusframework.util.StringUtils;
import org.citrusframework.variable.VariableUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Base test case for parsing the test case.
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class BaseTestCaseParser<T extends TestCase> implements BeanDefinitionParser {

    /**
     * The type of test case to create
     */
    private final Class<? extends TestCase> testCaseType;

    protected BaseTestCaseParser(Class<T> testCaseType) {
        this.testCaseType = testCaseType;
    }

    /**
     * Parses the test case element and returns a bean definition for test case
     * @param element the xml element to be parsed
     * @param parserContext the parser context
     * @return a bean definition for the test case bean
     */
    @Override
    public final BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder testCaseFactory = BeanDefinitionBuilder.rootBeanDefinition(TestCaseFactory.class);
        BeanDefinitionBuilder testCase = BeanDefinitionBuilder.rootBeanDefinition(testCaseType);

        String testName = element.getAttribute("name");
        if (!StringUtils.hasText(testName)) {
            throw new BeanCreationException("Please provide proper test case name");
        }

        testCase.addPropertyValue("name", testName);

        parseMetaInfo(testCase, element, parserContext);
        parseVariableDefinitions(testCase, element);

        DescriptionElementParser.doParse(element, testCase);

        Element actionsElement = DomUtils.getChildElementByTagName(element, "actions");
        Element finallyBlockElement = DomUtils.getChildElementByTagName(element, "finally");

        testCaseFactory.addPropertyValue("testCase", testCase.getBeanDefinition());
        testCaseFactory.addPropertyValue("testActions", parseActions(actionsElement, parserContext));
        testCaseFactory.addPropertyValue("finalActions", parseActions(finallyBlockElement, parserContext));

        parserContext.getRegistry().registerBeanDefinition(testName, testCaseFactory.getBeanDefinition());

        return parserContext.getRegistry().getBeanDefinition(testName);
    }

    /**
     * Parses action elements and adds them to a managed list.
     *
     * @param actionsContainerElement the action container.
     * @param parserContext           the current parser context.
     * @return
     */
    private ManagedList<BeanDefinition> parseActions(Element actionsContainerElement, ParserContext parserContext) {
        ManagedList<BeanDefinition> actions = new ManagedList<>();

        if (actionsContainerElement != null) {
            List<Element> actionList = DomUtils.getChildElements(actionsContainerElement);
            for (Element action : actionList) {
                BeanDefinitionParser parser = null;
                if (action.getNamespaceURI().equals(actionsContainerElement.getNamespaceURI())) {
                    parser = CitrusNamespaceParserRegistry.getBeanParser(action.getLocalName());
                }

                if (parser == null) {
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
     *
     * @param testCase the target bean definition builder for this test case.
     * @param element  the source element.
     */
    private void parseVariableDefinitions(BeanDefinitionBuilder testCase, Element element) {
        Element variablesElement = DomUtils.getChildElementByTagName(element, "variables");
        if (variablesElement != null) {
            Map<String, String> testVariables = new LinkedHashMap<>();
            List<?> variableElements = DomUtils.getChildElementsByTagName(variablesElement, "variable");
            for (Object variableElement : variableElements) {
                Element variableDefinition = (Element) variableElement;
                Element variableValueElement = DomUtils.getChildElementByTagName(variableDefinition, "value");
                if (variableValueElement == null) {
                    testVariables.put(variableDefinition.getAttribute("name"), variableDefinition.getAttribute("value"));
                } else {
                    Element variableScript = DomUtils.getChildElementByTagName(variableValueElement, "script");
                    if (variableScript != null) {
                        String scriptEngine = variableScript.getAttribute("type");
                        testVariables.put(variableDefinition.getAttribute("name"), VariableUtils.getValueFromScript(scriptEngine, variableScript.getTextContent()));
                    }

                    Element variableData = DomUtils.getChildElementByTagName(variableValueElement, "data");
                    if (variableData != null) {
                        testVariables.put(variableDefinition.getAttribute("name"), DomUtils.getTextValue(variableData).trim());
                    }
                }
            }
            testCase.addPropertyValue("variableDefinitions", testVariables);
        }
    }

    /**
     * Parses meta information and adds it to the test case bean definition builder.
     *  @param testCase the target bean definition builder for this test case.
     * @param element  the source element.
     * @param parserContext
     */
    private void parseMetaInfo(BeanDefinitionBuilder testCase, Element element, ParserContext parserContext) {
        Element metaInfoElement = DomUtils.getChildElementByTagName(element, "meta-info");
        if (metaInfoElement != null) {
            BeanDefinition metaInfoDefinition = CitrusNamespaceParserRegistry.getBeanParser("meta-info").parse(metaInfoElement, parserContext);
            testCase.addPropertyValue("metaInfo", metaInfoDefinition);
        }
    }
}
