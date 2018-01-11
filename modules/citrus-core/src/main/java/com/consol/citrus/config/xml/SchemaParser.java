/*
 * Copyright 2006-2013 the original author or authors.
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

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.json.schema.SimpleJsonSchema;
import com.consol.citrus.xml.schema.WsdlXsdSchema;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.w3c.dom.Element;

/**
 * Bean definition parser for schema configuration.
 *
 * @author Martin.Maher@consol.de
 * @since 1.3.1
 */
public class SchemaParser implements BeanDefinitionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String location = element.getAttribute("location");
        BeanDefinitionBuilder builder = null;

        if (location.endsWith(".wsdl")) {
            builder = BeanDefinitionBuilder.genericBeanDefinition(WsdlXsdSchema.class);
            BeanDefinitionParserUtils.setPropertyValue(builder, location, "wsdl");
        } else if (location.endsWith(".xsd")) {
            builder = BeanDefinitionBuilder.genericBeanDefinition(SimpleXsdSchema.class);
            BeanDefinitionParserUtils.setPropertyValue(builder, location, "xsd");
        } else if (location.endsWith(".json")) {
            builder = BeanDefinitionBuilder.genericBeanDefinition(SimpleJsonSchema.class);
            BeanDefinitionParserUtils.setPropertyValue(builder, location, "json");
        }

        if (builder != null) {
            parserContext.getRegistry().registerBeanDefinition(element.getAttribute("id"), builder.getBeanDefinition());
        }

        return null;
    }
}
