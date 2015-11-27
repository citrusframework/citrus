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

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class StartCamelRouteAction extends AbstractCamelRouteAction {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(StartCamelRouteAction.class);

    /**
     * Default constructor.
     */
    public StartCamelRouteAction() {
        setName("start-routes");
    }

    @Override
    public void doExecute(TestContext context) {
        try {
            for (String routeId : routeIds) {
                camelContext.startRoute(routeId);
                log.info(String.format("Started Camel route '%s' on context '%s'", routeId, camelContext.getName()));
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to start Camel routes", e);
        }
    }
}
