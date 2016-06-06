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

package com.consol.citrus.cucumber.config.xml;

import com.consol.citrus.config.xml.ActionContainerParser;
import com.consol.citrus.config.xml.DescriptionElementParser;
import com.consol.citrus.cucumber.container.StepTemplate;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.util.regex.Pattern;

/**
 * Parser configures a step template with pattern and parameter names.
 * @author Christoph Deppisch
 * @since 2.6
 */
public class StepTemplateParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(StepTemplate.class);

        DescriptionElementParser.doParse(element, builder);

        if (element.hasAttribute("given")) {
            builder.addPropertyValue("name", element.getLocalName() + "(given)");
            builder.addPropertyValue("pattern", Pattern.compile(element.getAttribute("given")));
        } else if (element.hasAttribute("when")) {
            builder.addPropertyValue("name", element.getLocalName() + "(when)");
            builder.addPropertyValue("pattern", Pattern.compile(element.getAttribute("when")));
        } else if (element.hasAttribute("then")) {
            builder.addPropertyValue("name", element.getLocalName() + "(then)");
            builder.addPropertyValue("pattern", Pattern.compile(element.getAttribute("then")));
        }

        if (element.hasAttribute("parameter-names")) {
            builder.addPropertyValue("parameterNames", StringUtils.commaDelimitedListToStringArray(element.getAttribute("parameter-names")));
        }

        String globalContext = element.getAttribute("global-context");
        if (StringUtils.hasText(globalContext)) {
            builder.addPropertyValue("globalContext", globalContext);
        }

        ActionContainerParser.doParse(element, parserContext, builder);

        String beanName = parserContext.getReaderContext().generateBeanName(builder.getBeanDefinition());
        parserContext.getRegistry().registerBeanDefinition(beanName, builder.getBeanDefinition());
        return parserContext.getRegistry().getBeanDefinition(beanName);
    }
}
