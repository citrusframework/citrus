/*
 * Copyright 2006-2018 the original author or authors.
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

import org.citrusframework.TestAction;
import org.citrusframework.container.Async;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for sequential container in test case.
 *
 * @author Christoph Deppisch
 */
public class AsyncParser implements BeanDefinitionParser {

    @Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(AsyncFactoryBean.class);

        DescriptionElementParser.doParse(element, builder);
        builder.addPropertyValue("name", element.getLocalName());

        ActionContainerParser.doParse(DomUtils.getChildElementByTagName(element, "actions"), parserContext, builder);

        Element successActions = DomUtils.getChildElementByTagName(element, "success");
        if (successActions != null) {
            ActionContainerParser.doParse(successActions, parserContext, builder, "successActions");
        }

        Element errorActions = DomUtils.getChildElementByTagName(element, "error");
        if (errorActions != null) {
            ActionContainerParser.doParse(errorActions, parserContext, builder, "errorActions");
        }

        return builder.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class AsyncFactoryBean extends AbstractTestContainerFactoryBean<Async, Async.Builder> {

        private final Async.Builder builder = new Async.Builder();

        private List<TestAction> successActions = new ArrayList<>();
        private List<TestAction> errorActions = new ArrayList<>();

        /**
         * Sets the success test actions.
         * @param successActions
         */
        public void setSuccessActions(List<TestAction> successActions) {
            this.successActions = successActions;
        }

        /**
         * Sets the error test actions.
         * @param errorActions
         */
        public void setErrorActions(List<TestAction> errorActions) {
            this.errorActions = errorActions;
        }

        @Override
        public Async getObject() throws Exception {
            errorActions.forEach(builder::errorAction);
            successActions.forEach(builder::successAction);

            return getObject(builder.build());
        }

        @Override
        public Class<?> getObjectType() {
            return Async.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public Async.Builder getBuilder() {
            return builder;
        }
    }
}
