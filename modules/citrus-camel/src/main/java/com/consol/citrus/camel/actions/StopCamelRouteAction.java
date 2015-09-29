/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.camel.actions;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class StopCamelRouteAction extends AbstractCamelRouteAction {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(StopCamelRouteAction.class);

    /** The Camel route to start */
    private List<String> routeIds = new ArrayList<>();

    /**
     * Default constructor.
     */
    public StopCamelRouteAction() {
        setName("stop-routes");
    }

    @Override
    public void doExecute(TestContext context) {
        try {
            for (String routeId : routeIds) {
                log.info(String.format("Stopping Camel route '%s' on context '%s'", routeId, camelContext.getName()));
                camelContext.stopRoute(routeId);
                log.info(String.format("Successfully stopped Camel route '%s'", routeId));
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to stop Camel routes", e);
        }
    }

    /**
     * Sets the Camel routes to stop.
     * @param routeIds
     */
    public void setRouteIds(List<String> routeIds) {
        this.routeIds = routeIds;
    }

    /**
     * Gets the Camel routes to stop.
     * @return
     */
    public List<String> getRouteIds() {
        return routeIds;
    }
}
