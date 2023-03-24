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

package org.citrusframework.message;

/**
 * Enumeration representing the different error handling strategies in synchronous communication
 * with client server interaction where the client receives an error message as response.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public enum ErrorHandlingStrategy {
    THROWS_EXCEPTION("throwsException"),
    PROPAGATE("propagateError");

    /** Name representation */
    private String name;

    /**
     * Default constructor using String name representation field.
     * @param name
     */
    ErrorHandlingStrategy(String name) {
        this.name = name;
    }

    /**
     * Gets the strategy from given name representation.
     * @param name
     * @return
     */
    public static ErrorHandlingStrategy fromName(String name) {
        for (ErrorHandlingStrategy strategy : values()) {
            if (strategy.getName().equals(name)) {
                return strategy;
            }
        }

        throw new IllegalArgumentException("Unknown error handling strategy: " + name);
    }

    /**
     * Gets the name representation.
     * @return the name
     */
    public String getName() {
        return name;
    }
}
