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

package org.citrusframework.playwright.yaml;

import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.playwright.model.LocatorSpec;

/**
 * Locator element model shared by all element aware Playwright YAML actions.
 */
public class Element extends ElementLocator {

    private ElementLocator within;

    @SchemaProperty(description = "Enclosing locator specification narrowing matches via Playwright's within() composition.")
    public void setWithin(ElementLocator within) {
        this.within = within;
    }

    public ElementLocator getWithin() {
        return within;
    }

    @Override
    public LocatorSpec toLocatorSpec() {
        LocatorSpec spec = super.toLocatorSpec();
        if (within != null) {
            spec.within(within.toLocatorSpec());
        }
        return spec;
    }
}
