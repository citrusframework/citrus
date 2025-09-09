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

package org.citrusframework.openapi.testapi;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.Cookie;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.actions.HttpClientRequestActionBuilder;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.openapi.AutoFillType;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.actions.OpenApiClientRequestActionBuilder;
import org.citrusframework.openapi.actions.OpenApiSpecificationSource;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources.ClasspathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static java.lang.String.format;
import static java.net.URLEncoder.encode;
import static java.util.Objects.isNull;
import static org.citrusframework.openapi.testapi.OpenApiParameterFormatter.formatAccordingToStyle;
import static org.citrusframework.openapi.testapi.ParameterStyle.NONE;
import static org.citrusframework.openapi.util.OpenApiUtils.createFullPathOperationIdentifier;
import static org.citrusframework.util.FileUtils.getDefaultCharset;
import static org.citrusframework.util.StringUtils.isEmpty;

public class RestApiSendMessageActionBuilder extends OpenApiClientRequestActionBuilder {

    private final GeneratedApi generatedApi;

    private final List<ApiActionBuilderCustomizer> customizers;

    public RestApiSendMessageActionBuilder(GeneratedApi generatedApi,
        OpenApiSpecification openApiSpec,
        String method,
        String path,
        String operationName) {
        this(generatedApi, openApiSpec, new HttpMessage(), method, path, operationName);
    }

    public RestApiSendMessageActionBuilder(GeneratedApi generatedApi,
        OpenApiSpecification openApiSpec,
        HttpMessage httpMessage, String method,
        String path, String operationName) {
        this(generatedApi, openApiSpec,
            new TestApiClientRequestMessageBuilder(httpMessage,
                new OpenApiSpecificationSource(openApiSpec),
                createFullPathOperationIdentifier(method, path)),
            httpMessage,
            method,
            path,
            operationName);
    }

    public RestApiSendMessageActionBuilder(GeneratedApi generatedApi,
        OpenApiSpecification openApiSpec,
        TestApiClientRequestMessageBuilder messageBuilder,
        HttpMessage httpMessage,
        String method,
        String path,
        String operationName) {
        super(new OpenApiSpecificationSource(openApiSpec), messageBuilder, httpMessage,
            createFullPathOperationIdentifier(method, path));

        this.generatedApi = generatedApi;
        this.customizers = generatedApi.getCustomizers();

        name(format("send-%s:%s", generatedApi.getClass().getSimpleName().toLowerCase(),
            operationName));

        getMessageBuilderSupport().header("citrus_open_api_operation_name", operationName);
        getMessageBuilderSupport().header("citrus_open_api_method", method);
        getMessageBuilderSupport().header("citrus_open_api_path", path);
    }

    public GeneratedApi getGeneratedApi() {
        return generatedApi;
    }

    public List<ApiActionBuilderCustomizer> getCustomizers() {
        return customizers;
    }

    @Override
    public final HttpClientRequestActionBuilder name(String name) {
        return super.name(name);
    }

    protected void pathParameter(String name, Object value, ParameterStyle parameterStyle,
        boolean explode, boolean isObject) {

        if (isNull(value)) {
            return;
        }

        ((TestApiClientRequestMessageBuilder) getMessageBuilderSupport().getMessageBuilder()).pathParameter(
            name, value, parameterStyle, explode, isObject);
    }

    protected void formParameter(String name, Object value) {

        if (isNull(value)) {
            return;
        }

        setFormParameter(name, value);
    }

    protected void setFormParameter(String name, Object value) {

        if (isNull(value)) {
            return;
        }

        ((TestApiClientRequestMessageBuilder) getMessageBuilderSupport().getMessageBuilder()).formParameter(
            name, value);
    }

    protected void queryParameter(final String name, Object value) {

        if (isNull(value)) {
            return;
        }

        ((TestApiClientRequestMessageBuilder) getMessageBuilderSupport().getMessageBuilder()).queryParameter(
            name, value);
    }

    protected void queryParameter(final String name, Object value, ParameterStyle parameterStyle,
        boolean explode, boolean isObject) {

        if (isNull(value)) {
            return;
        }

        ((TestApiClientRequestMessageBuilder) getMessageBuilderSupport().getMessageBuilder()).queryParameter(
            name, value, parameterStyle, explode, isObject);
    }

    protected void headerParameter(String name, Object value) {

        if (isNull(value)) {
            return;
        }

        ((TestApiClientRequestMessageBuilder) getMessageBuilderSupport().getMessageBuilder()).headerParameter(
            name, value, NONE, false, false);
    }

    protected void headerParameter(String name, Object value, ParameterStyle parameterStyle,
        boolean explode, boolean isObject) {

        if (isNull(value)) {
            return;
        }

        ((TestApiClientRequestMessageBuilder) getMessageBuilderSupport().getMessageBuilder()).headerParameter(
            name, value, parameterStyle, explode, isObject);

    }

    protected void cookieParameter(String name, Object value) {

        if (isNull(value)) {
            return;
        }

        ((TestApiClientRequestMessageBuilder) getMessageBuilderSupport().getMessageBuilder()).cookieParameter(
            name, value, NONE, false, false);

    }

