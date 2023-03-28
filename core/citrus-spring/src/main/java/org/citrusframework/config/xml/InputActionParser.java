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

import org.citrusframework.actions.InputAction;
import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for input action in test case.
 *
 * @author Christoph Deppisch
 */
public class InputActionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(InputActionFactoryBean.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("message"), "message");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("variable"), "variable");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("valid-answers"), "validAnswers");

        return beanDefinition.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class InputActionFactoryBean extends AbstractTestActionFactoryBean<InputAction, InputAction.Builder> {

        private final InputAction.Builder builder = new InputAction.Builder();

        /**
         * Sets the message.
         * @param message the message to set
         */
        public void setMessage(String message) {
            builder.message(message);
        }

        /**
         * Sets the variable.
         * @param variable the variable to set
         */
        public void setVariable(String variable) {
            builder.result(variable);
        }

        /**
         * Sets the valid answers.
         * @param validAnswers the validAnswers to set
         */
        public void setValidAnswers(String validAnswers) {
            builder.answers(validAnswers);
        }

        @Override
        public InputAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return InputAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public InputAction.Builder getBuilder() {
            return builder;
        }
    }
}
