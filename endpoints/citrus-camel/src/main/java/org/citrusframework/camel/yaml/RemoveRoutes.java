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

import java.util.List;

import org.citrusframework.camel.actions.RemoveCamelRouteAction;
import org.citrusframework.yaml.SchemaProperty;

public class RemoveRoutes implements CamelActionBuilderWrapper<RemoveCamelRouteAction.Builder> {
    private final RemoveCamelRouteAction.Builder builder = new RemoveCamelRouteAction.Builder();

    @SchemaProperty(description = "The Camel route ids to remove.")
    public void setRoutes(List<String> routeIds) {
        builder.routeIds(routeIds);
    }

    @Override
    public RemoveCamelRouteAction.Builder getBuilder() {
        return builder;
    }
}
