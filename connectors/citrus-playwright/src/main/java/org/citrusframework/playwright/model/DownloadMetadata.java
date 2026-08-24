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

import java.nio.file.Path;

/**
 * Stable metadata for the latest Playwright download observed by the endpoint.
 *
 * @param path saved or temporary download path
 * @param suggestedFilename filename suggested by the browser
 * @param url download URL
 * @param failure Playwright failure text, when download failed
 */
public record DownloadMetadata(String path, String suggestedFilename, String url, String failure) {

    /**
     * Creates metadata from a path and raw Playwright download fields.
     *
     * @param path saved or temporary download path
     * @param suggestedFilename suggested filename
     * @param url download URL
     * @param failure failure text
     * @return download metadata
     */
    public static DownloadMetadata of(Path path, String suggestedFilename, String url, String failure) {
        return new DownloadMetadata(path == null ? null : path.toString(), suggestedFilename, url, failure);
    }
}
