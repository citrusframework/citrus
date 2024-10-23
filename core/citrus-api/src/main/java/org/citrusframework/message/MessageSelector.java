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

package org.citrusframework.message;

import org.citrusframework.context.TestContext;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.citrusframework.spi.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@FunctionalInterface
public interface MessageSelector {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(MessageSelector.class);

    /** Message selector resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/message/selector";

    /** Type resolver to find custom message selectors on classpath via resource path lookup */
    TypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    Map<String, MessageSelectorFactory> factories = new ConcurrentHashMap<>();

    /**
     * Resolves all available selectors from resource path lookup. Scans classpath for validator meta information
     * and instantiates those selectors.
     *
     * @return
     */
    static Map<String, MessageSelectorFactory> lookup() {
        if (factories.isEmpty()) {
            factories.putAll(TYPE_RESOLVER.resolveAll());

            if (logger.isDebugEnabled()) {
                factories.forEach((k, v) -> logger.debug("Found message selector '{}' as {}", k, v.getClass()));
            }
        }

        return factories;
    }

    /**
     * Checks weather this selector should accept given message or not. When accepting the message the
     * selective consumer is provided with the message otherwise the message is skipped for this consumer.
     *
     * @param message the message to check
     * @return true if the message will be accepted, false otherwise.
     */
    boolean accept(Message message);

    /**
     * Special message selector accepts all messages on queue.
     */
    final class AllAcceptingMessageSelector implements MessageSelector {

        public boolean accept(Message message) {
            return true;
        }
    }

    /**
     * Factory capable of creating a message selector from key value pairs.
     */
    interface MessageSelectorFactory {

        /**
         * Check if this factory is able to create a message selector for given key.
         *
         * @param key selector key
         * @return true if the factory accepts the key, false otherwise.
         */
        boolean supports(String key);

        /**
         * Create new message selector for given predicates.
         *
         * @param key selector key
         * @param value selector value
         * @param context test context
         *
         * @return the created selector
         */
        MessageSelector create(String key, String value, TestContext context);
    }
}
