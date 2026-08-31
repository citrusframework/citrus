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

package org.citrusframework.playwright.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.citrusframework.TestActor;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.WaitForAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.util.StringUtils;

/**
 * Waits for a locator or page load state condition.
 */
@XmlRootElement(name = "wait")
public class Wait extends AbstractPlaywrightAction.Builder<WaitForAction, Wait> {

    private final WaitForAction.Builder delegate = new WaitForAction.Builder();

    @XmlAttribute(name = "condition")
    public void setCondition(String condition) {
        if (!StringUtils.hasText(condition)) {
            return;
        }
        switch (condition.trim().toLowerCase().replace('-', '_')) {
            case "visible" -> delegate.visible();
            case "hidden" -> delegate.hidden();
            case "attached" -> delegate.attached();
            case "detached" -> delegate.detached();
            case "load" -> delegate.load();
            case "dom_content_loaded" -> delegate.domContentLoaded();
            case "network_idle" -> delegate.networkIdle();
            default -> throw new IllegalArgumentException("Unsupported Playwright wait condition: " + condition);
        }
    }

    @XmlElement
    public void setElement(Element element) {
        delegate.locator(element.toLocatorSpec());
    }

    @Override
    public Wait description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Wait actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Wait browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public WaitForAction build() {
        return delegate.build();
    }
}
