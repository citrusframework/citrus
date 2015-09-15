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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for docker client action in test case.
 * 
 * @author Christoph Deppisch
 * @since 2.3.1
 */
public class DockerExecuteActionParser implements BeanDefinitionParser {

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(DockerExecuteAction.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        BeanDefinitionParserUtils.setPropertyReference(beanDefinition, element.getAttribute("docker-client"), "dockerClient");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("image"), "imageId");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("container"), "containerId");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("command"), "command");

        return beanDefinition.getBeanDefinition();
    }
}
