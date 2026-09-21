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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.function.Consumer;

import com.microsoft.playwright.Screencast;
import com.microsoft.playwright.ScreencastFrame;
import com.microsoft.playwright.options.AnnotatePosition;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.LocatorResolver;

/**
 * Records the current page as a video and annotates the recording.
 *
 * <p>Recording is never implicit - it starts only when this action starts it, and the artifact is
 * written to the path the test chooses. Action annotations and chapter overlays make the
 * recording readable as a walkthrough of what the test did.</p>
 *
 * <p>Frames can carry whatever the page displays, so the real-time {@code onFrame} callback is
 * Java DSL only and its data is never written to logs or failure evidence by this action.</p>
 */
public class ScreencastAction extends AbstractPlaywrightAction {

    public enum Command {
        START,
        STOP,
        SHOW_ACTIONS,
        HIDE_ACTIONS,
        SHOW_CHAPTER
    }

    private final Command command;
    private final String path;
    private final Integer quality;
    private final Consumer<ScreencastFrame> onFrame;
    private final AnnotatePosition position;
    private final Integer fontSize;
    private final Double duration;
    private final String chapterTitle;
    private final String description;
    private final String variable;

    public ScreencastAction(Builder builder) {
        super("screencast", builder);
        this.command = builder.command;
        this.path = builder.path;
        this.quality = builder.quality;
        this.onFrame = builder.onFrame;
        this.position = builder.position;
        this.fontSize = builder.fontSize;
        this.duration = builder.duration;
        this.chapterTitle = builder.chapterTitle;
        this.description = builder.description;
        this.variable = builder.variable;
    }

    public Command getCommand() {
        return command;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        Screencast screencast = browser.getCurrentPage().screencast();

        switch (command) {
            case START -> startRecording(screencast, context);
            case STOP -> screencast.stop();
            case SHOW_ACTIONS -> screencast.showActions(showActionsOptions());
            case HIDE_ACTIONS -> screencast.hideActions();
            case SHOW_CHAPTER -> screencast.showChapter(
                    LocatorResolver.resolve(chapterTitle, context), showChapterOptions(context));
        }
    }

    private void startRecording(Screencast screencast, TestContext context) {
        Path target = Path.of(LocatorResolver.resolve(path, context));
        createParentDirectory(target);

        Screencast.StartOptions options = new Screencast.StartOptions().setPath(target);
        if (quality != null) {
            options.setQuality(quality);
        }
        if (onFrame != null) {
            options.setOnFrame(onFrame);
        }

        screencast.start(options);

        if (variable != null) {
            context.setVariable(variable, target.toString());
        }
    }

    private Screencast.ShowActionsOptions showActionsOptions() {
        Screencast.ShowActionsOptions options = new Screencast.ShowActionsOptions();

        if (position != null) {
            options.setPosition(position);
        }
        if (fontSize != null) {
            options.setFontSize(fontSize);
        }
        if (duration != null) {
            options.setDuration(duration);
        }

        return options;
    }

    private Screencast.ShowChapterOptions showChapterOptions(TestContext context) {
        Screencast.ShowChapterOptions options = new Screencast.ShowChapterOptions();

        if (description != null) {
            options.setDescription(LocatorResolver.resolve(description, context));
        }
        if (duration != null) {
            options.setDuration(duration);
        }

        return options;
    }

    private void createParentDirectory(Path target) {
        if (target.getParent() == null) {
            return;
        }

        try {
            Files.createDirectories(target.getParent());
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to create Playwright screencast directory: " + target.getParent(), e);
        }
    }

    /**
     * Resolves the DSL annotation position names onto the Playwright enum.
     *
     * @param position position name such as {@code top-right}
     * @return matching annotation position
     */
    static AnnotatePosition annotatePosition(String position) {
        try {
            return AnnotatePosition.valueOf(position.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException e) {
            throw new CitrusRuntimeException("Unsupported Playwright screencast annotation position: " + position, e);
        }
    }

    /**
     * Fluent builder for screencast recording and annotation commands.
     */
    public static class Builder extends AbstractPlaywrightAction.Builder<ScreencastAction, Builder> {

        private Command command;
        private String path;
        private Integer quality;
        private Consumer<ScreencastFrame> onFrame;
        private AnnotatePosition position;
        private Integer fontSize;
        private Double duration;
        private String chapterTitle;
        private String description;
        private String variable;

        /**
         * Starts recording the current page into the given file.
         *
         * @param path target video file
         * @return this builder
         */
        public Builder start(String path) {
            this.command = Command.START;
            this.path = path;
            return this;
        }

        /**
         * Stops an active recording.
         *
         * @return this builder
         */
        public Builder stop() {
            this.command = Command.STOP;
            return this;
        }

        /**
         * Shows built-in annotations highlighting each interacted element.
         *
         * @return this builder
         */
        public Builder showActions() {
            this.command = Command.SHOW_ACTIONS;
            return this;
        }

        /**
         * Hides the action annotations again.
         *
         * @return this builder
         */
        public Builder hideActions() {
            this.command = Command.HIDE_ACTIONS;
            return this;
        }

        /**
         * Shows a chapter title overlay on the recording.
         *
         * @param title chapter title
         * @return this builder
         */
        public Builder showChapter(String title) {
            this.command = Command.SHOW_CHAPTER;
            this.chapterTitle = title;
            return this;
        }

        /**
         * Sets the chapter description shown below the title.
         *
         * <p>Deliberately not called {@code description}: that name belongs to the Citrus action
         * description on {@code AbstractTestActionBuilder}, and overriding it here would
         * silently redirect every {@code description(...)} call on this builder.</p>
         *
         * @param chapterDescription chapter description
         * @return this builder
         */
        public Builder chapterDescription(String chapterDescription) {
            this.description = chapterDescription;
            return this;
        }

        /**
         * Sets the display duration in milliseconds for annotations and chapters.
         *
         * @param duration duration in milliseconds
         * @return this builder
         */
        public Builder duration(double duration) {
            this.duration = duration;
            return this;
        }

        /**
         * Sets where action annotations are drawn, for example {@code top-right}.
         *
         * @param position annotation position name
         * @return this builder
         */
        public Builder position(String position) {
            this.position = annotatePosition(position);
            return this;
        }

        /**
         * Sets the annotation font size in pixels.
         *
         * @param fontSize font size
         * @return this builder
         */
        public Builder fontSize(int fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        /**
         * Sets the recording quality.
         *
         * @param quality quality between 0 and 100
         * @return this builder
         */
        public Builder quality(int quality) {
            this.quality = quality;
            return this;
        }

        /**
         * Receives every captured frame in real time. Java DSL only - a callback has no
         * declarative representation, and frame data must not be logged.
         *
         * @param onFrame frame consumer
         * @return this builder
         */
        public Builder onFrame(Consumer<ScreencastFrame> onFrame) {
            this.onFrame = onFrame;
            return this;
        }

        /**
         * Stores the recording path in a test variable.
         *
         * @param variable target test variable
         * @return this builder
         */
        public Builder variable(String variable) {
            this.variable = variable;
            return this;
        }

        @Override
        public ScreencastAction build() {
            if (command == null) {
                throw new CitrusRuntimeException("Missing Playwright screencast command");
            }
            if (command == Command.START && (path == null || path.isBlank())) {
                throw new CitrusRuntimeException("Missing Playwright screencast target path - call start(...) with a file");
            }
            if (command == Command.SHOW_CHAPTER && (chapterTitle == null || chapterTitle.isBlank())) {
                throw new CitrusRuntimeException("Missing Playwright screencast chapter title");
            }
            return new ScreencastAction(this);
        }
    }
}
