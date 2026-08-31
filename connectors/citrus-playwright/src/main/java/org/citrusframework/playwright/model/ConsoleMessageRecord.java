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

import com.microsoft.playwright.ConsoleMessage;

/**
 * Immutable snapshot of a Playwright console message.
 *
 * @param type Playwright console message type
 * @param text console text
 * @param location source location reported by Playwright
 * @param timestamp Playwright message timestamp
 */
public record ConsoleMessageRecord(String type, String text, String location, double timestamp) {

    /**
     * Creates a record from a Playwright console message event.
     *
     * @param message Playwright console message
     * @return immutable console message record
     */
    public static ConsoleMessageRecord from(ConsoleMessage message) {
        return new ConsoleMessageRecord(message.type(), message.text(), message.location(), message.timestamp());
    }

    /**
     * Formats the console message for reports and failure evidence logs.
     *
     * @return human-readable console message line
     */
    public String format() {
        return "[%s] %s".formatted(type, text);
    }
}
