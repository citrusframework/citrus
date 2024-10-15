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

package org.citrusframework.openapi.validation;

import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.report.ValidationReport;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.http.message.HttpMessageUtils;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.citrusframework.util.FileUtils;
import org.springframework.util.MultiValueMap;

/**
 * Specific validator that uses atlassian and is responsible for validating HTTP requests
 * against an OpenAPI specification using the provided {@code OpenApiInteractionValidator}.
 */
public class OpenApiRequestValidator extends OpenApiValidator {

    public OpenApiRequestValidator(OpenApiSpecification openApiSpecification) {
        super(openApiSpecification);
    }

    @Override
    protected String getType() {
        return "request";
    }

    public void validateRequest(OperationPathAdapter operationPathAdapter,
        HttpMessage requestMessage) {

        if (openApiInteractionValidator != null) {
            ValidationReport validationReport = openApiInteractionValidator.validateRequest(
                createRequestFromMessage(operationPathAdapter, requestMessage));
            if (validationReport.hasErrors()) {
                throw new ValidationException(
                    constructErrorMessage(operationPathAdapter, validationReport));
            }
        }
    }

    public ValidationReport validateRequestToReport(OperationPathAdapter operationPathAdapter,
        HttpMessage requestMessage) {

        if (openApiInteractionValidator != null) {
            return openApiInteractionValidator.validateRequest(
                createRequestFromMessage(operationPathAdapter, requestMessage));
        }

        return ValidationReport.empty();
    }

    Request createRequestFromMessage(OperationPathAdapter operationPathAdapter,
        HttpMessage httpMessage) {
        var payload = httpMessage.getPayload();

        String contextPath = operationPathAdapter.contextPath();
        String requestUri = (String) httpMessage.getHeader(HttpMessageHeaders.HTTP_REQUEST_URI);
        if (contextPath != null && requestUri.startsWith(contextPath)) {
            requestUri = requestUri.substring(contextPath.length());
        }

        SimpleRequest.Builder requestBuilder = new SimpleRequest.Builder(
            httpMessage.getRequestMethod().asHttpMethod().name(), requestUri
        );

        if (payload != null) {
            requestBuilder = requestBuilder.withBody(convertPayload(payload));
        }

        SimpleRequest.Builder finalRequestBuilder = requestBuilder;
        finalRequestBuilder.withAccept(httpMessage.getAccept());

        HttpMessageUtils.getQueryParameterMap(httpMessage)
            .forEach((key, value) -> finalRequestBuilder.withQueryParam(key, new ArrayList<>(
                value)));

        httpMessage.getHeaders().forEach((key, value) -> {
            if (value instanceof Collection<?> collection) {
                collection.forEach( v -> finalRequestBuilder.withHeader(key, v != null ? v.toString() : null));
            } else {
                finalRequestBuilder.withHeader(key,
                    value != null ? value.toString() : null);
            }
        });

        httpMessage.getCookies().forEach(cookie -> finalRequestBuilder.withHeader("Cookie", URLDecoder.decode(cookie.getName()+"="+cookie.getValue(),
            FileUtils.getDefaultCharset())));

        return requestBuilder.build();
    }

    private String convertPayload(Object payload) {

        if (payload instanceof MultiValueMap<?,?> multiValueMap) {
            return serializeForm(multiValueMap, StandardCharsets.UTF_8);
        }

        return payload != null ? payload.toString() : null;
    }

    /**
     * We cannot validate a MultiValueMap. The map will later on be converted to a string representation
     * by Spring. For validation, we need to mimic this transformation here.

     * @see org.springframework.http.converter.FormHttpMessageConverter
     */
    private String serializeForm(MultiValueMap<?, ?> formData, Charset charset) {
        StringBuilder builder = new StringBuilder();
        formData.forEach((name, values) -> values.forEach(value -> {
            if (!builder.isEmpty()) {
                builder.append('&');
            }
            builder.append(URLEncoder.encode(name.toString(), charset));
            if (value != null) {
                builder.append('=');
                builder.append(URLEncoder.encode(String.valueOf(value), charset));
            }
        }));

        return builder.toString();
    }
}
