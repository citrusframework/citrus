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

import java.util.List;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.spi.Resource;

public interface CamelCreateRouteActionBuilder<T extends TestAction, B extends TestActionBuilder<T>>
        extends CamelRouteActionBuilderBase<T, B> {

    /**
     * Adds route as a RouteBuilder or RouteDefinition using one of the supported languages XML or Groovy.
     */
    B route(Object o);

    /**
     * Adds route using one of the supported languages XML or Groovy.
     */
    @Deprecated
    default B routeContext(String routeSpec) {
        return route(routeSpec);
    }

    /**
     * Adds route using one of the supported languages XML or Groovy.
     */
    B route(String routeSpec);

    /**
     * Adds route using the content of the given resource.
     * The file name is used as a route id.
     */
    B route(Resource routeResource);

    /**
     * Adds route using one of the supported languages XML or Groovy.
     */
    B route(String routeId, String routeSpec);

    /**
     * Sets the route id.
     */
    B routeId(String id);

    /**
     * Adds route definitions.
     */
    B routes(List<?> routes);
}
