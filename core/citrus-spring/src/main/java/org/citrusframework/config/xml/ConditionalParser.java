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

package org.citrusframework.config.xml;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.container.ConditionExpression;
import org.citrusframework.container.Conditional;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for selection container in test case.
 *
 * @author Matthias Beil
 * @since 1.2
 */
public class ConditionalParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(final Element element, final ParserContext parserContext) {
        // create new bean builder
        final BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ConditionalFactoryBean.class);

        // see if there is a description
        DescriptionElementParser.doParse(element, builder);

        // set condition, which is mandatory
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("expression"), "condition");

        // get all internal actions
        ActionContainerParser.doParse(element, parserContext, builder);

        // finally return the complete builder with its bean definition
        return builder.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class ConditionalFactoryBean extends AbstractTestContainerFactoryBean<Conditional, Conditional.Builder> {

        private final Conditional.Builder builder = new Conditional.Builder();

        /**
         * Condition which allows execution if true.
         * @param condition
         */
        public void setCondition(final String condition) {
            this.builder.when(condition);
        }

        /**
         * Condition expression allows container execution if evaluates to true.
         * @param conditionExpression
         */
        public void setConditionExpression(ConditionExpression conditionExpression) {
            this.builder.when(conditionExpression);
        }

        @Override
        public Conditional getObject() throws Exception {
            return getObject(builder.build());
        }

        @Override
        public Class<?> getObjectType() {
            return Conditional.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public Conditional.Builder getBuilder() {
            return builder;
        }
    }

}
