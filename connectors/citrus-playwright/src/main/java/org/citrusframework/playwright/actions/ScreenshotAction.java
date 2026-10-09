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

import java.nio.file.Path;
import java.util.Locale;

import com.microsoft.playwright.options.ScreenshotType;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.LocatorResolver;

/**
 * Captures a screenshot of the current page.
 *
 * <p>The image format is taken from the file extension unless an explicit type is set. Lossy
 * formats additionally support a quality setting, where 100 is lossless for WebP.</p>
 */
public class ScreenshotAction extends AbstractPlaywrightAction {

    private final String path;
    private final String variable;
    private final ScreenshotType type;
    private final Integer quality;
    private final Boolean fullPage;

    public ScreenshotAction(Builder builder) {
        super("screenshot", builder);
        this.path = builder.path;
        this.variable = builder.variable;
        this.type = builder.type;
        this.quality = builder.quality;
        this.fullPage = builder.fullPage;
    }

    public String getPath() {
        return path;
    }

    public String getVariable() {
        return variable;
    }

    public ScreenshotType getType() {
        return type;
    }

    public Integer getQuality() {
        return quality;
    }

    public Boolean getFullPage() {
        return fullPage;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        String resolvedPath = LocatorResolver.resolve(path, context);

        com.microsoft.playwright.Page.ScreenshotOptions options =
                new com.microsoft.playwright.Page.ScreenshotOptions().setPath(Path.of(resolvedPath));

        ScreenshotType resolvedType = type != null ? type : ScreenshotTypes.fromPath(resolvedPath);
        if (resolvedType != null) {
            options.setType(resolvedType);
        }
        if (quality != null) {
            options.setQuality(quality);
        }
        if (fullPage != null) {
            options.setFullPage(fullPage);
        }

        browser.getCurrentPage().screenshot(options);

        if (variable != null) {
            context.setVariable(variable, resolvedPath);
        }
    }

    /**
     * Resolves screenshot type names and file extensions onto the Playwright enum.
     */
    static final class ScreenshotTypes {

        private ScreenshotTypes() {
        }

        static ScreenshotType parse(String type) {
            try {
                return ScreenshotType.valueOf(type.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new CitrusRuntimeException("Unsupported Playwright screenshot type: " + type, e);
            }
        }

        /**
         * Derives the screenshot type from a file extension. Returns null for an unknown or
         * missing extension so the driver default applies.
         *
         * @param path screenshot target path
         * @return matching screenshot type or null
         */
        static ScreenshotType fromPath(String path) {
            int separator = path.lastIndexOf('.');
            if (separator < 0) {
                return null;
            }

            return switch (path.substring(separator + 1).toLowerCase(Locale.ROOT)) {
                case "webp" -> ScreenshotType.WEBP;
                case "jpeg", "jpg" -> ScreenshotType.JPEG;
                default -> null;
            };
        }
    }

    public static class Builder extends AbstractPlaywrightAction.Builder<ScreenshotAction, Builder> {

        private String path = "target/playwright/screenshot.png";
        private String variable;
        private ScreenshotType type;
        private Integer quality;
        private Boolean fullPage;

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder variable(String variable) {
            this.variable = variable;
            return this;
        }

        /**
         * Sets the image format explicitly. Overrides the format inferred from the file
         * extension. Supported values are {@code png}, {@code jpeg} and {@code webp}.
         *
         * @param type screenshot type name
         * @return this builder
         */
        public Builder type(String type) {
            this.type = ScreenshotTypes.parse(type);
            return this;
        }

        /**
         * Sets the image quality for the lossy formats. For WebP a quality of 100 is lossless.
         *
         * @param quality quality between 0 and 100
         * @return this builder
         */
        public Builder quality(int quality) {
            this.quality = quality;
            return this;
        }

        /**
         * Captures the full scrollable page instead of the current viewport.
         *
         * @param fullPage true to capture the whole page
         * @return this builder
         */
        public Builder fullPage(boolean fullPage) {
            this.fullPage = fullPage;
            return this;
        }

        @Override
        public ScreenshotAction build() {
            if (quality != null && effectiveType() == ScreenshotType.PNG) {
                throw new CitrusRuntimeException(
                        "Playwright screenshot quality is not supported for the png format - use jpeg or webp");
            }
            return new ScreenshotAction(this);
        }

        private ScreenshotType effectiveType() {
            if (type != null) {
                return type;
            }
            ScreenshotType fromPath = ScreenshotTypes.fromPath(path);
            return fromPath != null ? fromPath : ScreenshotType.PNG;
        }
    }
}
