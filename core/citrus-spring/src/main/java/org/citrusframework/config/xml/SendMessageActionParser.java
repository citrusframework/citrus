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

import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.message.MessageBuilder;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.variable.VariableExtractor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bean definition parser for send action in test case.
 *
 */
public class SendMessageActionParser extends AbstractMessageActionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = getBeanDefinitionBuilder(element, parserContext);
        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("fork"), "forkMode");

        Element messageElement = DomUtils.getChildElementByTagName(element, "message");
        if (messageElement != null) {
            String messageType = messageElement.getAttribute("type");
            if (StringUtils.hasText(messageType)) {
                builder.addPropertyValue("messageType", messageType);
            }

            String dataDictionary = messageElement.getAttribute("data-dictionary");
            if (StringUtils.hasText(dataDictionary)) {
                builder.addPropertyReference("dataDictionary", dataDictionary);
            }

            String schemaValidation = messageElement.getAttribute("schema-validation");
            if (StringUtils.hasText(schemaValidation)) {
                builder.addPropertyValue("schemaValidation", Boolean.valueOf(schemaValidation));
            }

            String schema = messageElement.getAttribute("schema");
            if (StringUtils.hasText(schema)) {
                builder.addPropertyValue("schemaValidation", Boolean.valueOf(schemaValidation));
                builder.addPropertyValue("schema", schema);
            }

            String schemaRepository = messageElement.getAttribute("schema-repository");
            if (StringUtils.hasText(schemaRepository)) {
                builder.addPropertyValue("schemaValidation", Boolean.valueOf(schemaValidation));
                builder.addPropertyValue("schemaRepository", schemaRepository);
            }

        }

        DefaultMessageBuilder messageBuilder = constructMessageBuilder(messageElement, builder);
        parseHeaderElements(element, messageBuilder, Collections.emptyList());

        if (messageBuilder != null) {
            builder.addPropertyValue("messageBuilder", messageBuilder);
        }

        List<VariableExtractor> variableExtractors = new ArrayList<>();
        parseExtractHeaderElements(element, variableExtractors);

        if (!variableExtractors.isEmpty()) {
            builder.addPropertyValue("variableExtractors", variableExtractors);
        }

        return builder.getBeanDefinition();
    }

    /**
     * Gets the bean definition builder class.
     */
    @Override
    protected Class<? extends AbstractSendMessageActionFactoryBean<?, ?, ?>> getMessageFactoryClass() {
        return SendMessageActionFactoryBean.class;
    }

    /**
     * Test action factory bean.
     */
    public static class SendMessageActionFactoryBean extends AbstractSendMessageActionFactoryBean<SendMessageAction, SendMessageAction.SendMessageActionBuilderSupport, SendMessageAction.Builder> {

        private final SendMessageAction.Builder builder;


        public SendMessageActionFactoryBean() {
            builder = new SendMessageAction.Builder();
        }

        public SendMessageActionFactoryBean(MessageBuilder messageBuilder) {
            builder = new SendMessageAction.Builder();
            builder.message(messageBuilder);
        }

        @Override
        public SendMessageAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return SendMessageAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public SendMessageAction.Builder getBuilder() {
            return builder;
        }
    }
}
