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

import static org.citrusframework.openapi.OpenApiSettings.isRequestValidationEnabledlobally;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.report.ValidationReport;
import java.util.ArrayList;
import java.util.Collection;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.openapi.model.OperationPathAdapter;

/**
 * Specific validator that uses atlassian and is responsible for validating HTTP requests
 * against an OpenAPI specification using the provided {@code OpenApiInteractionValidator}.
 */
public class OpenApiRequestValidator extends OpenApiValidator {

    public OpenApiRequestValidator(OpenApiInteractionValidator openApiInteractionValidator) {
        super(openApiInteractionValidator, isRequestValidationEnabledlobally());
    }

    @Override
    protected String getType() {
        return "request";
    }

    public void validateRequest(OperationPathAdapter operationPathAdapter,
        HttpMessage requestMessage) {

        if (enabled && openApiInteractionValidator != null) {
            ValidationReport validationReport = openApiInteractionValidator.validateRequest(
                createRequestFromMessage(operationPathAdapter, requestMessage));
            if (validationReport.hasErrors()) {
                throw new ValidationException(
                    constructErrorMessage(operationPathAdapter, validationReport));
            }
        }
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
            requestBuilder = requestBuilder.withBody(payload.toString());
        }

        SimpleRequest.Builder finalRequestBuilder = requestBuilder;
        finalRequestBuilder.withAccept(httpMessage.getAccept());

        httpMessage.getQueryParams()
            .forEach((key, value) -> finalRequestBuilder.withQueryParam(key, new ArrayList<>(
                value)));

        httpMessage.getHeaders().forEach((key, value) -> {
            if (value instanceof Collection<?>) {
                ((Collection<?>) value).forEach( v -> finalRequestBuilder.withHeader(key, v != null ? v.toString() : null));
            } else {
                finalRequestBuilder.withHeader(key,
                    value != null ? value.toString() : null);
            }
        });

        return requestBuilder.build();
    }

}
