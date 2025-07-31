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
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReferenceResolverAwareBuilder;

public interface CamelRouteActionBuilder<T extends TestAction, B extends TestActionBuilder<T>>
        extends ReferenceResolverAwareBuilder<T, B>, TestActionBuilder<T> {

    CamelRouteActionBuilder<T, B> context(String camelContext);

    CamelRouteActionBuilder<T, B> context(Object o);

    /**
     * Creates new Camel routes.
     */
    <S extends CamelCreateRouteActionBuilder<T, S>> S create();

    /**
     * Creates new Camel routes in route builder.
     */
    CamelCreateRouteActionBuilder<?, ?> create(Object routeBuilder);

    /**
     * Creates new Camel routes from route specification using one of the supported languages.
     */
    CamelCreateRouteActionBuilder<?, ?> create(String routeSpec);

    /**
     * Execute control bus Camel operations.
     */
    CamelControlBusActionBuilder<?, ?> controlBus();

    /**
     * Start these Camel routes.
     */
    CamelStartRouteActionBuilder<?, ?> start(String... routes);

    /**
     * Stop these Camel routes.
     */
    CamelStopRouteActionBuilder<?, ?> stop(String... routes);

    /**
     * Remove these Camel routes.
     */
    CamelRemoveRouteActionBuilder<?, ?> remove(String... routes);
}
