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
package org.citrusframework.graphql.message;

import org.citrusframework.message.MessageHeaders;

/**
 * Message headers set on GraphQL messages.
 */
public final class GraphQlMessageHeaders {

    public static final String GRAPHQL_PREFIX = MessageHeaders.PREFIX + "graphql_";

    /** Name of the executed operation: the given one, or the name of the document's single operation. */
    public static final String OPERATION_NAME = GRAPHQL_PREFIX + "operation_name";

    /** Type of the executed operation ({@code query}, {@code mutation}); only set when the document was parsed. */
    public static final String OPERATION_TYPE = GRAPHQL_PREFIX + "operation_type";

    /** Marks a message received through a GraphQL receive action: {@link #KIND_REQUEST} or {@link #KIND_RESPONSE}. */
    public static final String KIND = GRAPHQL_PREFIX + "kind";

    public static final String KIND_REQUEST = "request";
    public static final String KIND_RESPONSE = "response";

    private GraphQlMessageHeaders() {
        // constants only
    }
}
