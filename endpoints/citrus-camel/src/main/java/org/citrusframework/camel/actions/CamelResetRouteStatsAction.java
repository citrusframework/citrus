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

package org.citrusframework.camel.actions;

import org.apache.camel.api.management.ManagedCamelContext;
import org.apache.camel.api.management.mbean.ManagedRouteMBean;
import org.citrusframework.api.actions.camel.CamelResetRouteStatsActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CamelResetRouteStatsAction extends AbstractCamelRouteAction {

    private static final Logger logger = LoggerFactory.getLogger(CamelResetRouteStatsAction.class);

    public CamelResetRouteStatsAction(Builder builder) {
        super("reset-route-stats", builder);
    }

    @Override
    public void doExecute(TestContext context) {
        String resolvedRouteId = context.replaceDynamicContentInString(getRouteId());

        ManagedCamelContext managedContext = camelContext.getCamelContextExtension()
                .getContextPlugin(ManagedCamelContext.class);

        if (managedContext == null) {
            throw new CitrusRuntimeException(
                    "Failed to get managed Camel context extension - make sure camel-management is on the classpath");
        }

        ManagedRouteMBean routeMBean = managedContext.getManagedRoute(resolvedRouteId);

        if (routeMBean == null) {
            throw new CitrusRuntimeException(
                    "Failed to get managed route statistics for routeId '%s'"
                            .formatted(resolvedRouteId));
        }

        try {
            routeMBean.reset(true);
            logger.info("Reset route '{}' statistics", resolvedRouteId);
        } catch (Exception e) {
            throw new CitrusRuntimeException(
                    "Failed to reset route statistics for routeId '%s'"
                            .formatted(resolvedRouteId), e);
        }
    }

    public static final class Builder extends AbstractCamelRouteAction.Builder<CamelResetRouteStatsAction, Builder>
            implements CamelResetRouteStatsActionBuilder<CamelResetRouteStatsAction, Builder> {

        @Override
        public CamelResetRouteStatsAction doBuild() {
            return new CamelResetRouteStatsAction(this);
        }
    }
}
