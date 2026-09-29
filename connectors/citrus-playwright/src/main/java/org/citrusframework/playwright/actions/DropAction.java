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

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.DropPayload;
import com.microsoft.playwright.options.FilePayload;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.LocatorResolver;

/**
 * Simulates an external drag and drop onto an element.
 *
 * <p>Playwright dispatches {@code dragenter}, {@code dragover} and {@code drop} with a synthetic
 * data transfer in the page context, so upload zones can be exercised without a real file
 * manager. The payload is either a file or a map of clipboard-like media types.</p>
 */
public class DropAction extends AbstractPlaywrightAction {

    private final org.citrusframework.playwright.model.LocatorSpec locator;
    private final String fileName;
    private final String fileMimeType;
    private final String fileContent;
    private final Map<String, String> data;
    private final Double timeout;
    private final Double positionX;
    private final Double positionY;

    public DropAction(Builder builder) {
        super("drop", builder);
        this.locator = builder.locator;
        this.fileName = builder.fileName;
        this.fileMimeType = builder.fileMimeType;
        this.fileContent = builder.fileContent;
        this.data = Map.copyOf(builder.data);
        this.timeout = builder.timeout;
        this.positionX = builder.positionX;
        this.positionY = builder.positionY;
    }

    public Map<String, String> getData() {
        return data;
    }

    public String getFileName() {
        return fileName;
    }

    public Double getTimeout() {
        return timeout;
    }

    public Double getPositionX() {
        return positionX;
    }

    public Double getPositionY() {
        return positionY;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        Locator element = LocatorResolver.resolve(browser.getCurrentPage(), locator, context);
        if (timeout == null && positionX == null) {
            element.drop(payload(context));
        } else {
            element.drop(payload(context), dropOptions());
        }
    }

    private Locator.DropOptions dropOptions() {
        Locator.DropOptions options = new Locator.DropOptions();
        if (timeout != null) {
            options.setTimeout(timeout);
        }
        if (positionX != null && positionY != null) {
            options.setPosition(positionX, positionY);
        }
        return options;
    }

    private DropPayload payload(TestContext context) {
        if (fileName != null) {
            return new DropPayload().setFiles(new FilePayload(
                    LocatorResolver.resolve(fileName, context),
                    LocatorResolver.resolve(fileMimeType, context),
                    LocatorResolver.resolve(fileContent, context).getBytes(StandardCharsets.UTF_8)));
        }

        Map<String, String> resolved = new LinkedHashMap<>();
        data.forEach((mimeType, value) -> resolved.put(
                LocatorResolver.resolve(mimeType, context), LocatorResolver.resolve(value, context)));

        return new DropPayload().setData(resolved);
    }

    /**
     * Fluent builder for the drop action.
     */
    public static class Builder extends ElementActionBuilder<DropAction, Builder> {

        private String fileName;
        private String fileMimeType;
        private String fileContent;
        private final Map<String, String> data = new LinkedHashMap<>();
        private Double timeout;
        private Double positionX;
        private Double positionY;

        /**
         * Drops a single in-memory file onto the element.
         *
         * @param name file name reported to the page
         * @param mimeType file media type
         * @param content file content
         * @return this builder
         */
        public Builder file(String name, String mimeType, String content) {
            this.fileName = name;
            this.fileMimeType = mimeType;
            this.fileContent = content;
            return this;
        }

        /**
         * Adds one clipboard-like entry to the drop payload. Call repeatedly to supply several
         * media types.
         *
         * @param mimeType media type, for example {@code text/plain}
         * @param value value for that media type
         * @return this builder
         */
        public Builder data(String mimeType, String value) {
            this.data.put(mimeType, value);
            return this;
        }

        /**
         * Sets the drop timeout in milliseconds.
         *
         * @param timeoutMs timeout in milliseconds
         * @return this builder
         */
        public Builder timeout(double timeoutMs) {
            this.timeout = timeoutMs;
            return this;
        }

        /**
         * Drops at a position relative to the element's top-left corner.
         *
         * @param x horizontal offset in pixels
         * @param y vertical offset in pixels
         * @return this builder
         */
        public Builder position(double x, double y) {
            this.positionX = x;
            this.positionY = y;
            return this;
        }

        @Override
        public DropAction build() {
            requireLocator();

            if (fileName == null && data.isEmpty()) {
                throw new CitrusRuntimeException(
                        "Missing Playwright drop payload - call file(...) or data(...) before building action");
            }
            if (fileName != null && !data.isEmpty()) {
                throw new CitrusRuntimeException(
                        "Ambiguous Playwright drop payload - set either a file or data entries, not both");
            }

            return new DropAction(this);
        }
    }
}
