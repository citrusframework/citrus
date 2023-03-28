/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.variable.dictionary;

import org.citrusframework.Scoped;
import org.citrusframework.common.InitializingPhase;
import org.citrusframework.context.TestContext;
import org.citrusframework.message.MessageDirectionAware;
import org.citrusframework.message.MessageProcessor;

/**
 * Data dictionary interface describes a mechanism to modify message content (payload) with global dictionary elements.
 * Dictionary translates element values to those defined in dictionary. Message construction process is aware of dictionaries
 * in Spring application context so user just has to add dictionary implementation to application context.
 *
 * Dictionary takes part in message construction for inbound and outbound messages in Citrus.
 * @author Christoph Deppisch
 * @since 1.4
 */
public interface DataDictionary<T> extends MessageProcessor, MessageDirectionAware, Scoped, InitializingPhase {

    /**
     * Translate value with given path in message content.
     * @param value current value
     * @param key the key element in message content
     * @param context the current test context
     * @return
     */
    <R> R translate(T key, R value, TestContext context);

    /**
     * Gets the data dictionary name.
     * @return
     */
    String getName();

    /**
     * Gets the path mapping strategy.
     */
    PathMappingStrategy getPathMappingStrategy();

    /**
     * Possible mapping strategies for identifying matching dictionary items
     * with path comparison.
     */
    enum PathMappingStrategy {
        EXACT,
        ENDS_WITH,
        STARTS_WITH;
    }
}
