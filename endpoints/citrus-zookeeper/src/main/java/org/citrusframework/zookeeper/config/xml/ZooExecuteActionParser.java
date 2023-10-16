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

package org.citrusframework.zookeeper.config.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.util.ValidateMessageParserUtil;
import org.citrusframework.config.util.VariableExtractorParserUtil;
import org.citrusframework.config.xml.AbstractTestActionFactoryBean;
import org.citrusframework.config.xml.DescriptionElementParser;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.json.JsonPathMessageValidationContext;
import org.citrusframework.variable.VariableExtractor;
import org.citrusframework.zookeeper.actions.ZooExecuteAction;
import org.citrusframework.zookeeper.client.ZooClient;
import org.citrusframework.zookeeper.command.ZooCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Bean definition parser for zookeeper client action in test case.
 *
 * @author Martin Maher
 * @since 2.5
 */
public class ZooExecuteActionParser implements BeanDefinitionParser {

    /** ZooKeeper command to execute */
    private final Class<? extends ZooCommand<?>> zookeeperCommandClass;

    /**
     * Constructor using zookeeper command.
     * @param commandClass
     */
    public <T extends ZooCommand<?>> ZooExecuteActionParser(Class<T> commandClass) {
        this.zookeeperCommandClass = commandClass;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        ZooCommand command;
        try {
            command = zookeeperCommandClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new CitrusRuntimeException(e);
        }

        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(ZooExecuteActionFactoryBean.class);

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
        Map<String, Object> extractJsonPath = new LinkedHashMap<>();
        List<?> messageValueElements = DomUtils.getChildElementsByTagName(extractElement, "message");
        VariableExtractorParserUtil.parseMessageElement(messageValueElements, extractJsonPath);
        if (!extractJsonPath.isEmpty()) {
            VariableExtractorParserUtil.addPayloadVariableExtractors(extractElement, variableExtractors, extractJsonPath);
        }
        return variableExtractors;
    }

    private JsonPathMessageValidationContext getValidationContext(Element validateElement) {
        Map<String, Object> validateJsonPathExpressions = new HashMap<>();
        ValidateMessageParserUtil.parseJsonPathElements(validateElement, validateJsonPathExpressions);
        return new JsonPathMessageValidationContext.Builder()
                .expressions(validateJsonPathExpressions)
                .build();
    }

    /**
     * Test action factory bean.
     */
    public static class ZooExecuteActionFactoryBean extends AbstractTestActionFactoryBean<ZooExecuteAction, ZooExecuteAction.Builder> {

        private final ZooExecuteAction.Builder builder = new ZooExecuteAction.Builder();

        @Autowired(required = false)
        @Qualifier("zookeeperClient")
        private ZooClient zookeeperClient;

        @Autowired(required = false)
        @Qualifier("zookeeperCommandResultMapper")
        private ObjectMapper jsonMapper;

        @Autowired(required = false)
        @Qualifier("defaultJsonMessageValidator")
        private MessageValidator<? extends ValidationContext> jsonMessageValidator;

        @Autowired(required = false)
        @Qualifier("defaultJsonPathMessageValidator")
        private MessageValidator<? extends ValidationContext> jsonPathMessageValidator;

        /**
         * Sets zookeeper command to execute.
         *
         * @param command
         * @return
         */
        public void setCommand(ZooCommand<?> command) {
            builder.command(command);
        }

        /**
         * Sets the zookeeper client.
         *
         * @param zookeeperClient
         */
        public void setZookeeperClient(ZooClient zookeeperClient) {
            builder.client(zookeeperClient);
        }

        /**
         * Sets the expected command result data.
         *
         * @param expectedCommandResult
         */
        public void setExpectedCommandResult(String expectedCommandResult) {
            builder.result(expectedCommandResult);
        }

        /**
         * Sets the JSON object mapper.
         *
         * @param jsonMapper
         */
        public void setJsonMapper(ObjectMapper jsonMapper) {
            builder.mapper(jsonMapper);
        }

        /**
         * Set the list of variable extractors.
         *
         * @param variableExtractors the variableExtractors to set
         */
        public void setVariableExtractors(List<VariableExtractor> variableExtractors) {
            variableExtractors.forEach(builder::extract);
        }

        /**
         * Sets the JsonPathMessageValidationContext for this action.
         *
         * @param jsonPathMessageValidationContext the json-path validation context
         */
        public void setJsonPathMessageValidationContext(JsonPathMessageValidationContext jsonPathMessageValidationContext) {
            builder.validationContext(jsonPathMessageValidationContext);
        }

        @Override
        public ZooExecuteAction getObject() throws Exception {
            if (zookeeperClient != null) {
                builder.client(zookeeperClient);
            }

            if (jsonMessageValidator != null) {
                builder.validator(jsonMessageValidator);
            }

            if (jsonPathMessageValidator != null) {
                builder.pathExpressionValidator(jsonPathMessageValidator);
            }

            if (jsonMapper != null) {
                builder.mapper(jsonMapper);
            }



            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return ZooExecuteAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public ZooExecuteAction.Builder getBuilder() {
            return builder;
        }
    }
}
