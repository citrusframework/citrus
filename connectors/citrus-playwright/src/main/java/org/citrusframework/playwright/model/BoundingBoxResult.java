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

package org.citrusframework.playwright.model;

import com.microsoft.playwright.options.BoundingBox;

/**
 * Stable Citrus-facing representation of a Playwright element bounding box.
 *
 * @param x left coordinate in pixels
 * @param y top coordinate in pixels
 * @param width width in pixels
 * @param height height in pixels
 */
public record BoundingBoxResult(double x, double y, double width, double height) {

    /**
     * Converts a Playwright bounding box to a stable record.
     *
     * @param box Playwright bounding box
     * @return result record, or {@code null} when Playwright returned no box
     */
    public static BoundingBoxResult from(BoundingBox box) {
        return box == null ? null : new BoundingBoxResult(box.x, box.y, box.width, box.height);
    }
}
