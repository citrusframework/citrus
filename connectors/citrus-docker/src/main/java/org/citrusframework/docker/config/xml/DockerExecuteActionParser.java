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

package org.citrusframework.docker.config.xml;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.config.xml.AbstractTestActionFactoryBean;
import org.citrusframework.config.xml.DescriptionElementParser;
import org.citrusframework.docker.actions.DockerExecuteAction;
import org.citrusframework.docker.client.DockerClient;
import org.citrusframework.docker.command.DockerCommand;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.context.ValidationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Bean definition parser for docker client action in test case.
 *
 * @author Christoph Deppisch
 * @since 2.4
 */
public class DockerExecuteActionParser implements BeanDefinitionParser {

    /** Docker command to execute */
    private Class<? extends DockerCommand<?>> commandType;
    private Class<? extends DockerCommand<?>> imageCommandType;
    private Class<? extends DockerCommand<?>> containerCommandType;

    /**
     * Constructor using docker command variations for image and container.
     * @param imageCommandType
     * @param containerCommandType
     */
    public DockerExecuteActionParser(Class<? extends DockerCommand<?>> imageCommandType, Class<? extends DockerCommand<?>> containerCommandType) {
        this.imageCommandType = imageCommandType;
        this.containerCommandType = containerCommandType;
    }

    /**
     * Constructor using docker command.
     * @param commandType
     */
    public DockerExecuteActionParser(Class<? extends DockerCommand<?>> commandType) {
        this.commandType = commandType;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(DockerExecuteActionFactoryBean.class);

        DescriptionElementParser.doParse(element, beanDefinition);
        BeanDefinitionParserUtils.setPropertyReference(beanDefinition, element.getAttribute("docker-client"), "dockerClient");

        DockerCommand command;
        if (commandType != null) {
            command = createCommand(commandType);
        } else {
            if (element.hasAttribute("image") && element.hasAttribute("container")) {
                throw new BeanCreationException("Both docker image and docker container are specified for command - " +
                        "please choose one of docker image or docker container as command target.");
            }

            if (element.hasAttribute("image")) {
                command = createCommand(imageCommandType);
            } else if (element.hasAttribute("container")) {
                command = createCommand(containerCommandType);
            } else {
                throw new BeanCreationException("Missing docker image or docker container name attribute for command");
            }
        }

        for (int i = 0; i < element.getAttributes().getLength(); i++) {
            Node attribute = element.getAttributes().item(i);
            if (!attribute.getNodeName().equals("docker-client")) {
                command.getParameters().put(attribute.getNodeName(), attribute.getNodeValue());
            }
        }

        Element expectCmdResult = DomUtils.getChildElementByTagName(element, "expect");
        if (expectCmdResult != null) {
            beanDefinition.addPropertyValue("expectedCommandResult", DomUtils.getTextValue(DomUtils.getChildElementByTagName(expectCmdResult, "result")));
        }

        beanDefinition.addPropertyValue("command", command);
        return beanDefinition.getBeanDefinition();
    }

    /**
     * Creates new Docker command instance of given type.
     * @param commandType
     * @return
     */
    private DockerCommand<?> createCommand(Class<? extends DockerCommand<?>> commandType) {
        try {
            return commandType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BeanCreationException("Failed to create Docker command of type: " + commandType, e);
        }
    }

    /**
     * Test action factory bean.
     */
    public static class DockerExecuteActionFactoryBean extends AbstractTestActionFactoryBean<DockerExecuteAction, DockerExecuteAction.Builder> {

        private final DockerExecuteAction.Builder builder = new DockerExecuteAction.Builder();

        @Autowired(required = false)
        @Qualifier("dockerClient")
        private DockerClient dockerClient;

        @Autowired(required = false)
        @Qualifier("dockerCommandResultMapper")
        private ObjectMapper jsonMapper;

        @Autowired(required = false)
        @Qualifier("defaultJsonMessageValidator")
        private MessageValidator<? extends ValidationContext> jsonMessageValidator;

        /**
         * Sets docker command to execute.
         * @param command
         * @return
         */
        public void setCommand(DockerCommand<?> command) {
            builder.command(command);
        }

        /**
         * Sets the docker client.
         * @param dockerClient
         */
        public void setDockerClient(DockerClient dockerClient) {
            this.dockerClient = dockerClient;
            builder.client(dockerClient);
        }

        /**
         * Sets the expected command result data.
         * @param expectedCommandResult
         */
        public void setExpectedCommandResult(String expectedCommandResult) {
            builder.result(expectedCommandResult);
        }

        @Override
        public DockerExecuteAction getObject() throws Exception {
            if (dockerClient != null) {
                builder.client(dockerClient);
            }

            if (jsonMessageValidator != null) {
                builder.validator(jsonMessageValidator);
            }

            if (jsonMapper != null) {
                builder.mapper(jsonMapper);
            }

            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return DockerExecuteAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public DockerExecuteAction.Builder getBuilder() {
            return builder;
        }
    }
}
