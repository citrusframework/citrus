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

import org.citrusframework.camel.actions.CamelVerifyRouteStatsAction;
import org.citrusframework.api.yaml.SchemaProperty;

public class VerifyRouteStats implements CamelActionBuilderWrapper<CamelVerifyRouteStatsAction.Builder> {

    private final CamelVerifyRouteStatsAction.Builder builder = new CamelVerifyRouteStatsAction.Builder();

    @SchemaProperty(description = "The Camel route id to verify statistics for.")
    public void setRoute(String routeId) {
        builder.route(routeId);
    }

    @SchemaProperty(description = "The expected number of completed exchanges.")
    public void setCompleted(long completed) {
        builder.completed(completed);
    }

    @SchemaProperty(description = "The expected number of failed exchanges.")
    public void setFailed(long failed) {
        builder.failed(failed);
    }

    @SchemaProperty(description = "Expected route statistics as JSON for comparison with the actual stats dump.")
    public void setStats(String stats) {
        builder.stats(stats);
    }

    @Override
    public CamelVerifyRouteStatsAction.Builder getBuilder() {
        return builder;
    }
}
