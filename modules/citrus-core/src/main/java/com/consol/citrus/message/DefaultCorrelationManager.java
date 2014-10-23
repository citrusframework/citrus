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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class DefaultCorrelationManager<T> implements CorrelationManager<T> {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(DefaultCorrelationManager.class);

    /** Map of managed objects */
    private Map<String, T> objectStore = new ConcurrentHashMap<String, T>();

    @Override
    public void store(String correlationKey, T object) {
        if (object == null) {
            log.warn(String.format("Ignore correlated null object for '%s'", correlationKey));
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Saving correlated object for '%s'", correlationKey));
        }

        objectStore.put(correlationKey, object);
    }

    @Override
    public T find(String correlationKey) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Finding correlated object for '%s'", correlationKey));
        }

        return objectStore.remove(correlationKey);
    }

}