    protected void cookieParameter(String name, Object value, ParameterStyle parameterStyle,
        boolean explode, boolean isObject) {

        if (isNull(value)) {
            return;
        }

        ((TestApiClientRequestMessageBuilder) getMessageBuilderSupport().getMessageBuilder()).cookieParameter(
            name, value, parameterStyle, explode, isObject);

    }

    @Override
    public SendMessageAction doBuild() {

        // If no endpoint was set explicitly, use the default endpoint given by api
        if (getEndpoint() == null && getEndpointUri() == null) {
            if (generatedApi.getEndpoint() == null) {
                throw new CitrusRuntimeException("No endpoint specified for action!");
            }
            endpoint(generatedApi.getEndpoint());
        }

        return super.doBuild();
    }

    protected Object toBinary(Object object) {
        if (object instanceof byte[] bytes) {
            return bytes;
        } else if (object instanceof Resource resource) {
            return new ClasspathResource(resource.getLocation());
        } else if (object instanceof org.springframework.core.io.Resource resource) {
            return resource;
        } else if (object instanceof String location) {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            return resolver.getResource(location);
        }

        throw new IllegalArgumentException(
            "Cannot convert object to binary. Only byte[], Resource, and String are supported: "
                + object.getClass());
    }

    protected String getOrDefault(String value, String defaultValue, boolean base64Encode) {
        if (isEmpty(value) && isEmpty(defaultValue)) {
            return null;
        }

        if (isEmpty(value)) {
            value = defaultValue;
        }

        if (base64Encode) {
            value = "citrus:encodeBase64('" + value + "')";
        }

        return value;
    }

