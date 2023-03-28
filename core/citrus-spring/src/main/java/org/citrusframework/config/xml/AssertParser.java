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

import org.citrusframework.TestAction;
import org.citrusframework.config.CitrusNamespaceParserRegistry;
import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.container.Assert;
import org.apache.xerces.util.DOMUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for assert action in test case.
 *
 * @author Christoph Deppisch
 */
public class AssertParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(AssertFactoryBean.class);

        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("exception"), "exception");
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("message"), "message");

        DescriptionElementParser.doParse(element, builder);

        Element action = DOMUtil.getFirstChildElement(DomUtils.getChildElementByTagName(element, "when"));
        if (action != null) {
            BeanDefinitionParser parser = null;
            if (action.getNamespaceURI().equals(element.getNamespaceURI())) {
                parser = CitrusNamespaceParserRegistry.getBeanParser(action.getLocalName());
            }

            if (parser == null) {
                builder.addPropertyValue("action", parserContext.getReaderContext().getNamespaceHandlerResolver().resolve(action.getNamespaceURI()).parse(action, parserContext));
            } else {
                builder.addPropertyValue("action", parser.parse(action, parserContext));
            }
        }

        builder.addPropertyValue("name", element.getLocalName());

        return builder.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class AssertFactoryBean extends AbstractTestContainerFactoryBean<Assert, Assert.Builder> {

        private final Assert.Builder builder = new Assert.Builder();

        /**
         * Sets the test action.
         * @param action
         */
        public void setAction(TestAction action) {
            builder.action(action);
        }

        /**
         * Set the message to send.
         * @param message the message to set
         */
        public void setMessage(String message) {
            this.builder.message(message);
        }

        /**
         * Sets the exception.
         * @param exception the exception to set
         */
        public void setException(Class<? extends Throwable> exception) {
            this.builder.exception(exception);
        }

        @Override
        public Assert getObject() throws Exception {
            return getObject(builder.build());
        }

        @Override
        public Class<?> getObjectType() {
            return Assert.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public Assert.Builder getBuilder() {
            return builder;
        }
    }
}
