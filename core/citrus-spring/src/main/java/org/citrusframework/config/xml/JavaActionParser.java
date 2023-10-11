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

package org.citrusframework.config.xml;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.actions.JavaAction;
import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.util.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for java action in test case.
 *
 * @author Christoph Deppisch
 */
public class JavaActionParser implements BeanDefinitionParser {

    @Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(JavaActionFactoryBean.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("class"), "className");
        BeanDefinitionParserUtils.setPropertyReference(beanDefinition, element.getAttribute("ref"), "instance");

        Element constructorElement = DomUtils.getChildElementByTagName(element, "constructor");
        List<Object> arguments = new ArrayList<Object>();
        if (constructorElement != null) {
            List<Element> argumentList = DomUtils.getChildElementsByTagName(constructorElement, "argument");
            for (Element arg : argumentList) {
                arguments.add(resolveArgument(arg.getAttribute("type"), arg.getTextContent()));
            }
            beanDefinition.addPropertyValue("constructorArgs", arguments);
        }

        Element methodElement = DomUtils.getChildElementByTagName(element, "method");
        arguments = new ArrayList<Object>();
        if (methodElement != null) {
            String methodName = methodElement.getAttribute("name");
            beanDefinition.addPropertyValue("methodName", methodName);

            List<Element> argumentList = DomUtils.getChildElementsByTagName(methodElement, "argument");
            for (Element arg : argumentList) {
                arguments.add(resolveArgument(arg.getAttribute("type"), DomUtils.getTextValue(arg)));
            }
            beanDefinition.addPropertyValue("methodArgs", arguments);
        }

        return beanDefinition.getBeanDefinition();
    }

    private Object resolveArgument(String type, String value) {
        if (!StringUtils.hasText(type) || type.equals("String")) {
            return value;
        } else if (type.equals("String[]")) {
            return value.split(",");
        } else if (type.equals("boolean")) {
            return Boolean.valueOf(value).booleanValue();
        }  else if (type.equals("int")) {
            return Integer.valueOf(value).intValue();
        } else if (type.equals("long")) {
            return Long.valueOf(value);
        } else if (type.equals("double")) {
            return Double.valueOf(value);
        }

        throw new BeanCreationException("Found unsupported method argument type: '" + type + "'");
    }

    /**
     * Test action factory bean.
     */
    public static class JavaActionFactoryBean extends AbstractTestActionFactoryBean<JavaAction, JavaAction.Builder> {

        private final JavaAction.Builder builder = new JavaAction.Builder();

        /**
         * Setter for class name
         * @param className
         */
        public void setClassName(String className) {
            builder.className(className);
        }

        /**
         * Setter for constructor args
         * @param constructorArgs
         */
        public void setConstructorArgs(List<Object> constructorArgs) {
            builder.constructorArgs(constructorArgs);
        }

        /**
         * Setter for method args
         * @param methodArgs
         */
        public void setMethodArgs(List<Object> methodArgs) {
            builder.methodArgs(methodArgs);
        }

        /**
         * Setter for method name
         * @param methodName
         */
        public void setMethodName(String methodName) {
            builder.method(methodName);
        }

        /**
         * Setter for object instance
         * @param instance
         */
        public void setInstance(Object instance) {
            builder.instance(instance);
        }

        @Override
        public JavaAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return JavaAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public JavaAction.Builder getBuilder() {
            return builder;
        }
    }

}
