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
import jakarta.xml.bind.annotation.XmlRootElement;
import org.citrusframework.TestActor;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.NavigateAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.util.StringUtils;

/**
 * Navigates the current page backward, forward, or reloads it.
 */
@XmlRootElement(name = "navigate")
public class Navigate extends AbstractPlaywrightAction.Builder<NavigateAction, Navigate> {

    private final NavigateAction.Builder delegate = new NavigateAction.Builder();

    @XmlAttribute
    public void setDirection(String direction) {
        if (!StringUtils.hasText(direction)) {
            return;
        }
        switch (direction.trim().toLowerCase()) {
            case "back" -> delegate.back();
            case "forward" -> delegate.forward();
            case "reload" -> delegate.reload();
            default -> throw new IllegalArgumentException("Unsupported Playwright navigate direction: " + direction);
        }
    }

    @Override
    public Navigate description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Navigate actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Navigate browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public NavigateAction build() {
        return delegate.build();
    }
}
