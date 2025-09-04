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

import java.util.List;

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.config.xml.AbstractReceiveMessageActionFactoryBean;
import org.citrusframework.exceptions.CitrusRuntimeException;
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
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.context.ValidationContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.http.HttpStatusCode;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static org.citrusframework.openapi.validation.OpenApiMessageValidationContext.Builder.openApi;
import static org.citrusframework.util.StringUtils.hasText;
import static org.citrusframework.util.StringUtils.isNotEmpty;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * Parses XML configuration for receiving API responses based on OpenAPI specifications. Extends
 * {@link HttpReceiveResponseActionParser} to handle OpenAPI-specific response builders and
 * validation.
 */
public class RestApiReceiveMessageActionParser extends HttpReceiveResponseActionParser {

    public static final String STATUS_CODE = "responseCode";
    public static final String SCHEMA_VALIDATION = "schema-validation";

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
        BeanDefinitionBuilder beanDefinitionBuilder = super.createBeanDefinitionBuilder(element,
            parserContext);

        // Remove the messageBuilder property and inject it directly into the action builder.
        BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        OpenApiClientResponseMessageBuilder messageBuilder = (OpenApiClientResponseMessageBuilder) beanDefinition.getPropertyValues()
            .get("messageBuilder");

        String statusCodeString = element.getAttribute(STATUS_CODE);
        messageBuilder.statusCode(statusCodeString);

        if (StringUtils.isNotEmpty(statusCodeString) && messageBuilder.getMessage() != null) {
            try {
                HttpStatusCode httpStatusCode = HttpStatusCode.valueOf(
                    parseInt(statusCodeString));
                messageBuilder.getMessage().status(httpStatusCode);
            } catch (Exception e) {
                // Ignore
            }
        }

        beanDefinition.getPropertyValues().removePropertyValue("messageBuilder");

        /*
         * Allows overriding schema validation at the action level.
         * For simplicity, this setting takes precedence over any existing
         * message-level validation settings.
         */
        String actionLevelSchemaValidation = element.getAttribute(SCHEMA_VALIDATION);
        if (isNotEmpty(actionLevelSchemaValidation)) {
            boolean isSchemaValidation = parseBoolean(actionLevelSchemaValidation);
            beanDefinitionBuilder.addPropertyValue("schemaValidation", isSchemaValidation);
            var validationContextBuilder = (List) beanDefinition.getPropertyValues()
                .get("validationContextBuilder");
            if (validationContextBuilder != null) {
                OpenApiMessageValidationContext.Builder openApiValidationContextBuilder = (OpenApiMessageValidationContext.Builder) validationContextBuilder.stream()
                    .filter(builder -> builder instanceof OpenApiMessageValidationContext.Builder)
                    .findFirst().orElse(null);
                openApiValidationContextBuilder.schemaValidation(isSchemaValidation);
            } else {
                throw new CitrusRuntimeException("Unexpectedly did not find OpenApiMessageValidationContextBuilder in bean properties!");
            }
        }

        BeanDefinitionBuilder actionBuilder = genericBeanDefinition(beanClass);
        actionBuilder.addConstructorArgValue(new RuntimeBeanReference(apiBeanClass));
        actionBuilder.addConstructorArgValue(messageBuilder);

        beanDefinitionBuilder.addConstructorArgValue(actionBuilder.getBeanDefinition());
        setDefaultEndpoint(beanDefinitionBuilder);

        // By default, the type is xml. This not a common case in rest, which is why we switch to json here,
        // if no explicit type is specified.
        Attr type = element.getAttributeNode("type");
        if (type == null) {
            beanDefinitionBuilder.addPropertyValue("messageType", "json");
        }

        return beanDefinitionBuilder;
    }

    /**
     * Sets the default endpoint for the message if not already specified.
     */
    protected void setDefaultEndpoint(BeanDefinitionBuilder beanDefinitionBuilder) {
        if (!beanDefinitionBuilder.getBeanDefinition().getPropertyValues().contains("endpoint")
            && !beanDefinitionBuilder.getBeanDefinition().getPropertyValues()
            .contains("endpointUri")) {
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
    protected List<ValidationContext.Builder<?, ?>> parseValidationContexts(Element messageElement,
        BeanDefinitionBuilder builder) {
        List<ValidationContext.Builder<?, ?>> validationContextBuilders = super.parseValidationContexts(
            messageElement, builder);
        OpenApiMessageValidationContext.Builder openApiMessageValidationContextBuilder = getOpenApiMessageValidationContext(
            messageElement);
        validationContextBuilders.add(openApiMessageValidationContextBuilder);
        return validationContextBuilders;
    }

    /**
     * Constructs the OpenAPI message validation context based on the XML element.
     */
    private OpenApiMessageValidationContext.Builder getOpenApiMessageValidationContext(
        Element messageElement) {
        OpenApiMessageValidationContext.Builder validationContextBuilder = openApi(
            openApiSpecification);

        if (messageElement != null) {
            Element parentElement = (Element) messageElement.getParentNode();
            String actionSchemaValidation = parentElement.getAttribute(SCHEMA_VALIDATION);
            if (hasText(actionSchemaValidation)) {
                validationContextBuilder.schemaValidation(
                    parseBoolean(actionSchemaValidation));
            }

            String schema = messageElement.getAttribute("schema");
            if (hasText(schema)) {
                validationContextBuilder.schema(schema);
            }

            String schemaRepository = messageElement.getAttribute("schema-repository");
            if (hasText(schemaRepository)) {
                validationContextBuilder.schema(schemaRepository);
            }

            String schemaValidation = messageElement.getAttribute(SCHEMA_VALIDATION);
            if (hasText(schemaValidation)) {
                validationContextBuilder.schemaValidation(parseBoolean(schemaValidation));
            }
        }

        return validationContextBuilder;
    }

    /**
     * Factory bean for creating {@link ReceiveMessageAction} instances using the provided
     * {@link RestApiReceiveMessageActionBuilder}.
     */
    public static class TestApiOpenApiClientReceiveActionBuilderFactoryBean extends
        AbstractReceiveMessageActionFactoryBean<ReceiveMessageAction, HttpMessageBuilderSupport, HttpClientResponseActionBuilder> {

        private RestApiReceiveMessageActionBuilder builder;

        public TestApiOpenApiClientReceiveActionBuilderFactoryBean(
            RestApiReceiveMessageActionBuilder builder) {
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

        public void setSchemaValidation(final boolean enabled) {
            this.builder.schemaValidation(enabled);
        }
    }
}
