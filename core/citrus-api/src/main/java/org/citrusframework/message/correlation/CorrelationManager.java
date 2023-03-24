/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.message.correlation;

import org.citrusframework.context.TestContext;

/**
 * Correlation manager stores objects with a correlation key. Clients can access the same objects
 * some time later with same correlation key. This mechanism is used in synchronous communication where
 * request and response messages are stored for correlating consumer and producer components.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public interface CorrelationManager<T> {

    /**
     * Creates new correlation key in test context by saving as test variable. Method is called when
     * synchronous communication is initialized.
     *
     * @param correlationKeyName
     * @param correlationKey
     * @param context
     */
    void saveCorrelationKey(String correlationKeyName, String correlationKey, TestContext context);

    /**
     * Gets correlation key for given identifier. Consults test context with test variables
     * for retrieving stored correlation key.
     *
     * @param correlationKeyName
     * @param context
     * @return
     */
    String getCorrelationKey(String correlationKeyName, TestContext context);

    /**
     * Store object to correlation storage using the given correlation key.
     * @param correlationKey
     * @param object
     */
    void store(String correlationKey, T object);

    /**
     * Finds stored object by its correlation key.
     * @param correlationKey
     * @param timeout
     * @return
     */
    T find(String correlationKey, long timeout);

    /**
     * Sets the object store implementation
     * @param store
     */
    void setObjectStore(ObjectStore<T> store);

    /**
     * Gets the object store implementation.
     * @return
     */
    ObjectStore<T> getObjectStore();

}
