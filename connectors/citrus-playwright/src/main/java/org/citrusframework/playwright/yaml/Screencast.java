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

import org.citrusframework.TestActor;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.ScreencastAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;

/**
 * Records and annotates a screencast of the current page.
 *
 * <p>The real-time frame callback of the Java DSL has no declarative equivalent and is
 * deliberately not exposed here.</p>
 */
public class Screencast extends AbstractPlaywrightAction.Builder<ScreencastAction, Screencast> {

    private final ScreencastAction.Builder delegate = new ScreencastAction.Builder();

    private String path;
    private String title;

    @SchemaProperty
    public void setPath(String path) {
        this.path = path;
    }

    @SchemaProperty
    public void setTitle(String title) {
        this.title = title;
    }

    @SchemaProperty
    public void setCommand(String command) {
        switch (command.trim().toLowerCase(java.util.Locale.ENGLISH).replace('_', '-')) {
            case "start" -> delegate.start(path);
            case "stop" -> delegate.stop();
            case "show-actions" -> delegate.showActions();
            case "hide-actions" -> delegate.hideActions();
            case "show-chapter" -> delegate.showChapter(title);
            default -> throw new CitrusRuntimeException("Unsupported Playwright screencast command: " + command);
        }
    }

    @SchemaProperty
    public void setChapterDescription(String description) {
        delegate.chapterDescription(description);
    }

    @SchemaProperty
    public void setPosition(String position) {
        delegate.position(position);
    }

    @SchemaProperty
    public void setFontSize(Integer fontSize) {
        delegate.fontSize(fontSize);
    }

    @SchemaProperty
    public void setDuration(Double duration) {
        delegate.duration(duration);
    }

    @SchemaProperty
    public void setQuality(Integer quality) {
        delegate.quality(quality);
    }

    @SchemaProperty
    public void setVariable(String variable) {
        delegate.variable(variable);
    }

    @Override
    public Screencast description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Screencast actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Screencast browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public ScreencastAction build() {
        return delegate.build();
    }
}
