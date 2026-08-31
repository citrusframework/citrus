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

package org.citrusframework.playwright.actions;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.model.LocatorSpec;
import org.citrusframework.playwright.model.PlaywrightTarget;

public abstract class ElementActionBuilder<T extends PlaywrightAction, B extends ElementActionBuilder<T, B>>
        extends AbstractPlaywrightAction.Builder<T, B> {

    protected LocatorSpec locator;

    /**
     * Uses a CSS selector as the action locator.
     *
     * @param css CSS selector
     * @return this builder
     */
    public B locator(String css) {
        this.locator = LocatorSpec.css(css);
        return self;
    }

    /**
     * Uses an explicit locator specification as the action locator.
     *
     * @param locator locator specification
     * @return this builder
     */
    public B locator(LocatorSpec locator) {
        this.locator = locator;
        return self;
    }

    /**
     * Uses a reusable labeled target as the action locator.
     *
     * @param target reusable Playwright target
     * @return this builder
     */
    public B locator(PlaywrightTarget target) {
        this.locator = target.toLocatorSpec();
        return self;
    }

    /**
     * Fails fast when an element action is built without a locator.
     */
    protected void requireLocator() {
        if (locator == null) {
            throw new CitrusRuntimeException("Missing Playwright locator - call locator(...) before building action");
        }
    }
}
