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

import org.citrusframework.context.TestContext;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.validation.ValidationProcessor;

/**
 * {@code ValidationProcessor} that delegates validation of OpenApi responses to instances of {@link OpenApiResponseValidator}.
 */
public class OpenApiResponseValidationProcessor implements
    ValidationProcessor {

    private final OpenApiSpecification openApiSpecification;

    private final String operationId;

    private boolean enabled = true;

    public OpenApiResponseValidationProcessor(OpenApiSpecification openApiSpecification, String operationId) {
        this.operationId = operationId;
        this.openApiSpecification = openApiSpecification;
    }

    @Override
    public void validate(Message message, TestContext context) {

        if (!enabled || !(message instanceof HttpMessage httpMessage)) {
            return;
        }

        openApiSpecification.getOperation(
            operationId, context).ifPresent(operationPathAdapter ->
            openApiSpecification.getResponseValidator().ifPresent(openApiResponseValidator ->
                openApiResponseValidator.validateResponse(operationPathAdapter, httpMessage)));
    }

    public void setEnabled(boolean b) {
        this.enabled = b;
    }
}
