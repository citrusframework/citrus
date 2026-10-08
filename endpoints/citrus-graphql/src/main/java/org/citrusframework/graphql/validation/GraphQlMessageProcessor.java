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
package org.citrusframework.graphql.validation;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.graphql.document.GraphQlDocuments;
import org.citrusframework.graphql.document.GraphQlOperation;
import org.citrusframework.graphql.message.GraphQlMessageHeaders;
import org.citrusframework.graphql.message.GraphQlMessages;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageProcessor;
import org.springframework.graphql.GraphQlRequest;

/**
 * Runs first on messages received through GraphQL receive actions: marks them with
 * {@link GraphQlMessageHeaders#KIND}, so the GraphQL validator is selected for them and nothing changes
 * for other messages, and sets the operation headers on incoming requests so tests can validate or
 * extract them. Invalid requests are left to the validator, which reports them.
 */
public class GraphQlMessageProcessor implements MessageProcessor {

    private final GraphQlMessageValidationContext validationContext;

    public GraphQlMessageProcessor(GraphQlMessageValidationContext validationContext) {
        this.validationContext = validationContext;
    }

    @Override
    public void process(Message message, TestContext context) {
        if (validationContext.getKind() == GraphQlMessageValidationContext.Kind.RESPONSE) {
            message.setHeader(GraphQlMessageHeaders.KIND, GraphQlMessageHeaders.KIND_RESPONSE);
            return;
        }

        message.setHeader(GraphQlMessageHeaders.KIND, GraphQlMessageHeaders.KIND_REQUEST);
        if (!validationContext.getSettings(context).strict()) {
            return;
        }

        try {
            GraphQlRequest request = GraphQlMessages.toRequest(message);
            GraphQlOperation operation = GraphQlDocuments.selectOperation(
                    GraphQlDocuments.parse(request.getDocument()), request.getOperationName());
            if (operation.name() != null) {
                message.setHeader(GraphQlMessageHeaders.OPERATION_NAME, operation.name());
            }
            message.setHeader(GraphQlMessageHeaders.OPERATION_TYPE, operation.typeName());
        } catch (ValidationException e) {
            // reported by the validator
        }
    }

    public GraphQlMessageValidationContext getValidationContext() {
        return validationContext;
    }
}
