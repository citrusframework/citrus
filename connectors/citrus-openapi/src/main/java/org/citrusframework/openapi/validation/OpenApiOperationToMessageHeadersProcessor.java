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
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.openapi.OpenApiMessageType;
import org.citrusframework.openapi.OpenApiSpecification;

import static org.citrusframework.openapi.OpenApiMessageHeaders.OAS_MESSAGE_TYPE;
import static org.citrusframework.openapi.OpenApiMessageHeaders.OAS_SPECIFICATION_ID;
import static org.citrusframework.openapi.OpenApiMessageHeaders.OAS_UNIQUE_OPERATION_ID;

/**
 * {@code MessageProcessor} that prepares the message for OpenAPI validation by setting respective
 * message headers.
 */
public class OpenApiOperationToMessageHeadersProcessor implements MessageProcessor {

    private final OpenApiSpecification openApiSpecification;

    private final String operationKey;

    private final OpenApiMessageType type;

    public OpenApiOperationToMessageHeadersProcessor(OpenApiSpecification openApiSpecification,
                                                     String operationKey,
                                                     OpenApiMessageType type) {
        this.operationKey = operationKey;
        this.openApiSpecification = openApiSpecification;
        this.type = type;
    }

    @Override
    public void process(Message message, TestContext context) {
        openApiSpecification
                .getOperation(operationKey, context)
                .ifPresent(operationPathAdapter -> {
                    message.setHeader(OAS_SPECIFICATION_ID, openApiSpecification.getUid());
                    // Store the uniqueId of the operation, rather than the operationKey, to avoid clashes.
                    message.setHeader(OAS_UNIQUE_OPERATION_ID, operationPathAdapter.uniqueOperationId());
                    message.setHeader(OAS_MESSAGE_TYPE, type.toHeaderName());
                });
    }
}
