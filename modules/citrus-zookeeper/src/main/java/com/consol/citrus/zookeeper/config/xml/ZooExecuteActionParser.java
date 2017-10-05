/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.zookeeper.config.xml;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.config.util.ValidateMessageParserUtil;
import com.consol.citrus.config.util.VariableExtractorParserUtil;
import com.consol.citrus.config.xml.DescriptionElementParser;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.validation.json.JsonPathMessageValidationContext;
import com.consol.citrus.variable.VariableExtractor;
import com.consol.citrus.zookeeper.actions.ZooExecuteAction;
import com.consol.citrus.zookeeper.command.ZooCommand;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bean definition parser for zookeeper client action in test case.
 *
 * @author Martin Maher
 * @since 2.5
 */
public class ZooExecuteActionParser implements BeanDefinitionParser {

    /**
     * ZooKeeper command to execute
     */
    private Class<? extends ZooCommand> zookeeperCommandClass;

    /**
     * Constructor using zookeeper command.
     *
     * @param commandClass
     */
    public <T extends ZooCommand> ZooExecuteActionParser(Class<T> commandClass) {
        this.zookeeperCommandClass = commandClass;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        ZooCommand command = null;
        try {
            command = zookeeperCommandClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new CitrusRuntimeException(e);
        }

        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(ZooExecuteAction.class);

        DescriptionElementParser.doParse(element, beanDefinition);
        BeanDefinitionParserUtils.setPropertyReference(beanDefinition, element.getAttribute("zookeeper-client"), "zookeeperClient");

        for (int i = 0; i < element.getAttributes().getLength(); i++) {
            Node attribute = element.getAttributes().item(i);
            if (!attribute.getNodeName().equals("zookeeper-client")) {
                command.getParameters().put(attribute.getNodeName(), attribute.getNodeValue());
            }
        }

        Element expectCmdResult = DomUtils.getChildElementByTagName(element, "expect");
        if (expectCmdResult != null) {
            beanDefinition.addPropertyValue("expectedCommandResult", DomUtils.getTextValue(DomUtils.getChildElementByTagName(expectCmdResult, "result")));
        }

        Element data = DomUtils.getChildElementByTagName(element, "data");
        if (data != null) {
            command.getParameters().put("data", DomUtils.getTextValue(data));
        }

        Element validateCmdResult = DomUtils.getChildElementByTagName(element, "validate");
        if (validateCmdResult != null) {
            beanDefinition.addPropertyValue("jsonPathMessageValidationContext", getValidationContext(validateCmdResult));
        }

        Element extractCmdResult = DomUtils.getChildElementByTagName(element, "extract");
        if (extractCmdResult != null) {
            beanDefinition.addPropertyValue("variableExtractors", getVariableExtractors(extractCmdResult));
        }

        beanDefinition.addPropertyValue("command", command);
        return beanDefinition.getBeanDefinition();
    }

    private List<VariableExtractor> getVariableExtractors(Element extractElement) {
        List<VariableExtractor> variableExtractors = new ArrayList<>();
        Map<String, String> extractJsonPath = new HashMap<>();
        List<?> messageValueElements = DomUtils.getChildElementsByTagName(extractElement, "message");
        VariableExtractorParserUtil.parseMessageElement(messageValueElements, extractJsonPath);
        if (!CollectionUtils.isEmpty(extractJsonPath)) {
            VariableExtractorParserUtil.addPayloadVariableExtractors(extractElement, variableExtractors, extractJsonPath);
        }
        return variableExtractors;
    }

    private JsonPathMessageValidationContext getValidationContext(Element validateElement) {
        Map<String, Object> validateJsonPathExpressions = new HashMap<>();
        ValidateMessageParserUtil.parseJsonPathElements(validateElement, validateJsonPathExpressions);
        JsonPathMessageValidationContext context = new JsonPathMessageValidationContext();
        context.setJsonPathExpressions(validateJsonPathExpressions);
        return context;
    }
}
