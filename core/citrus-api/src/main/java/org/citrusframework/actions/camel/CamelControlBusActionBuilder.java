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

package org.citrusframework.actions.camel;

import org.citrusframework.TestAction;

public interface CamelControlBusActionBuilder<T extends TestAction, B extends CamelControlBusActionBuilder<T, B>>
        extends CamelRouteActionBuilderBase<T, B> {

    /**
     * Sets route action to execute.
     */
    CamelControlBusRouteActionBuilder<T, B> route(String id);

    /**
     * Sets route action to execute.
     */
    B route(String id, String action);

    /**
     * Sets a simple language expression to execute.
     */
    B simple(String expression);

    /**
     * Sets a language expression to execute.
     */
    B language(String language, String expression);

    /**
     * Sets the expected result.
     */
    B result(Enum<?> status);

    /**
     * Sets the expected result.
     */
    B result(String result);

    interface CamelControlBusRouteActionBuilder <T extends TestAction, B extends CamelControlBusActionBuilder<T, B>> {

        /**
         * Performs generic action on the given route.
         */
        B action(String action);

        /**
         * Start given route.
         */
        B start();

        /**
         * Stop given route.
         */
        B stop();

        /**
         * Retrieve status of given route.
         */
        B status();
    }
}
