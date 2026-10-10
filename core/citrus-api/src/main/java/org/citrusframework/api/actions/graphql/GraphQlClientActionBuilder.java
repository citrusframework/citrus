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
package org.citrusframework.api.actions.graphql;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.api.actions.ReferenceResolverAwareBuilder;
import org.citrusframework.api.actions.http.HttpReceiveResponseMessageBuilderFactory;
import org.citrusframework.api.actions.http.HttpSendRequestMessageBuilderFactory;

public interface GraphQlClientActionBuilder<T extends TestAction, B extends TestActionBuilder.DelegatingTestActionBuilder<T>>
        extends ReferenceResolverAwareBuilder<T, B> {

    /**
     * Sends a GraphQL operation as client.
     */
    <M extends HttpSendRequestMessageBuilderFactory<T, M>> GraphQlClientRequestActionBuilder<T, M, ?> send();

    /**
     * Receives the GraphQL response as client.
     */
    <M extends HttpReceiveResponseMessageBuilderFactory<T, M>> GraphQlClientResponseActionBuilder<T, M, ?> receive();
}
