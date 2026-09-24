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

import java.util.Objects;
import java.util.stream.Stream;

import org.citrusframework.TestActor;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.PageAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.DslCommands;

/**
 * Opens, switches or closes a page (tab) of the current browser context.
 */
public class Page extends AbstractPlaywrightAction.Builder<PageAction, Page> {

    private final PageAction.Builder delegate = new PageAction.Builder();

    private String command;
    private String alias;
    private Integer index;
    private String title;
    private String urlContains;

    @SchemaProperty
    public void setCommand(String command) {
        this.command = command;
    }

    @SchemaProperty
    public void setAlias(String alias) {
        this.alias = alias;
    }

    @SchemaProperty
    public void setContext(String contextAlias) {
        delegate.inContext(contextAlias);
    }

    @SchemaProperty
    public void setIndex(Integer index) {
        this.index = index;
    }

    @SchemaProperty
    public void setTitle(String title) {
        this.title = title;
    }

    @SchemaProperty
    public void setUrlContains(String urlContains) {
        this.urlContains = urlContains;
    }

    @Override
    public Page description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Page actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Page browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public PageAction build() {
        switch (DslCommands.normalize(command)) {
            case "create" -> delegate.newPage(alias);
            case "switch" -> {
                long selectors = Stream.of(alias, index, title, urlContains).filter(Objects::nonNull).count();
                if (selectors == 0) {
                    throw new CitrusRuntimeException("Missing Playwright page switch selector - use one of alias, index, title or url-contains");
                }
                if (selectors > 1) {
                    throw new CitrusRuntimeException("Ambiguous Playwright page switch - use only one of alias, index, title or url-contains");
                }
                if (index != null) {
                    delegate.switchToIndex(index);
                } else if (title != null) {
                    delegate.switchToTitle(title);
                } else if (urlContains != null) {
                    delegate.switchToUrlContaining(urlContains);
                } else {
                    delegate.switchTo(alias);
                }
            }
            case "close" -> delegate.close(alias);
            default -> throw DslCommands.unsupported("page", command);
        }

        return delegate.build();
    }
}
