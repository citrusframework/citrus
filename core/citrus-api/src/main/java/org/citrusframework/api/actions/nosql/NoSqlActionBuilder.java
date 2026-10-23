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

package org.citrusframework.api.actions.nosql;

import java.time.Duration;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.api.actions.ActionBuilder;
import org.citrusframework.api.actions.ReferenceResolverAwareBuilder;

/**
 * Builder interface for generic NoSQL test actions. The interface is free of database SDK types;
 * the actual builder lives in the {@code citrus-nosql} connector.
 */
public interface NoSqlActionBuilder<T extends TestAction, B extends NoSqlActionBuilder<T, B>>
        extends ActionBuilder<T, B>, TestActionBuilder<T>, ReferenceResolverAwareBuilder<T, B> {

    /**
     * Name of the named database configuration to use.
     */
    B client(String client);

    /**
     * Query documents or records from the target.
     */
    B query();

    /**
     * Insert a new document or record.
     */
    B insert();

    /**
     * Update matching documents or records.
     */
    B update();

    /**
     * Delete matching documents or records.
     */
    B delete();

    /**
     * Provider specific native operation. The command name must be declared by the provider.
     */
    B nativeOperation(String command);

    /**
     * Target collection, table or index, depending on the provider.
     */
    B target(String target);

    /**
     * Operation payload. For queries and deletes this is the filter expression.
     */
    B payload(String payload);

    /**
     * Alias for {@link #payload(String)} for query and delete filters.
     */
    B filter(String filter);

    /**
     * Validate a value in the result at the given JSON path.
     */
    B validate(String path, String expected);

    /**
     * Extract a value from the result at the given JSON path into a test variable.
     */
    B extract(String path, String variable);

    /**
     * Limit the number of returned query results.
     */
    B maxResults(int maxResults);

    /**
     * Explicit opt-in retry policy for read operations. Writes are never retried implicitly.
     */
    B retry(int maxAttempts, Duration interval);

    /**
     * Factory used by action support classes and DSLs to create a NoSQL action builder.
     */
    interface BuilderFactory {

        NoSqlActionBuilder<?, ?> nosql();
    }
}
