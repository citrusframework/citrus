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

package com.consol.citrus.message;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public interface CorrelationManager<T> {

    /**
     * Store object to correlation storage using the given correlation key.
     * @param correlationKey
     * @param object
     */
    void store(String correlationKey, T object);

    /**
     * Finds stored object by its correlation key.
     * @param correlationKey
     * @return
     */
    T find(String correlationKey);

}
