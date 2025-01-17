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

package org.citrusframework.camel.yaml;

import org.citrusframework.camel.actions.CreateCamelRouteAction;
import org.citrusframework.spi.Resources;

public class CreateRoutes implements CamelActionBuilderWrapper<CreateCamelRouteAction.Builder> {
    private final CreateCamelRouteAction.Builder builder = new CreateCamelRouteAction.Builder();

    public void setRouteContext(String routeContext) {
        builder.route(routeContext);
    }

    public void setFile(String file) {
        builder.route(Resources.create(file));
    }

    public void setRoute(String routeSpec) {
        builder.route(routeSpec);
    }

    public void setId(String routeId) {
        builder.routeId(routeId);
    }

    @Override
    public CreateCamelRouteAction.Builder getBuilder() {
        return builder;
    }
}
