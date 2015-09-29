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

import com.consol.citrus.actions.AbstractTestAction;
import org.apache.camel.CamelContext;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public abstract class AbstractCamelRouteAction extends AbstractTestAction {

    /** Target Camel context */
    protected CamelContext camelContext;

    /**
     * Sets the target Camel context.
     * @param camelContext
     */
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    /**
     * Gets the target camel context.
     * @return
     */
    public CamelContext getCamelContext() {
        return camelContext;
    }
}
