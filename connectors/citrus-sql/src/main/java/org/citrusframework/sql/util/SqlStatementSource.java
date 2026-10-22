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

package org.citrusframework.sql.util;

/**
 * Source of SQL statements for a database test action. Sources are kept in the order they were
 * declared on the action builder and are resolved to SQL statements when the action executes.
 */
public sealed interface SqlStatementSource {

    /**
     * A single inline SQL statement.
     *
     * @param sql the SQL statement.
     */
    record Statement(String sql) implements SqlStatementSource {
    }

    /**
     * An inline SQL script that may hold several statements, such as a PL/SQL block.
     *
     * @param script the SQL script.
     */
    record Script(String script) implements SqlStatementSource {
    }

    /**
     * Path to an external SQL script resource. The path is resolved when the action executes and
     * may use the "classpath:" or "file:" prefix, a URL, or a plain file system path.
     *
     * @param path the resource path.
     */
    record ResourcePath(String path) implements SqlStatementSource {
    }
}
