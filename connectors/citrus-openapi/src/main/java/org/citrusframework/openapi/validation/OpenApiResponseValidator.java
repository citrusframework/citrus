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

import com.atlassian.oai.validator.model.Request.Method;
import com.atlassian.oai.validator.model.Response;
import com.atlassian.oai.validator.model.SimpleResponse;
import com.atlassian.oai.validator.report.ValidationReport;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.model.OperationPathAdapter;
import org.springframework.http.HttpStatusCode;

/**
 * Specific validator, that facilitates the use of Atlassian's Swagger Request Validator,
 * and delegates validation of OpenApi requests to instances of {@link OpenApiRequestValidator}.
 */
public class OpenApiResponseValidator extends OpenApiValidator {

    public OpenApiResponseValidator(OpenApiSpecification openApiSpecification) {
        super(openApiSpecification);
    }

    @Override
    protected String getType() {
        return "response";
    }

    public void validateResponse(OperationPathAdapter operationPathAdapter, HttpMessage httpMessage) {

        if (openApiInteractionValidator != null) {
            HttpStatusCode statusCode = httpMessage.getStatusCode();
            Response response = createResponseFromMessage(httpMessage,
                statusCode != null ? statusCode.value() : null);

            ValidationReport validationReport = openApiInteractionValidator.validateResponse(
                operationPathAdapter.apiPath(),
                Method.valueOf(operationPathAdapter.operation().getMethod().toUpperCase()),
                response);
            if (validationReport.hasErrors()) {
                throw new ValidationException(constructErrorMessage(operationPathAdapter, validationReport));
            }
        }
    }

    public ValidationReport validateResponseToReport(OperationPathAdapter operationPathAdapter, HttpMessage httpMessage) {

        if (openApiInteractionValidator != null) {
            HttpStatusCode statusCode = httpMessage.getStatusCode();
            Response response = createResponseFromMessage(httpMessage,
                statusCode != null ? statusCode.value() : null);

            return openApiInteractionValidator.validateResponse(
                operationPathAdapter.apiPath(),
                Method.valueOf(operationPathAdapter.operation().getMethod().toUpperCase()),
                response);

        }
        return ValidationReport.empty();
    }

    Response createResponseFromMessage(HttpMessage message, Integer statusCode) {
        var payload = message.getPayload();
        SimpleResponse.Builder responseBuilder = new SimpleResponse.Builder(statusCode);

        if (payload != null) {
            responseBuilder = responseBuilder.withBody(payload.toString());
        }

        SimpleResponse.Builder finalResponseBuilder = responseBuilder;
        message.getHeaders().forEach((key, value) -> finalResponseBuilder.withHeader(key,
            value != null ? value.toString() : null));

        return responseBuilder.build();
    }
}