    public static final class TestApiClientRequestMessageBuilder extends
        OpenApiClientRequestMessageBuilder {

        private final Map<String, ParameterData> pathParameters = new HashMap<>();

        private final Map<String, ParameterData> queryParameters = new HashMap<>();

        private final Map<String, ParameterData> headerParameters = new HashMap<>();

        private final Map<String, ParameterData> cookieParameters = new HashMap<>();

        private final MultiValueMap<String, Object> formParameters = new LinkedMultiValueMap<>();

        public TestApiClientRequestMessageBuilder(HttpMessage httpMessage,
            OpenApiSpecificationSource openApiSpec,
            String operationId) {
            super(httpMessage, openApiSpec, operationId);
            // Disable autofill by default, as the api enforces required parameters.
            autoFill(AutoFillType.NONE);
        }

        private static void encodeArrayStyleCookies(HttpMessage message) {
            if (message.getCookies() != null && !message.getCookies().isEmpty()) {
                for (Cookie cookie : message.getCookies()) {
                    if (cookie.getValue().contains(",")) {
                        cookie.setValue(encode(cookie.getValue(), getDefaultCharset()));
                    }
                }
            }
        }

        private void pathParameter(String name, Object value, ParameterStyle parameterStyle,
            boolean explode, boolean isObject) {
            // A missing path parameter would result in improper URL. That is why we fail on null or empty.
            if (isNull(value) || isEmpty(value.toString())) {
                throw new CitrusRuntimeException(
                    "Required path parameter '%s' must not be empty".formatted(name));
            }

            pathParameters.put(name,
                new ParameterData(name, value, parameterStyle, explode, isObject));
        }

        private void queryParameter(final String name, Object value,
            ParameterStyle parameterStyle, boolean explode, boolean isObject) {
            queryParameters.put(name,
                new ParameterData(name, value, parameterStyle, explode, isObject));
        }

        private void headerParameter(final String name, Object value,
            ParameterStyle parameterStyle, boolean explode, boolean isObject) {
            headerParameters.put(name,
                new ParameterData(name, value, parameterStyle, explode, isObject));
        }

        private void cookieParameter(final String name, Object value,
            ParameterStyle parameterStyle, boolean explode, boolean isObject) {
            cookieParameters.put(name,
                new ParameterData(name, value, parameterStyle, explode, isObject));
        }

        private void queryParameter(final String name, Object value) {
            queryParameters.put(name,
                new ParameterData(name, value, NONE, false, false));
        }

        private void formParameter(final String name, Object value) {
            if (isNull(value) || isEmpty(value.toString())) {
                return;
            }

            setParameter(formParameters::add, name, value);
        }

        @Override
        protected String getDefinedPathParameter(TestContext context, String name) {
            ParameterData parameterData = pathParameters.get(name);
            String value = name;
            if (parameterData != null) {
                String formatted = formatAccordingToStyle(name, parameterData.value,
                    parameterData.parameterStyle,
                    parameterData.explode, parameterData.isObject);
                int index = formatted.indexOf("=");
                if (index > 0) {
                    value = formatted.substring(index + 1);
                } else {
                    throw new IllegalArgumentException(
                        "formatted should be prefixed with parameter name");
                }
            }

            return context.replaceDynamicContentInString(value);
        }

        @Override
        public Message build(TestContext context, String messageType) {

            HttpMessage message = (HttpMessage) super.build(context, messageType);

            // Apply all parameters directly to the message after it has been created.
            // Message creation produces a clone of the original message.
            // This is necessary because messages built via the XML parser are shared across all builders,
            // whereas attributes set via BeanDefinition properties are applied individually for each bean instance.
            // To avoid applying parameters multiple times to the original message, we must apply them here explicitly.

            applyPathParameters(context, message);
            applyQueryParameters(context, message);
            applyFormParameters(context, message);
            applyHeaderParameters(context, message);
            applyCookieParameters(context, message);

            return message;
        }

        private void applyFormParameters(TestContext context, HttpMessage message) {
            if (!formParameters.isEmpty()) {
                formParameters.replaceAll((key, value) -> {
                    if (value == null) {
                        return null;
                    }
                    return value.stream().map(
                        v -> v instanceof String string ? context.replaceDynamicContentInString(
                            string) : v).toList();
                });
                message.setPayload(formParameters);
            }
        }

        private void applyHeaderParameters(TestContext context, HttpMessage message) {
            headerParameters.forEach((name, params) ->
                setParamRespectingArrayFormat(context, name, params, message::header)
            );
        }

        private void setParamRespectingArrayFormat(TestContext context, String name,
            ParameterData params, BiConsumer<String, Object> parameterConsumer) {
            Object valueToSet = replaceDynamicContentInValue(params.value, context);
            String formatted = formatAccordingToStyle(name, valueToSet, params.parameterStyle,
                params.explode, params.isObject);
            int index = formatted.indexOf("=");
            if (index > 0) {
                formatted = formatted.substring(index + 1);
            } else {
                throw new IllegalArgumentException(
                    "formatted should be prefixed with parameter name");
            }
            setParameter(parameterConsumer, name, formatted);
        }

        private void applyCookieParameters(TestContext context, HttpMessage message) {
            cookieParameters.forEach(
                (name, params) -> setParamRespectingArrayFormat(context, name, params,
                    (paramName, paramValue) -> message.cookie(
                        new Cookie(paramName, paramValue != null ? paramValue.toString() : null))));

            encodeArrayStyleCookies(message);
        }

        private void applyPathParameters(TestContext context, HttpMessage message) {
            // Start with the original path to remove potentially added value from the context (real ord random).
            // We definitely want to set our path parameters, no matter what has been derived before.
            String path = getMessage().getPath();
            message.path(path);
            pathParameters.forEach((name, params) -> {
                String pathParameter = getDefinedPathParameter(context, name);
                message.path(message.getPath().replace("${" + name + "}", pathParameter));
                message.path(message.getPath().replace("{" + name + "}", pathParameter));
            });
            // All still undefined parameters must still exist in the context.
            message.path(context.resolveDynamicValue(message.getPath()));
        }

        private void applyQueryParameters(TestContext context, HttpMessage message) {
            queryParameters.forEach((name, params) -> {
                // We are working on the copy of the message. It may contain random expressions, created by super.
                // Since we have explicit values, we first need to clear the existing to avoid accumulation.
                message.getQueryParams().remove(name);

                Object valueToSet = replaceDynamicContentInValue(params.value, context);

                String formatted = formatAccordingToStyle(name, valueToSet, params.parameterStyle,
                    params.explode, params.isObject);
                String[] queryParamValues = formatted.split("&");
                for (String queryParamValue : queryParamValues) {
                    String[] keyValue = queryParamValue.split("=");

                    // It is valid to have a parameter name with space. We need to encode this for sending.
                    String key = keyValue[0].replace(" ", "%20");
                    String value = keyValue.length > 1 ? keyValue[1] : "";

                    message.queryParam(key, value);
                }
            });
        }

        private void setParameter(BiConsumer<String, Object> parameterConsumer,
            final String parameterName, Object parameterValue) {
            if (parameterValue != null) {
                if (byte[].class.isAssignableFrom(parameterValue.getClass())) {
                    // Pass through byte array
                    parameterConsumer.accept(parameterName, parameterValue);
                } else if (parameterValue.getClass().isArray()) {
                    int length = Array.getLength(parameterValue);
                    for (int i = 0; i < length; i++) {
                        Object singleValue = Array.get(parameterValue, i);
                        parameterConsumer.accept(parameterName, singleValue);
                    }
                } else if (parameterValue instanceof Collection<?> collection) {
                    collection.forEach(
                        singleValue -> parameterConsumer.accept(parameterName, singleValue));
                } else {
                    parameterConsumer.accept(parameterName, parameterValue);
                }
            }
        }

        private static Object replaceDynamicContentInValue(Object value, TestContext context) {
            Object valueToSet = value;
            if (value instanceof String stringValue) {
                valueToSet = context.replaceDynamicContentInString(stringValue);
            } else if (value instanceof List<?> list) {
                valueToSet = list.stream().map(element -> element instanceof String stringValue
                    ? context.replaceDynamicContentInString(stringValue) : element).toList();
            }
            return valueToSet;
        }
    }

    public record ParameterData(@Nonnull String name, Object value,
                                @Nonnull ParameterStyle parameterStyle,
                                boolean explode, boolean isObject) {

    }
}
