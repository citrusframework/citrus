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

package com.consol.citrus.variable.dictionary;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.validation.interceptor.MessageConstructionInterceptor;

/**
 * Data dictionary interface describes a mechanism to modify message content (payload) with global dictionary elements.
 * Dictionary translates element values to those defined in dictionary. Message construction process is aware of dictionaries
 * in Spring application context so user just has to add dictionary implementation to application context.
 *
 * Dictionary takes part in message construction for inbound and outbound messages in Citrus.
 * @author Christoph Deppisch
 * @since 1.4
 */
public interface DataDictionary extends MessageConstructionInterceptor {

    /**
     * Translate value with given path in message content.
     * @param value current value
     * @param path the element path in message content
     * @param context the current test context
     * @return
     */
    String translate(String value, String path, TestContext context);

    /**
     * Sets the path mapping strategy.
     * @param strategy
     */
    void setPathMappingStrategy(PathMappingStrategy strategy);

    /**
     * Sets the dictionary scope.
     * @param scope
     */
    void setScope(DictionaryScope scope);

    /**
     * Gets the dictionary scope. If global scope dictionary will be applied to all messages. Inbound and outbound scopes
     * restrict dictionary usage to messages received or sent out by Citrus. Explicit scope means that tester explicitly
     * adds dictionary usage to a message processing in test case. Default scope is global.
     * @return
     */
    DictionaryScope getScope();

    /**
     * Dictionary scope marks range where dictionary should be applied. Globally, inbound or outbound messages.
     * Explicit scope means that dictionary will only be linked explicitly to a message by test case definition.
     */
    public static enum DictionaryScope {
        GLOBAL,
        INBOUND,
        OUTBOUND,
        EXPLICIT
    }

    /**
     * Possible mapping strategies for identifying matching dictionary items
     * with path comparison.
     */
    public static enum PathMappingStrategy {
        EXACT_MATCH,
        ENDS_WITH,
        STARTS_WITH;
    }
}
