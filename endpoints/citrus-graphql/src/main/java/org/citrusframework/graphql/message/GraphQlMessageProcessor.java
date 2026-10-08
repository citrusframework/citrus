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

import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageProcessor;

/**
 * Marks messages received through GraphQL receive actions, so the GraphQL validator is selected for
 * them and nothing else changes for other messages.
 */
public class GraphQlMessageProcessor implements MessageProcessor {

    private final String kind;

    public GraphQlMessageProcessor(String kind) {
        this.kind = kind;
    }

    @Override
    public void process(Message message, TestContext context) {
        message.setHeader(GraphQlMessageHeaders.KIND, kind);
    }

    public String getKind() {
        return kind;
    }
}
