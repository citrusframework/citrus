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

import jakarta.servlet.http.Cookie;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.actions.HttpClientRequestActionBuilder;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.actions.OpenApiClientRequestActionBuilder;
import org.citrusframework.openapi.actions.OpenApiSpecificationSource;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.lang.String.format;
import static org.citrusframework.openapi.util.OpenApiUtils.createFullPathOperationIdentifier;
import static org.citrusframework.util.FileUtils.copyToByteArray;
import static org.citrusframework.util.FileUtils.getDefaultCharset;
import static org.citrusframework.util.StringUtils.isEmpty;

public class RestApiSendMessageActionBuilder extends OpenApiClientRequestActionBuilder {

    private final GeneratedApi generatedApi;

    private final List<ApiActionBuilderCustomizer> customizers;

    private final MultiValueMap<String, Object> formParameters = new LinkedMultiValueMap<>();

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
        super(new OpenApiSpecificationSource(openApiSpec), messageBuilder, httpMessage, createFullPathOperationIdentifier(method, path));

        this.generatedApi = generatedApi;
        this.customizers = generatedApi.getCustomizers();

        endpoint(generatedApi.getEndpoint());

        httpMessage.path(path);

        name(format("send-%s:%s", generatedApi.getClass().getSimpleName().toLowerCase(), operationName));

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

    protected void pathParameter(String name, Object value, ParameterStyle parameterStyle, boolean explode, boolean isObject) {
        ((TestApiClientRequestMessageBuilder) getMessageBuilderSupport().getMessageBuilder()).pathParameter(name, value, parameterStyle, explode, isObject);
    }

    protected void formParameter(String name, Object value) {
        setFormParameter(name, value);
    }

    protected void setFormParameter(String name, Object value) {
        if (value == null || isEmpty(value.toString())) {
            return;
        }

        setParameter(formParameters::add, name, value);
    }

    protected void queryParameter(final String name, Object value) {
        if (value == null || isEmpty(value.toString())) {
            return;
        }

        setParameter((paramName, paramValue) -> super.queryParam(paramName, paramValue != null ? paramValue.toString() : null), name, value);
    }

    protected void queryParameter(final String name, Object value, ParameterStyle parameterStyle, boolean explode, boolean isObject) {
        if (value == null || isEmpty(value.toString())) {
            return;
        }

        String formatted = OpenApiParameterFormatter.formatArray(name, value, parameterStyle, explode, isObject);
        String[] queryParamValues = formatted.split("&");
        for (String queryParamValue : queryParamValues) {
            String[] keyValue = queryParamValue.split("=");
            queryParameter(keyValue[0], keyValue[1]);
        }
    }

    protected void headerParameter(String name, Object value) {
        if (value == null || isEmpty(value.toString())) {
            return;
        }

        setParameter((paramName, paramValue) -> getMessageBuilderSupport().header(paramName, paramValue), name, value);
    }

    protected void headerParameter(String name, Object value, ParameterStyle parameterStyle, boolean explode, boolean isObject) {
        if (value == null || isEmpty(value.toString())) {
            return;
        }

        headerParameter(name, OpenApiParameterFormatter.formatArray(name, value, parameterStyle, explode, isObject));
    }

    protected void cookieParameter(String name, Object value) {
        if (value == null || isEmpty(value.toString())) {
            return;
        }

        setParameter((paramName, paramValue) -> getMessageBuilderSupport().cookie((new Cookie(paramName, paramValue != null ? paramValue.toString() : null))), name, value);
    }

    protected void cookieParameter(String name, Object value, ParameterStyle parameterStyle, boolean explode, boolean isObject) {
        if (value == null || isEmpty(value.toString())) {
            return;
        }

        String formatted = OpenApiParameterFormatter.formatArray(name, value, parameterStyle, explode, isObject);
        String[] keyValue = formatted.split("=");

        // URL Encoding is mandatory especially in the case of multiple values, as multiple values
        // are separated by a comma and a comma is not a valid character in cookies.
        cookieParameter(keyValue[0], keyValue[1]);
    }

    private void setParameter(BiConsumer<String, Object> parameterConsumer, final String parameterName, Object parameterValue) {
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

    @Override
    public SendMessageAction doBuild() {
        if (!formParameters.isEmpty()) {
            getMessageBuilderSupport().body(formParameters);
        }

        return super.doBuild();
    }

    protected byte[] toBinary(Object object) {
        if (object instanceof byte[] bytes) {
            return bytes;
        } else if (object instanceof Resource resource) {
            return copyToByteArray(resource.getInputStream());
        } else if (object instanceof String string) {

            Resource resource = Resources.create(string);
            if (resource != null && resource.exists()) {
                return toBinary(resource);
            }

            try {
                return Base64.getDecoder().decode(string);
            } catch (IllegalArgumentException e) {
                // Ignore decoding failure and treat as regular string
            }

            return string.getBytes(getDefaultCharset());
        }

        throw new IllegalArgumentException(
                "Cannot convert object to byte array. Only byte[], Resource, and String are supported: "
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

    public static final class TestApiClientRequestMessageBuilder extends OpenApiClientRequestMessageBuilder {

        private final Map<String, ParameterData> pathParameters = new HashMap<>();

        public TestApiClientRequestMessageBuilder(HttpMessage httpMessage,
                                                  OpenApiSpecificationSource openApiSpec,
                                                  String operationId) {
            super(httpMessage, openApiSpec, operationId);
        }

        private static void encodeArrayStyleCookies(HttpMessage message) {
            if (message.getCookies() != null && !message.getCookies().isEmpty()) {
                for (Cookie cookie : message.getCookies()) {
                    if (cookie.getValue().contains(",")) {
                        cookie.setValue(URLEncoder.encode(cookie.getValue(), getDefaultCharset()));
                    }
                }
            }
        }

        public void pathParameter(String name, Object value, ParameterStyle parameterStyle, boolean explode, boolean isObject) {
            if (value == null) {
                throw new CitrusRuntimeException(
                        "Mandatory path parameter '%s' must not be null".formatted(name));
            }

            pathParameters.put(name, new ParameterData(name, value, parameterStyle, explode, isObject));
        }

        @Override
        protected String getDefinedPathParameter(TestContext context, String name) {
            ParameterData parameterData = pathParameters.get(name);
            String formatted = name;
            if (parameterData != null) {
                formatted = OpenApiParameterFormatter.formatArray(name, parameterData.value, parameterData.parameterStyle,
                        parameterData.explode, parameterData.isObject);
            }

            return context.replaceDynamicContentInString(formatted);
        }

        @Override
        public Message build(TestContext context, String messageType) {
            HttpMessage message = (HttpMessage) super.build(context, messageType);
            encodeArrayStyleCookies(message);
            return message;
        }
    }

    public record ParameterData(String name, Object value, ParameterStyle parameterStyle, boolean explode, boolean isObject) {
    }
}
