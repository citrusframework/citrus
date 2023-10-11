/*
 * Copyright 2006-2011 the original author or authors.
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

package org.citrusframework.config.util;

import org.citrusframework.util.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.ParserContext;

/**
 * Provides shared utility methods for bean definition parsing.
 *
 * @author Christoph Deppisch
 */
public abstract class BeanDefinitionParserUtils {

    /**
     * Prevent instantiation.
     */
    private BeanDefinitionParserUtils() {
    }

    /**
     * Sets the property value on bean definition in case value
     * is set properly.
     *
     * @param builder the bean definition builder to be configured
     * @param propertyValue the property value
     * @param propertyName the name of the property
     */
    public static void setPropertyValue(BeanDefinitionBuilder builder, String propertyValue, String propertyName) {
        if (StringUtils.hasText(propertyValue)) {
            builder.addPropertyValue(propertyName, propertyValue);
        }
    }

    /**
     * Sets the property value on bean definition as constructor argument in case value
     * is not null.
     *
     * @param builder the bean definition to be configured
     * @param propertyValue the property value
     */
    public static void setConstructorArgValue(BeanDefinitionBuilder builder, String propertyValue) {
        if (StringUtils.hasText(propertyValue)) {
            builder.addConstructorArgValue(propertyValue);
        }
    }

    /**
     * Sets the property reference on bean definition in case reference
     * is set properly.
     *
     * @param builder the bean definition builder to be configured
     * @param beanReference bean reference to populate the property
     * @param propertyName the name of the property
     */
    public static void setPropertyReference(BeanDefinitionBuilder builder, String beanReference, String propertyName) {
        if (StringUtils.hasText(beanReference)) {
            builder.addPropertyReference(propertyName, beanReference);
        }
    }

    /**
     * Sets the property reference on bean definition in case reference
     * is set properly.
     *
     * @param builder the bean definition builder to be configured
     * @param beanReference bean reference to add as constructor arg
     */
    public static void addConstructorArgReference(BeanDefinitionBuilder builder, String beanReference) {
        if (StringUtils.hasText(beanReference)) {
            builder.addConstructorArgReference(beanReference);
        }
    }

    /**
     * Sets the property reference on bean definition. In case reference is not available a default value is set.
     *
     * @param builder
     * @param beanReference
     * @param propertyName
     * @param defaultValue
     */
    public static void setPropertyReference(BeanDefinitionBuilder builder, String beanReference, String propertyName, String defaultValue) {
        if (StringUtils.hasText(beanReference)) {
            builder.addPropertyReference(propertyName, beanReference);
        } else {
            builder.addPropertyReference(propertyName, defaultValue);
        }
    }

    /**
     * Creates new bean definition from bean class and registers new bean in parser registry.
     * Returns bean definition holder.
     * @param beanId
     * @param beanClass
     * @param parserContext
     * @param shouldFireEvents
     */
    public static BeanDefinitionHolder registerBean(String beanId, Class<?> beanClass, ParserContext parserContext, boolean shouldFireEvents) {
        return registerBean(beanId, BeanDefinitionBuilder.genericBeanDefinition(beanClass).getBeanDefinition(), parserContext, shouldFireEvents);
    }

    /**
     * Registers bean definition in parser registry and returns bean definition holder.
     * @param beanId
     * @param beanDefinition
     * @param parserContext
     * @param shouldFireEvents
     * @return
     */
    public static BeanDefinitionHolder registerBean(String beanId, BeanDefinition beanDefinition, ParserContext parserContext, boolean shouldFireEvents) {
        if (parserContext.getRegistry().containsBeanDefinition(beanId)) {
            return new BeanDefinitionHolder(parserContext.getRegistry().getBeanDefinition(beanId), beanId);
        }

        BeanDefinitionHolder configurationHolder = new BeanDefinitionHolder(beanDefinition, beanId);
        BeanDefinitionReaderUtils.registerBeanDefinition(configurationHolder, parserContext.getRegistry());

        if (shouldFireEvents) {
            BeanComponentDefinition componentDefinition = new BeanComponentDefinition(configurationHolder);
            parserContext.registerComponent(componentDefinition);
        }

        return configurationHolder;
    }
}
