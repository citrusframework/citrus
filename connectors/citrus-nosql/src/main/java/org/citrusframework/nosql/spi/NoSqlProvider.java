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

package org.citrusframework.nosql.spi;

import java.util.Set;

import org.citrusframework.nosql.model.NoSqlResult;

/**
 * Service provider interface for NoSQL databases. Implementations are discovered through
 * {@code META-INF/citrus/nosql/provider/<name>} classpath resources that declare the implementation
 * type. Providers must be stateless and thread safe: a single instance serves all operations of a
 * test suite.
 */
public interface NoSqlProvider {

    /**
     * Unique provider id, for example {@code mongodb}.
     */
    String id();

    /**
     * Operation kinds and features supported by this provider.
     */
    Set<NoSqlCapability> capabilities();

    /**
     * Native command names supported through {@link NoSqlOperationKind#NATIVE}. Undeclared commands
     * are rejected before any client call.
     */
    default Set<String> nativeCommands() {
        return Set.of();
    }

    /**
     * Execute the operation and return a normalized result.
     */
    NoSqlResult execute(NoSqlOperation operation, NoSqlProviderContext context);
}
