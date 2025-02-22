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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import java.time.Duration;

import static org.citrusframework.config.util.BeanDefinitionParserUtils.setPropertyValue;
import static org.citrusframework.util.StringUtils.hasText;

/**
 * Abstract parser implementation for all iterative container actions. Parser takes care of index name, aborting
 * condition, index start value and description
 */
public abstract class AbstractIterationTestActionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = parseComponent(element, parserContext);

        MessageSelectorParser.doParse(element, builder);

        setPropertyValue(builder, element.getAttribute("condition"), "condition");
        setPropertyValue(builder, element.getAttribute("index"), "indexName");

        if (hasText(element.getAttribute("timeout"))) {
            builder.addPropertyValue("timeout", Duration.parse(element.getAttribute("timeout")));
        }

        builder.addPropertyValue("name", element.getLocalName());

        ActionContainerParser.doParse(element, parserContext, builder);

        return builder.getBeanDefinition();
    }

    protected abstract BeanDefinitionBuilder parseComponent(Element element, ParserContext parserContext);
}
