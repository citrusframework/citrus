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
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default correlation manager implementation works on simple in memory map for storing objects.
 * Correlation key is the map key. Clients can access objects in the store using the correlation key.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public class DefaultCorrelationManager<T> implements CorrelationManager<T> {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(DefaultCorrelationManager.class);

    /** Map of managed objects */
    private ObjectStore<T> objectStore = new DefaultObjectStore<T>();

    @Override
    public void saveCorrelationKey(String correlationKeyName, String correlationKey, TestContext context) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Saving correlation key for '%s'", correlationKeyName));
        }

        context.setVariable(correlationKeyName, correlationKey);
    }

    @Override
    public String getCorrelationKey(String correlationKeyName, TestContext context) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Get correlation key for '%s'", correlationKeyName));
        }

        if (context.getVariables().containsKey(correlationKeyName)) {
            return context.getVariable(correlationKeyName);
        }

        throw new CitrusRuntimeException(String.format("Failed to get correlation key for '%s'", correlationKeyName));
    }

    @Override
    public void store(String correlationKey, T object) {
        if (object == null) {
            logger.warn(String.format("Ignore correlated null object for '%s'", correlationKey));
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Saving correlated object for '%s'", correlationKey));
        }

        objectStore.add(correlationKey, object);
    }

    @Override
    public T find(String correlationKey, long timeout) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Finding correlated object for '%s'", correlationKey));
        }

        return objectStore.remove(correlationKey);
    }

    @Override
    public void setObjectStore(ObjectStore<T> store) {
        this.objectStore = store;
    }

    @Override
    public ObjectStore<T> getObjectStore() {
        return this.objectStore;
    }

}
