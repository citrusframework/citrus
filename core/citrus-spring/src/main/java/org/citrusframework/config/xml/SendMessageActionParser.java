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
import java.util.Collections;
import java.util.List;

import org.citrusframework.CitrusSettings;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.variable.VariableExtractor;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for send action in test case.
 *
 * @author Christoph Deppisch
 */
public class SendMessageActionParser extends AbstractMessageActionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String endpointUri = element.getAttribute("endpoint");

        if (!StringUtils.hasText(endpointUri)) {
            throw new BeanCreationException("Endpoint reference must not be empty");
        }

        BeanDefinitionBuilder builder = parseComponent(element, parserContext);
        builder.addPropertyValue("name", element.getLocalName());

        if (endpointUri.contains(":") || (endpointUri.contains(CitrusSettings.VARIABLE_PREFIX) && endpointUri.contains(CitrusSettings.VARIABLE_SUFFIX))) {
            builder.addPropertyValue("endpointUri", endpointUri);
        } else {
            builder.addPropertyReference("endpoint", endpointUri);
        }

        DescriptionElementParser.doParse(element, builder);
        BeanDefinitionParserUtils.setPropertyReference(builder, element.getAttribute("actor"), "actor");
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
     * Parse component returning generic bean definition.
     * @param element
     * @param parserContext
     * @return
     */
    protected BeanDefinitionBuilder parseComponent(Element element, ParserContext parserContext) {
        return BeanDefinitionBuilder.genericBeanDefinition(getBeanDefinitionClass());
    }

    /**
     * Gets the bean definition builder class.
     * @return
     */
    protected Class<? extends AbstractSendMessageActionFactoryBean<?, ?, ?>> getBeanDefinitionClass() {
        return SendMessageActionFactoryBean.class;
    }

    /**
     * Test action factory bean.
     */
    public static class SendMessageActionFactoryBean extends AbstractSendMessageActionFactoryBean<SendMessageAction, SendMessageAction.SendMessageActionBuilderSupport, SendMessageAction.Builder> {

        private final SendMessageAction.Builder builder = new SendMessageAction.Builder();

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
