/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.config.xml;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.consol.citrus.container.Iterate;

/**
 * Bean definition parser for assert action in test case.
 * 
 * @author Christoph Deppisch
 */
public class IterateParser extends AbstractIterationTestActionParser {

    /**
     * @see com.consol.citrus.config.xml.AbstractIterationTestActionParser#parseComponent(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    @Override
	public BeanDefinitionBuilder parseComponent(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(Iterate.class);
        
        String start = element.getAttribute("start");
        if (StringUtils.hasText(start)) {
            builder.addPropertyValue("start", Integer.valueOf(start));
        }
        
        String step = element.getAttribute("step");
        if (StringUtils.hasText(step)) {
            builder.addPropertyValue("step", Integer.valueOf(step));
        }

        return builder;
    }
}
