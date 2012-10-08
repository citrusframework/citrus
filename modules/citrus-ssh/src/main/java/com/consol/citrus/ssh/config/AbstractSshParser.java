/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.ssh.config;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Helper class for providing a mapping of XML attribute names to bean properties
 * and other utility stuff common for all config parser.
 *
 * @author Roland Huss
 * @since 1.3
 */
abstract public class AbstractSshParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(getBeanClass());

        // Direct properties
        String[] mapping = getAttributePropertyMapping();
        for (int i = 0;i < mapping.length; i+=2) {
            String value = element.getAttribute(mapping[i]);
            if (StringUtils.hasText(value)) {
                builder.addPropertyValue(mapping[i+1], value);
            }
        }

        // References
        mapping = getAttributePropertyReferenceMapping();
        for (int i = 0;i < mapping.length; i+=2) {
            String value = element.getAttribute(mapping[i]);
            if (StringUtils.hasText(value)) {
                builder.addPropertyReference(mapping[i+1], value);
            }
        }

        // Parse any extra information
        parseExtra(builder,element,parserContext);

        return builder.getBeanDefinition();
    }

    /**
     * Get a mapping from XML attribute names to bean properties name as to add
     * to the bean definition builder.
     *
     * @return attribute property mapping, must never be null. Single array, with odd elements
     * pointing to XML attribute names and even elements are the corresponding
     * property names.
     */
    protected abstract String[] getAttributePropertyMapping();

    /**
     * Return mappings for attrinute to bean reference names, which are used
     * to set a property reference
     * @return mapping for attrubute property reference mapping.
     */
    protected abstract String[] getAttributePropertyReferenceMapping();

    /**
     * Name of the bean class to instantiate.
     *
     * @return class name, must never be null
     */
    protected abstract Class<?> getBeanClass();

    /**
     * Hook for doing extra initializations to the BeanDefinitionBuilder
     *
     * @param pBuilder builder to add values
     * @param pElement the XML element of the parsed config
     * @param pParserContext parser context
     */
    protected void parseExtra(BeanDefinitionBuilder pBuilder, Element pElement, ParserContext pParserContext) {
        // Empty by default
    }
}
