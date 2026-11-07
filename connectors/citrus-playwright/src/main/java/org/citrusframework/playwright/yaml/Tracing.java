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

import org.citrusframework.TestActor;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.actions.AbstractPlaywrightAction;
import org.citrusframework.playwright.actions.TracingAction;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.DslCommands;

/**
 * Starts or stops Playwright tracing and HAR recording.
 */
public class Tracing extends AbstractPlaywrightAction.Builder<TracingAction, Tracing> {

    private final TracingAction.Builder delegate = new TracingAction.Builder();

    private String command;
    private String path;

    @SchemaProperty(description = "Tracing command to execute.")
    public void setCommand(String command) {
        this.command = command;
    }

    @SchemaProperty(description = "File path for trace output.")
    public void setPath(String path) {
        this.path = path;
    }

    @SchemaProperty(description = "Includes screenshots in the trace.")
    public void setScreenshots(Boolean screenshots) {
        delegate.screenshots(screenshots);
    }

    @SchemaProperty(description = "Includes DOM snapshots in the trace.")
    public void setSnapshots(Boolean snapshots) {
        delegate.snapshots(snapshots);
    }

    @SchemaProperty(description = "Includes source files in the trace.")
    public void setSources(Boolean sources) {
        delegate.sources(sources);
    }

    @SchemaProperty(description = "Includes ARIA snapshots in the trace.")
    public void setAriaSnapshots(Boolean ariaSnapshots) {
        delegate.ariaSnapshots(ariaSnapshots);
    }

    @SchemaProperty(description = "Includes screen snapshots in the trace.")
    public void setScreenSnapshots(Boolean screenSnapshots) {
        delegate.screenSnapshots(screenSnapshots);
    }

    @SchemaProperty(description = "HAR content mode for tracing.")
    public void setHarContent(String harContent) {
        delegate.harContent(harContent);
    }

    @SchemaProperty(description = "HAR recording mode for tracing.")
    public void setHarMode(String harMode) {
        delegate.harMode(harMode);
    }

    @SchemaProperty(description = "URL filter for HAR recording.")
    public void setHarUrlFilter(String harUrlFilter) {
        delegate.harUrlFilter(harUrlFilter);
    }

    @SchemaProperty(description = "Test variable to store tracing output.")
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
