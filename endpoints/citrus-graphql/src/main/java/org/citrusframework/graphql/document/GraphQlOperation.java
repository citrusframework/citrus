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
package org.citrusframework.graphql.document;

import graphql.language.OperationDefinition.Operation;

/**
 * The operation of a GraphQL document that a request executes.
 * @param name the operation name, {@code null} for an anonymous operation
 * @param type query, mutation or subscription
 */
public record GraphQlOperation(String name, Operation type) {

    /**
     * Gets the operation type as used in message headers, e.g. {@code query}.
     */
    public String typeName() {
        return type.name().toLowerCase();
    }
}
