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
package org.citrusframework.graphql.actions;

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.graphql.validation.GraphQlMessageProcessor;
import org.citrusframework.graphql.validation.GraphQlMessageValidationContext;
import org.citrusframework.validation.context.DefaultMessageValidationContext;

/**
 * Shared wiring of the GraphQL receive actions.
 */
final class GraphQlActions {

    private GraphQlActions() {
        // utility class
    }

    /**
     * Uses a GraphQL validation context the test added itself, or adds the one built from the
     * action's expectations, bound to the action's endpoint. Without a control body there is nothing
     * for a plain message validation context to compare, so the one inferred by default is dropped;
     * otherwise a non-JSON reply (e.g. an HTML error page) would need a text validator module.
     */
    static GraphQlMessageValidationContext reconcile(ReceiveMessageAction.ReceiveMessageActionBuilder<?, ?, ?> builder,
                                                     GraphQlMessageValidationContext.Builder expectations,
                                                     Endpoint endpoint, String endpointUri, boolean hasControlBody,
                                                     GraphQlMessageValidationContext previous) {
        if (!hasControlBody) {
            builder.getValidationContextBuilders()
                    .removeIf(contextBuilder -> contextBuilder.build().getClass() == DefaultMessageValidationContext.class);
        }

        GraphQlMessageValidationContext existing = builder.getValidationContexts().stream()
                .filter(GraphQlMessageValidationContext.class::isInstance)
                .map(GraphQlMessageValidationContext.class::cast)
                .findFirst()
                .orElse(null);
        if (existing != null) {
            if (existing != previous && expectations.hasExpectations()) {
                throw new CitrusRuntimeException("GraphQL expectations on the action cannot be combined with an explicit " +
                        "GraphQlMessageValidationContext - set them on one of both");
            }

            return existing;
        }

        GraphQlMessageValidationContext validationContext = expectations.endpoint(endpoint).endpointUri(endpointUri).build();
        builder.validate(validationContext);
        return validationContext;
    }

    /**
     * Adds the processor that marks received messages for GraphQL validation, once.
     */
    static void addProcessor(ReceiveMessageAction.ReceiveMessageActionBuilder<?, ?, ?> builder,
                             GraphQlMessageValidationContext validationContext) {
        if (validationContext != null && builder.getMessageProcessors().stream().noneMatch(GraphQlMessageProcessor.class::isInstance)) {
            builder.process(new GraphQlMessageProcessor(validationContext));
        }
    }
}
