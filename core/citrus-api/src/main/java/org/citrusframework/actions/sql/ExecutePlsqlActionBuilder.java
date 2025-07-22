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

package org.citrusframework.actions.sql;

import java.nio.charset.Charset;

import javax.sql.DataSource;

import org.citrusframework.TestAction;
import org.citrusframework.spi.Resource;

public interface ExecutePlsqlActionBuilder<T extends TestAction, B extends ExecutePlsqlActionBuilder<T, B>>
        extends DatabaseConnectingActionBuilder<T, B> {

    /**
     * Setter for inline script.
     * @param script
     */
    ExecutePlsqlActionBuilder<T, B> sqlScript(String script);

    /**
     * Setter for external file resource containing the SQL statements to execute.
     */
    ExecutePlsqlActionBuilder<T, B> sqlResource(Resource sqlResource, Charset charset);

    /**
     * Ignore errors during execution.
     * @param ignoreErrors boolean flag to set
     */
    ExecutePlsqlActionBuilder<T, B> ignoreErrors(boolean ignoreErrors);

    interface BuilderFactory {

        ExecutePlsqlActionBuilder<?, ?> plsql();

        default ExecutePlsqlActionBuilder<?, ?> plsql(DataSource dataSource) {
            return plsql().dataSource(dataSource);
        }

    }

}
