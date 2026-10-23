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

/**
 * Capabilities a provider may declare. The first five entries mirror the operation kinds. Native
 * commands are declared separately as free-form names so that providers can add commands without a
 * new enum member. New enum members are only added in minor releases.
 */
public enum NoSqlCapability {

    QUERY,
    INSERT,
    UPDATE,
    DELETE,
    NATIVE,

    /** Provider supports transactional execution across multiple operations. */
    TRANSACTIONS
}
