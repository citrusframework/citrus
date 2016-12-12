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

package com.consol.citrus.kubernetes.config.xml;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.config.xml.DescriptionElementParser;
import com.consol.citrus.kubernetes.actions.KubernetesExecuteAction;
import com.consol.citrus.kubernetes.command.KubernetesCommand;
import org.springframework.beans.factory.BeanCreationException;
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
public class KubernetesExecuteActionParser implements BeanDefinitionParser {

    /** Kubernetes command to execute */
    private Class<? extends KubernetesCommand> commandType;

    /**
     * Constructor using kubernetes command.
     * @param commandType
     */
    public KubernetesExecuteActionParser(Class<? extends KubernetesCommand> commandType) {
        this.commandType = commandType;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(KubernetesExecuteAction.class);

        DescriptionElementParser.doParse(element, beanDefinition);
        BeanDefinitionParserUtils.setPropertyReference(beanDefinition, element.getAttribute("client"), "kubernetesClient");

        KubernetesCommand command = createCommand(commandType);
        for (int i = 0; i < element.getAttributes().getLength(); i++) {
            Node attribute = element.getAttributes().item(i);
            if (!attribute.getNodeName().equals("client")) {
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
     * Creates new Kubernetes command instance of given type.
     * @param commandType
     * @return
     */
    private KubernetesCommand createCommand(Class<? extends KubernetesCommand> commandType) {
        try {
            return commandType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BeanCreationException("Failed to create Kubernetes command of type: " + commandType, e);
        }
    }
}
