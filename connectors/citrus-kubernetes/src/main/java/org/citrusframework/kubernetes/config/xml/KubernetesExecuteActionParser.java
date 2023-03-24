/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.kubernetes.config.xml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.xml.AbstractTestActionFactoryBean;
import org.citrusframework.config.xml.DescriptionElementParser;
import org.citrusframework.kubernetes.actions.KubernetesExecuteAction;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.kubernetes.command.KubernetesCommand;
import org.citrusframework.kubernetes.message.KubernetesMessageHeaders;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import org.springframework.beans.factory.BeanCreationException;
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
 * Bean definition parser for kubernetes client action in test case.
 *
 * @author Christoph Deppisch
 * @since 2.7
 */
public class KubernetesExecuteActionParser<T extends KubernetesCommand> implements BeanDefinitionParser {

    /** Kubernetes command to execute */
    private Class<T> commandType;

    /**
     * Constructor using kubernetes command.
     * @param commandType
     */
    public KubernetesExecuteActionParser(Class<T> commandType) {
        this.commandType = commandType;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(KubernetesExecuteActionFactoryBean.class);

        DescriptionElementParser.doParse(element, beanDefinition);
        BeanDefinitionParserUtils.setPropertyReference(beanDefinition, element.getAttribute("client"), "kubernetesClient");

        T command = parseCommand(createCommand(commandType), element, parserContext);
        for (int i = 0; i < element.getAttributes().getLength(); i++) {
            Node attribute = element.getAttributes().item(i);
            if (!attribute.getNodeName().equals("client")) {
                command.getParameters().put(KubernetesMessageHeaders.KUBERNETES_PREFIX + attribute.getNodeName(), attribute.getNodeValue());
            }
        }

        Element controlCmdResult = DomUtils.getChildElementByTagName(element, "validate");
        if (controlCmdResult != null) {
            Element resultElement = DomUtils.getChildElementByTagName(controlCmdResult, "result");
            if (resultElement != null) {
                beanDefinition.addPropertyValue("commandResult", DomUtils.getTextValue(resultElement));
            }

            Map<String, Object> pathExpressions = new HashMap<>();
            List<?> pathElements = DomUtils.getChildElementsByTagName(controlCmdResult, "element");
            for (Iterator<?> iter = pathElements.iterator(); iter.hasNext();) {
                Element messageValue = (Element) iter.next();
                pathExpressions.put(messageValue.getAttribute("path"), messageValue.getAttribute("value"));
            }

            if (!pathExpressions.isEmpty()) {
                beanDefinition.addPropertyValue("commandResultExpressions", pathExpressions);
            }
        }

        beanDefinition.addPropertyValue("command", command);
        return beanDefinition.getBeanDefinition();
    }

    /**
     * Subclasses may add custom command parsing logic here.
     * @param command
     * @param element
     * @param parserContext
     * @return
     */
    protected T parseCommand(T command, Element element, ParserContext parserContext) {
        return command;
    }

    /**
     * Creates new Kubernetes command instance of given type.
     * @param commandType
     * @return
     */
    private T createCommand(Class<T> commandType) {
        try {
            return commandType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BeanCreationException("Failed to create Kubernetes command of type: " + commandType, e);
        }
    }

    /**
     * Test action factory bean.
     */
    public static class KubernetesExecuteActionFactoryBean extends AbstractTestActionFactoryBean<KubernetesExecuteAction, KubernetesExecuteAction.Builder> {

        @Autowired(required = false)
        @Qualifier("k8sClient")
        private KubernetesClient kubernetesClient;

        @Autowired(required = false)
        @Qualifier("defaultJsonMessageValidator")
        private MessageValidator<? extends ValidationContext> jsonMessageValidator;

        @Autowired(required = false)
        @Qualifier("defaultJsonPathMessageValidator")
        private MessageValidator<? extends ValidationContext> jsonPathMessageValidator;

        private final KubernetesExecuteAction.Builder builder = new KubernetesExecuteAction.Builder();

        /**
         * Sets kubernetes command to execute.
         * @param command
         * @return
         */
        public void setCommand(KubernetesCommand<?> command) {
            builder.command(command);
        }

        /**
         * Sets the kubernetes client.
         * @param kubernetesClient
         */
        public void setKubernetesClient(KubernetesClient kubernetesClient) {
            builder.client(kubernetesClient);
        }

        /**
         * Sets the expected control command result data.
         * @param controlCommandResult
         */
        public void setCommandResult(String controlCommandResult) {
            builder.result(controlCommandResult);
        }

        /**
         * Sets the expected command result expressions for path validation.
         * @param commandResultExpressions
         */
        public void setCommandResultExpressions(Map<String, Object> commandResultExpressions) {
            commandResultExpressions.forEach(builder::validate);
        }

        @Override
        public KubernetesExecuteAction getObject() throws Exception {
            if (kubernetesClient != null) {
                builder.client(kubernetesClient);
            }

            if (jsonMessageValidator != null) {
                builder.validator(jsonMessageValidator);
            }

            if (jsonPathMessageValidator != null) {
                builder.pathExpressionValidator(jsonPathMessageValidator);
            }

            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return KubernetesExecuteAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public KubernetesExecuteAction.Builder getBuilder() {
            return builder;
        }
    }
}
