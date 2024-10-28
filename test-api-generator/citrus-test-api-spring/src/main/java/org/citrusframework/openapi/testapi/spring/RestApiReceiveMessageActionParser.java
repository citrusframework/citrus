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

package org.citrusframework.openapi.testapi.spring;

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.config.xml.AbstractReceiveMessageActionFactoryBean;
import org.citrusframework.http.actions.HttpClientResponseActionBuilder;
import org.citrusframework.http.actions.HttpClientResponseActionBuilder.HttpMessageBuilderSupport;
import org.citrusframework.http.config.xml.HttpReceiveResponseActionParser;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageBuilder;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.actions.OpenApiClientResponseActionBuilder.OpenApiClientResponseMessageBuilder;
import org.citrusframework.openapi.actions.OpenApiSpecificationSource;
import org.citrusframework.openapi.testapi.GeneratedApi;
import org.citrusframework.openapi.testapi.RestApiReceiveMessageActionBuilder;
import org.citrusframework.openapi.validation.OpenApiMessageValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import java.util.List;

import static org.citrusframework.openapi.validation.OpenApiMessageValidationContext.Builder.openApi;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * Parses XML configuration for receiving API responses based on OpenAPI specifications. Extends
 * {@link HttpReceiveResponseActionParser} to handle OpenAPI-specific response builders and
 * validation.
 */
public class RestApiReceiveMessageActionParser extends HttpReceiveResponseActionParser {

    /**
     * The generated api bean class.
     */
    private final Class<? extends GeneratedApi> apiBeanClass;

    /**
     * The builder class for the receive message action.
     */
    private final Class<? extends RestApiReceiveMessageActionBuilder> beanClass;

    /**
     * The OpenAPI specification related to this parser.
     */
    private final OpenApiSpecification openApiSpecification;

    /**
     * The OpenAPI operationId associated with this parser.
     */
    private final String operationId;

    private final String defaultApiEndpointName;

    public RestApiReceiveMessageActionParser(OpenApiSpecification openApiSpecification,
                                             String operationId,
                                             Class<? extends GeneratedApi> apiBeanClass,
                                             Class<? extends RestApiReceiveMessageActionBuilder> beanClass,
                                             String defaultApiEndpointName) {
        this.openApiSpecification = openApiSpecification;
        this.operationId = operationId;
        this.apiBeanClass = apiBeanClass;
        this.beanClass = beanClass;
        this.defaultApiEndpointName = defaultApiEndpointName;
    }

    @Override
    protected BeanDefinitionBuilder createBeanDefinitionBuilder(Element element,
                                                                ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinitionBuilder = super.createBeanDefinitionBuilder(element, parserContext);

        // Remove the messageBuilder property and inject it directly into the action builder.
        BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        OpenApiClientResponseMessageBuilder messageBuilder = (OpenApiClientResponseMessageBuilder) beanDefinition.getPropertyValues()
                .get("messageBuilder");
        messageBuilder.statusCode(element.getAttribute("statusCode"));

        beanDefinition.getPropertyValues().removePropertyValue("messageBuilder");

        BeanDefinitionBuilder actionBuilder = genericBeanDefinition(beanClass);
        actionBuilder.addConstructorArgValue(new RuntimeBeanReference(apiBeanClass));
        actionBuilder.addConstructorArgValue(openApiSpecification);
        actionBuilder.addConstructorArgValue(messageBuilder);

        beanDefinitionBuilder.addConstructorArgValue(actionBuilder.getBeanDefinition());
        setDefaultEndpoint(beanDefinitionBuilder);

        return beanDefinitionBuilder;
    }

    /**
     * Sets the default endpoint for the message if not already specified.
     */
    private void setDefaultEndpoint(BeanDefinitionBuilder beanDefinitionBuilder) {
        if (!beanDefinitionBuilder.getBeanDefinition().getPropertyValues().contains("endpoint")) {
            beanDefinitionBuilder.addPropertyReference("endpoint", defaultApiEndpointName);
        }
    }

    @Override
    protected Class<? extends AbstractReceiveMessageActionFactoryBean<?, ?, ?>> getMessageFactoryClass() {
        return TestApiOpenApiClientReceiveActionBuilderFactoryBean.class;
    }

    @Override
    protected void validateEndpointConfiguration(Element element) {
        // skip validation, as we support endpoint injection
    }

    @Override
    protected HttpMessageBuilder createMessageBuilder(HttpMessage httpMessage) {
        return new OpenApiClientResponseMessageBuilder(httpMessage,
                new OpenApiSpecificationSource(openApiSpecification), operationId, null);
    }

    @Override
    protected List<ValidationContext> parseValidationContexts(Element messageElement,
                                                              BeanDefinitionBuilder builder) {
        List<ValidationContext> validationContexts = super.parseValidationContexts(messageElement, builder);
        OpenApiMessageValidationContext openApiMessageValidationContext = getOpenApiMessageValidationContext(messageElement);
        validationContexts.add(openApiMessageValidationContext);
        return validationContexts;
    }

    /**
     * Constructs the OpenAPI message validation context based on the XML element.
     */
    private OpenApiMessageValidationContext getOpenApiMessageValidationContext(Element messageElement) {
        OpenApiMessageValidationContext.Builder context = openApi(openApiSpecification);

        if (messageElement != null) {
            addSchemaInformationToValidationContext(messageElement, context);
        }

        return context.build();
    }

    /**
     * Factory bean for creating {@link ReceiveMessageAction} instances using the provided
     * {@link RestApiReceiveMessageActionBuilder}.
     */
    public static class TestApiOpenApiClientReceiveActionBuilderFactoryBean extends
            AbstractReceiveMessageActionFactoryBean<ReceiveMessageAction, HttpMessageBuilderSupport, HttpClientResponseActionBuilder> {

        private RestApiReceiveMessageActionBuilder builder;

        public TestApiOpenApiClientReceiveActionBuilderFactoryBean(RestApiReceiveMessageActionBuilder builder) {
            this.builder = builder;
        }

        @Override
        public ReceiveMessageAction getObject() {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return SendMessageAction.class;
        }

        @Override
        public HttpClientResponseActionBuilder getBuilder() {
            return builder;
        }

        public void setBuilder(RestApiReceiveMessageActionBuilder builder) {
            this.builder = builder;
        }
    }
}
