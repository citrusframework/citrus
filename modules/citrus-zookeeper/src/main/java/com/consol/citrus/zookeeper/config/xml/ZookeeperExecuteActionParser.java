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
import com.consol.citrus.config.xml.DescriptionElementParser;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.zookeeper.actions.ZookeeperExecuteAction;
import com.consol.citrus.zookeeper.command.ZookeeperCommand;
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
public class ZookeeperExecuteActionParser implements BeanDefinitionParser {

    /** ZooKeeper command to execute */
    private Class<? extends ZookeeperCommand> zookeeperCommandClass;

    /**
     * Constructor using zookeeper command.
     * @param commandClass
     */
    public <T extends ZookeeperCommand> ZookeeperExecuteActionParser(Class<T> commandClass) {
        this.zookeeperCommandClass = commandClass;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        ZookeeperCommand command = null;
        try {
            // TODO MM factory better?
            command = zookeeperCommandClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new CitrusRuntimeException(e);
        }

        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(ZookeeperExecuteAction.class);

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

        // TODO MM add variable extractor ...
        // com.consol.citrus.variable.VariableExtractor
        // com.consol.citrus.actions.ReceiveMessageAction#validateMessage
        // com.consol.citrus.config.xml.ReceiveMessageActionParser#getVariableExtractors

        // TODO MM add Java-DSL
        // TODO MM documentation

        beanDefinition.addPropertyValue("command", command);
        return beanDefinition.getBeanDefinition();
    }
}
