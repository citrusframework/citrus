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
import com.microsoft.playwright.options.HarContentPolicy;
import com.microsoft.playwright.options.HarMode;

import java.nio.file.Files;
import java.nio.file.Path;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.HarPolicies;
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
        STOP,
        START_HAR,
        STOP_HAR
    }

    private final Command command;
    private final Boolean screenshots;
    private final Boolean snapshots;
    private final Boolean sources;
    private final Boolean ariaSnapshots;
    private final Boolean screenSnapshots;
    private final String path;
    private final String variable;
    private final String harPath;
    private final HarContentPolicy harContent;
    private final HarMode harMode;
    private final String harUrlFilter;

    public TracingAction(Builder builder) {
        super("tracing", builder);
        this.command = builder.command;
        this.screenshots = builder.screenshots;
        this.snapshots = builder.snapshots;
        this.sources = builder.sources;
        this.ariaSnapshots = builder.ariaSnapshots;
        this.screenSnapshots = builder.screenSnapshots;
        this.path = builder.path;
        this.variable = builder.variable;
        this.harPath = builder.harPath;
        this.harContent = builder.harContent;
        this.harMode = builder.harMode;
        this.harUrlFilter = builder.harUrlFilter;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        if (command == Command.START_HAR) {
            Path target = Path.of(LocatorResolver.resolve(harPath, context));
            createParentDirectory(target);

            Tracing.StartHarOptions harOptions = new Tracing.StartHarOptions().setContent(harContent);
            if (harMode != null) {
                harOptions.setMode(harMode);
            }
            if (harUrlFilter != null) {
                harOptions.setUrlFilter(LocatorResolver.resolve(harUrlFilter, context));
            }

            browser.getCurrentContext().tracing().startHar(target, harOptions);

            if (variable != null) {
                context.setVariable(variable, target.toString());
            }
            return;
        }

        if (command == Command.STOP_HAR) {
            browser.getCurrentContext().tracing().stopHar();
            return;
        }

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
            if (ariaSnapshots != null) {
                options.setAriaSnapshots(ariaSnapshots);
            }
            if (screenSnapshots != null) {
                options.setScreenSnapshots(screenSnapshots);
            }
            browser.getCurrentContext().tracing().start(options);
            return;
        }

        Path tracePath = path == null
                ? browser.getEndpointConfiguration().getArtifactDirectory().resolve("trace.zip")
                : Path.of(LocatorResolver.resolve(path, context));
        createParentDirectory(tracePath);
        browser.getCurrentContext().tracing().stop(new Tracing.StopOptions().setPath(tracePath));
        if (variable != null) {
            context.setVariable(variable, tracePath.toString());
        }
    }

    private void createParentDirectory(Path target) {
        if (target.getParent() == null) {
            return;
        }

        try {
            Files.createDirectories(target.getParent());
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to create Playwright trace directory: " + target.getParent(), e);
        }
    }

    public Command getCommand() {
        return command;
    }

    public Boolean getScreenshots() {
        return screenshots;
    }

    public Boolean getSnapshots() {
        return snapshots;
    }

    public Boolean getSources() {
        return sources;
    }

    public Boolean getAriaSnapshots() {
        return ariaSnapshots;
    }

    public Boolean getScreenSnapshots() {
        return screenSnapshots;
    }

    public String getPath() {
        return path;
    }

    public String getVariable() {
        return variable;
    }

    public String getHarPath() {
        return harPath;
    }

    public HarContentPolicy getHarContent() {
        return harContent;
    }

    public HarMode getHarMode() {
        return harMode;
    }

    public String getHarUrlFilter() {
        return harUrlFilter;
    }

    /**
     * Fluent builder for Playwright tracing commands.
     */
    public static class Builder extends AbstractPlaywrightAction.Builder<TracingAction, Builder> {
        private Command command;
        private Boolean screenshots;
        private Boolean snapshots;
        private Boolean sources;
        private Boolean ariaSnapshots;
        private Boolean screenSnapshots;
        private String path;
        private String variable;
        private String harPath;
        private HarContentPolicy harContent = HarContentPolicy.OMIT;
        private HarMode harMode;
        private String harUrlFilter;

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
         * Records an aria snapshot of the page on every action. Off by default because it adds
         * a capture to each step.
         *
         * @param ariaSnapshots true to record aria snapshots
         * @return this builder
         */
        public Builder ariaSnapshots(boolean ariaSnapshots) {
            this.ariaSnapshots = ariaSnapshots;
            return this;
        }

        /**
         * Records a screenshot of the page on every action. Off by default because it adds a
         * capture to each step.
         *
         * @param screenSnapshots true to record screen snapshots
         * @return this builder
         */
        public Builder screenSnapshots(boolean screenSnapshots) {
            this.screenSnapshots = screenSnapshots;
            return this;
        }

        /**
         * Starts HAR recording into the given file. Response bodies are omitted by default so
         * that secrets are not written to the artifact - call {@link #harContent(String)} to
         * change that deliberately.
         *
         * @param harPath target HAR file
         * @return this builder
         */
        public Builder startHar(String harPath) {
            this.command = Command.START_HAR;
            this.harPath = harPath;
            return this;
        }

        /**
         * Stops an active HAR recording.
         *
         * @return this builder
         */
        public Builder stopHar() {
            this.command = Command.STOP_HAR;
            return this;
        }

        /**
         * Controls whether response bodies are stored in the HAR file.
         *
         * @param content one of omit, embed or attach
         * @return this builder
         */
        public Builder harContent(String content) {
            this.harContent = HarPolicies.content(content);
            return this;
        }

        /**
         * Controls how much detail is recorded in the HAR file.
         *
         * @param mode one of full or minimal
         * @return this builder
         */
        public Builder harMode(String mode) {
            this.harMode = HarPolicies.mode(mode);
            return this;
        }

        /**
         * Restricts HAR recording to requests matching the given glob pattern.
         *
         * @param urlFilter glob pattern
         * @return this builder
         */
        public Builder harUrlFilter(String urlFilter) {
            this.harUrlFilter = urlFilter;
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
            if (command == Command.START_HAR && (harPath == null || harPath.isBlank())) {
                throw new CitrusRuntimeException("Missing Playwright HAR file path - call startHar(...) with a target file");
            }
            return new TracingAction(this);
        }
    }
}
