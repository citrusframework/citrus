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

package org.citrusframework.dsl.nosql;

import org.citrusframework.api.actions.nosql.NoSqlActionBuilder;
import org.citrusframework.api.actions.nosql.NoSqlTestActions;
import org.citrusframework.dsl.TestActionLookupSupport;

/**
 * Interface that extends {@code NoSqlTestActions} and {@code TestActionLookupSupport},
 * providing default lookup functionality for NoSQL action builders.
 * <p>
 * This interface facilitates the integration of NoSQL test action builders into the test framework
 * by using a common lookup mechanism. It enables the construction and retrieval of NoSQL test actions
 * via a domain-specific language (DSL) approach.
 */
public interface NoSqlTestActionSupport extends NoSqlTestActions, TestActionLookupSupport {

    @Override
    default NoSqlActionBuilder<?, ?> nosql() {
        return lookup("nosql");
    }
}
