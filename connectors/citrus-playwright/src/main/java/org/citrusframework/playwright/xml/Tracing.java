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
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.TracingAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.DslCommands;

/**
 * Starts or stops Playwright tracing and HAR recording.
 */
@XmlRootElement(name = "tracing")
public class Tracing extends AbstractPlaywrightAction.Builder<TracingAction, Tracing> {

    private final TracingAction.Builder delegate = new TracingAction.Builder();

    private String command;
    private String path;

    @XmlAttribute
    public void setCommand(String command) {
        this.command = command;
    }

    @XmlAttribute
    public void setPath(String path) {
        this.path = path;
    }

    @XmlAttribute
    public void setScreenshots(Boolean screenshots) {
        delegate.screenshots(screenshots);
    }

    @XmlAttribute
    public void setSnapshots(Boolean snapshots) {
        delegate.snapshots(snapshots);
    }

    @XmlAttribute
    public void setSources(Boolean sources) {
        delegate.sources(sources);
    }

    @XmlAttribute(name = "aria-snapshots")
    public void setAriaSnapshots(Boolean ariaSnapshots) {
        delegate.ariaSnapshots(ariaSnapshots);
    }

    @XmlAttribute(name = "screen-snapshots")
    public void setScreenSnapshots(Boolean screenSnapshots) {
        delegate.screenSnapshots(screenSnapshots);
    }

    @XmlAttribute(name = "har-content")
    public void setHarContent(String harContent) {
        delegate.harContent(harContent);
    }

    @XmlAttribute(name = "har-mode")
    public void setHarMode(String harMode) {
        delegate.harMode(harMode);
    }

    @XmlAttribute(name = "har-url-filter")
    public void setHarUrlFilter(String harUrlFilter) {
        delegate.harUrlFilter(harUrlFilter);
    }

    @XmlAttribute
    public void setVariable(String variable) {
        delegate.variable(variable);
    }

    @Override
    public Tracing description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Tracing actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Tracing browser(PlaywrightBrowser browser) {
        delegate.browser(browser);
        return this;
    }

    @Override
    public TracingAction build() {
        switch (DslCommands.normalize(command)) {
            case "start" -> {
                if (path != null) {
                    throw new CitrusRuntimeException("Playwright tracing path applies to stop or start-har, not start");
                }
                delegate.start();
            }
            case "stop" -> {
                delegate.stop();
                if (path != null) {
                    delegate.path(path);
                }
            }
            case "start-har" -> delegate.startHar(path);
            case "stop-har" -> delegate.stopHar();
            default -> throw DslCommands.unsupported("tracing", command);
        }

        return delegate.build();
    }
}
