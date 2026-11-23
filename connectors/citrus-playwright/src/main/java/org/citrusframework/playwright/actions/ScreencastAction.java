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
import com.microsoft.playwright.options.ScreencastActionStyle;
import com.microsoft.playwright.options.ScreencastCursor;

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
        SHOW_CHAPTER,
        SHOW_OVERLAY,
        SHOW_OVERLAYS,
        HIDE_OVERLAYS
    }

    private final Command command;
    private final String path;
    private final Integer quality;
    private final Integer fps;
    private final Integer sizeWidth;
    private final Integer sizeHeight;
    private final Consumer<ScreencastFrame> onFrame;
    private final AnnotatePosition position;
    private final ScreencastCursor cursor;
    private final ScreencastActionStyle style;
    private final String stylePoint;
    private final String styleHighlight;
    private final String styleTitle;
    private final Integer fontSize;
    private final Double duration;
    private final String chapterTitle;
    private final String description;
    private final String overlayHtml;
    private final String variable;

    public ScreencastAction(Builder builder) {
        super("screencast", builder);
        this.command = builder.command;
        this.path = builder.path;
        this.quality = builder.quality;
        this.fps = builder.fps;
        this.sizeWidth = builder.sizeWidth;
        this.sizeHeight = builder.sizeHeight;
        this.onFrame = builder.onFrame;
        this.position = builder.position;
        this.cursor = builder.cursor;
        this.style = builder.style;
        this.stylePoint = builder.stylePoint;
        this.styleHighlight = builder.styleHighlight;
        this.styleTitle = builder.styleTitle;
        this.fontSize = builder.fontSize;
        this.duration = builder.duration;
        this.chapterTitle = builder.chapterTitle;
        this.description = builder.description;
        this.overlayHtml = builder.overlayHtml;
        this.variable = builder.variable;
    }

    public Command getCommand() {
        return command;
    }

    public Integer getFps() {
        return fps;
    }

    public ScreencastCursor getCursor() {
        return cursor;
    }

    public ScreencastActionStyle getStyle() {
        return style;
    }

    public String getStylePoint() {
        return stylePoint;
    }

    public String getStyleHighlight() {
        return styleHighlight;
    }

    public String getStyleTitle() {
        return styleTitle;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        Screencast screencast = browser.getCurrentPage().screencast();

        switch (command) {
            case START -> startRecording(screencast, context);
            case STOP -> screencast.stop();
            case SHOW_ACTIONS -> screencast.showActions(showActionsOptions(context));
            case HIDE_ACTIONS -> screencast.hideActions();
            case SHOW_CHAPTER -> screencast.showChapter(
                    LocatorResolver.resolve(chapterTitle, context), showChapterOptions(context));
            case SHOW_OVERLAY ->
                // The returned handle is intentionally not retained: the overlay persists
                // until hideOverlays() hides it again. Removal is not modeled — stopping
                // the recording cleans up.
                    screencast.showOverlay(LocatorResolver.resolve(overlayHtml, context));
            case SHOW_OVERLAYS -> screencast.showOverlays();
            case HIDE_OVERLAYS -> screencast.hideOverlays();
        }
    }

    private void startRecording(Screencast screencast, TestContext context) {
        Path target = Path.of(LocatorResolver.resolve(path, context));
        createParentDirectory(target);

        Screencast.StartOptions options = new Screencast.StartOptions().setPath(target);
        if (quality != null) {
            options.setQuality(quality);
        }
        if (fps != null) {
            options.setFps(fps);
        }
        if (sizeWidth != null && sizeHeight != null) {
            options.setSize(sizeWidth, sizeHeight);
        }
        if (onFrame != null) {
            options.setOnFrame(onFrame);
        }

        screencast.start(options);

        if (variable != null) {
            context.setVariable(variable, target.toString());
        }
    }

    private Screencast.ShowActionsOptions showActionsOptions(TestContext context) {
        Screencast.ShowActionsOptions options = new Screencast.ShowActionsOptions();

        if (position != null) {
            options.setPosition(position);
        }
        if (cursor != null) {
            options.setCursor(cursor);
        }

        String point = stylePoint != null ? stylePoint : (style != null ? style.point : null);
        String highlight = styleHighlight != null ? styleHighlight : (style != null ? style.highlight : null);
        String title = styleTitle != null ? styleTitle : (style != null ? style.title : null);
        if (title == null && fontSize != null) {
            title = "font-size: %dpx".formatted(fontSize);
        }
        if (point != null || highlight != null || title != null) {
            ScreencastActionStyle resolvedStyle = new ScreencastActionStyle();
            if (point != null) {
                resolvedStyle.setPoint(LocatorResolver.resolve(point, context));
            }
            if (highlight != null) {
                resolvedStyle.setHighlight(LocatorResolver.resolve(highlight, context));
            }
            if (title != null) {
                resolvedStyle.setTitle(LocatorResolver.resolve(title, context));
            }
            options.setStyle(resolvedStyle);
        } else if (style != null) {
            options.setStyle(new ScreencastActionStyle());
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
     * Resolves the DSL screencast cursor names onto the Playwright enum.
     *
     * @param cursor cursor name such as {@code none} or {@code pointer}
     * @return matching screencast cursor
     */
    static ScreencastCursor screencastCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            throw new CitrusRuntimeException("Unsupported Playwright screencast cursor: " + cursor);
        }
        try {
            return ScreencastCursor.valueOf(cursor.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException e) {
            throw new CitrusRuntimeException("Unsupported Playwright screencast cursor: " + cursor, e);
        }
    }

    /**
     * Fluent builder for screencast recording and annotation commands.
     */
    public static class Builder extends AbstractPlaywrightAction.Builder<ScreencastAction, Builder> {

        private Command command;
        private String path;
        private Integer quality;
        private Integer fps;
        private Integer sizeWidth;
        private Integer sizeHeight;
        private Consumer<ScreencastFrame> onFrame;
        private AnnotatePosition position;
        private ScreencastCursor cursor;
        private ScreencastActionStyle style;
        private String stylePoint;
        private String styleHighlight;
        private String styleTitle;
        private Integer fontSize;
        private Double duration;
        private String chapterTitle;
        private String description;
        private String overlayHtml;
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
         * Sets the cursor indicator mode drawn for action annotations ({@code none} or
         * {@code pointer}).
         *
         * @param cursor screencast cursor mode
         * @return this builder
         */
        public Builder cursor(ScreencastCursor cursor) {
            this.cursor = cursor;
            return this;
        }

        /**
         * Sets the cursor indicator mode drawn for action annotations by name ({@code none} or
         * {@code pointer}).
         *
         * @param cursor cursor mode name
         * @return this builder
         */
        public Builder cursor(String cursor) {
            this.cursor = screencastCursor(cursor);
            return this;
        }

        /**
         * Configures custom CSS style declarations for action annotations (point indicator, target
         * highlight box, and title overlay). Individual {@link #stylePoint(String)},
         * {@link #styleHighlight(String)}, and {@link #styleTitle(String)} setters take precedence
         * over matching fields on this object, and Citrus {@code ${var}} placeholders are resolved
         * at execution time.
         *
         * @param style screencast action style
         * @return this builder
         */
        public Builder style(ScreencastActionStyle style) {
            this.style = style;
            return this;
        }

        /**
         * Sets CSS declarations for the action point indicator on {@link #showActions()}.
         *
         * @param stylePoint CSS declarations, may contain Citrus variables
         * @return this builder
         */
        public Builder stylePoint(String stylePoint) {
            this.stylePoint = stylePoint;
            return this;
        }

        /**
         * Sets CSS declarations for the target element highlight box on {@link #showActions()}.
         *
         * @param styleHighlight CSS declarations, may contain Citrus variables
         * @return this builder
         */
        public Builder styleHighlight(String styleHighlight) {
            this.styleHighlight = styleHighlight;
            return this;
        }

        /**
         * Sets CSS declarations for the action title overlay on {@link #showActions()}.
         *
         * @param styleTitle CSS declarations, may contain Citrus variables
         * @return this builder
         */
        public Builder styleTitle(String styleTitle) {
            this.styleTitle = styleTitle;
            return this;
        }

        /**
         * Sets the annotation font size in pixels.
         *
         * @param fontSize font size
         * @return this builder
         * @deprecated Use {@link #styleTitle(String)} or {@link #style(ScreencastActionStyle)} instead.
         */
        @Deprecated
        public Builder fontSize(int fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        /**
         * Shows a custom HTML overlay on the recording. The overlay stays visible until
         * {@link #hideOverlays()} closes it.
         *
         * @param html overlay HTML, test-supplied
         * @return this builder
         */
        public Builder showOverlay(String html) {
            this.command = Command.SHOW_OVERLAY;
            this.overlayHtml = html;
            return this;
        }

        /**
         * Re-shows custom overlays hidden again after {@link #hideOverlays()}.
         *
         * @return this builder
         */
        public Builder showOverlays() {
            this.command = Command.SHOW_OVERLAYS;
            return this;
        }

        /**
         * Hides custom overlays on the recording.
         *
         * @return this builder
         */
        public Builder hideOverlays() {
            this.command = Command.HIDE_OVERLAYS;
            return this;
        }

        /**
         * Sets the recording dimensions applied when the recording starts.
         *
         * @param width recording width in pixels, must be positive
         * @param height recording height in pixels, must be positive
         * @return this builder
         */
        public Builder size(int width, int height) {
            this.sizeWidth = width;
            this.sizeHeight = height;
            return this;
        }

        /**
         * Sets the recording frame rate in frames per second applied when the recording starts.
         *
         * @param fps frame rate in frames per second, must be positive
         * @return this builder
         */
        public Builder fps(int fps) {
            this.fps = fps;
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
            if (command == Command.SHOW_OVERLAY && (overlayHtml == null || overlayHtml.isBlank())) {
                throw new CitrusRuntimeException("Missing Playwright screencast overlay HTML - call showOverlay(...) with content");
            }
            if ((sizeWidth == null) != (sizeHeight == null)) {
                throw new CitrusRuntimeException(
                        "Incomplete Playwright screencast size - set both size-width and size-height");
            }
            if ((sizeWidth != null || sizeHeight != null) && command != Command.START) {
                throw new CitrusRuntimeException("Playwright screencast size only applies to the start command");
            }
            if ((sizeWidth != null && sizeWidth <= 0) || (sizeHeight != null && sizeHeight <= 0)) {
                throw new CitrusRuntimeException("Playwright screencast size must be positive");
            }
            if (fps != null && command != Command.START) {
                throw new CitrusRuntimeException("Playwright screencast fps only applies to the start command");
            }
            if (fps != null && fps <= 0) {
                throw new CitrusRuntimeException("Playwright screencast fps must be positive");
            }
            if ((style != null || stylePoint != null || styleHighlight != null || styleTitle != null)
                    && command != Command.SHOW_ACTIONS) {
                throw new CitrusRuntimeException("Playwright screencast style only applies to the show-actions command");
            }
            if (cursor != null && command != Command.SHOW_ACTIONS) {
                throw new CitrusRuntimeException("Playwright screencast cursor only applies to the show-actions command");
            }
            return new ScreencastAction(this);
        }
    }
}
