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

import com.microsoft.playwright.Tracing;

import java.nio.file.Files;
import java.nio.file.Path;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.LocatorResolver;

/**
 * Citrus action for starting and stopping Playwright tracing on the current context.
 *
 * <p>Trace stop writes the archive to disk and should be called only after a
 * matching start command has enabled tracing on the same context.</p>
 */
public class TracingAction extends AbstractPlaywrightAction {

    public enum Command {
        START,
        STOP
    }

    private final Command command;
    private final Boolean screenshots;
    private final Boolean snapshots;
    private final Boolean sources;
    private final String path;
    private final String variable;

    public TracingAction(Builder builder) {
        super("tracing", builder);
        this.command = builder.command;
        this.screenshots = builder.screenshots;
        this.snapshots = builder.snapshots;
        this.sources = builder.sources;
        this.path = builder.path;
        this.variable = builder.variable;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        if (command == Command.START) {
            Tracing.StartOptions options = new Tracing.StartOptions();
            if (screenshots != null) {
                options.setScreenshots(screenshots);
            }
            if (snapshots != null) {
                options.setSnapshots(snapshots);
            }
            if (sources != null) {
                options.setSources(sources);
            }
            browser.getCurrentContext().tracing().start(options);
            return;
        }

        Path tracePath = path == null
                ? browser.getEndpointConfiguration().getArtifactDirectory().resolve("trace.zip")
                : Path.of(LocatorResolver.resolve(path, context));
        try {
            if (tracePath.getParent() != null) {
                Files.createDirectories(tracePath.getParent());
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to create Playwright trace directory: " + tracePath.getParent(), e);
        }
        browser.getCurrentContext().tracing().stop(new Tracing.StopOptions().setPath(tracePath));
        if (variable != null) {
            context.setVariable(variable, tracePath.toString());
        }
    }

    /**
     * Fluent builder for Playwright tracing commands.
     */
    public static class Builder extends AbstractPlaywrightAction.Builder<TracingAction, Builder> {
        private Command command;
        private Boolean screenshots;
        private Boolean snapshots;
        private Boolean sources;
        private String path;
        private String variable;

        /**
         * Starts tracing on the current context.
         *
         * @return this builder
         */
        public Builder start() {
            this.command = Command.START;
            return this;
        }

        /**
         * Stops tracing and writes the trace archive.
         *
         * @return this builder
         */
        public Builder stop() {
            this.command = Command.STOP;
            return this;
        }

        /**
         * Configures screenshot capture in the trace.
         *
         * @param screenshots true to include screenshots
         * @return this builder
         */
        public Builder screenshots(boolean screenshots) {
            this.screenshots = screenshots;
            return this;
        }

        /**
         * Configures DOM snapshot capture in the trace.
         *
         * @param snapshots true to include snapshots
         * @return this builder
         */
        public Builder snapshots(boolean snapshots) {
            this.snapshots = snapshots;
            return this;
        }

        /**
         * Configures source capture in the trace.
         *
         * @param sources true to include sources
         * @return this builder
         */
        public Builder sources(boolean sources) {
            this.sources = sources;
            return this;
        }

        /**
         * Sets the trace archive output path.
         *
         * @param path trace ZIP path
         * @return this builder
         */
        public Builder path(String path) {
            this.path = path;
            return this;
        }

        /**
         * Stores the trace archive path in a Citrus variable.
         *
         * @param variable variable name
         * @return this builder
         */
        public Builder variable(String variable) {
            this.variable = variable;
            return this;
        }

        @Override
        public TracingAction build() {
            if (command == null) {
                throw new CitrusRuntimeException("Missing Playwright tracing command");
            }
            return new TracingAction(this);
        }
    }
}
