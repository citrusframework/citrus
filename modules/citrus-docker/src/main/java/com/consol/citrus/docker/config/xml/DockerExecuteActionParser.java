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

package com.consol.citrus.docker.config.xml;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.config.xml.DescriptionElementParser;
import com.consol.citrus.docker.actions.DockerExecuteAction;
import com.consol.citrus.docker.command.DockerCommand;
import org.springframework.beans.factory.BeanCreationException;
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
 * @since 2.3.1
 */
public class DockerExecuteActionParser implements BeanDefinitionParser {

    /** Docker command to execute */
    private DockerCommand dockerCommand;
    private DockerCommand imageCommand;
    private DockerCommand containerCommand;

    /**
     * Constructor using docker command variations for image and container.
     * @param imageCommand
     * @param containerCommand
     */
    public DockerExecuteActionParser(DockerCommand imageCommand, DockerCommand containerCommand) {
        this.imageCommand = imageCommand;
        this.containerCommand = containerCommand;
    }

    /**
     * Constructor using docker command.
     * @param dockerCommand
     */
    public DockerExecuteActionParser(DockerCommand dockerCommand) {
        this.dockerCommand = dockerCommand;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(DockerExecuteAction.class);

        DescriptionElementParser.doParse(element, beanDefinition);
        BeanDefinitionParserUtils.setPropertyReference(beanDefinition, element.getAttribute("docker-client"), "dockerClient");

        DockerCommand command;
        if (dockerCommand != null) {
            command = dockerCommand;
        } else {
            if (element.hasAttribute("image") && element.hasAttribute("container")) {
                throw new BeanCreationException("Both docker image and docker container are specified for command - " +
                        "please choose one of docker image or docker container as command target.");
            }

            if (element.hasAttribute("image")) {
                command = imageCommand;
            } else if (element.hasAttribute("container")) {
                command = containerCommand;
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
            command.setExpectedCommandResult(DomUtils.getTextValue(DomUtils.getChildElementByTagName(expectCmdResult, "result")));
        }

        beanDefinition.addPropertyValue("command", command);
        return beanDefinition.getBeanDefinition();
    }
}
