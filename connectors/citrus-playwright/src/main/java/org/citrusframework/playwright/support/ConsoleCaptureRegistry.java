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

package org.citrusframework.playwright.support;

import com.microsoft.playwright.Page;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.playwright.model.ConsoleMessageRecord;

/**
 * Per-page registry for bounded console-message capture.
 */
public class ConsoleCaptureRegistry {

    private final Map<Page, BoundedEventBuffer<ConsoleMessageRecord>> buffers = new IdentityHashMap<>();

    /**
     * Starts console capture for a page if it is not already captured.
     *
     * @param page page to capture
     * @param limit maximum retained messages
     */
    public synchronized void capture(Page page, int limit) {
        if (buffers.containsKey(page)) {
            return;
        }
        BoundedEventBuffer<ConsoleMessageRecord> buffer = new BoundedEventBuffer<>(limit);
        buffers.put(page, buffer);
        page.onConsoleMessage(message -> buffer.add(ConsoleMessageRecord.from(message)));
    }

    /**
     * Returns captured console messages for a page.
     *
     * @param page captured page
     * @return retained message snapshot
     */
    public synchronized List<ConsoleMessageRecord> messages(Page page) {
        BoundedEventBuffer<ConsoleMessageRecord> buffer = buffers.get(page);
        return buffer == null ? List.of() : buffer.snapshot();
    }

    /**
     * Clears captured console messages for a page while keeping capture active.
     *
     * @param page captured page
     */
    public synchronized void clear(Page page) {
        BoundedEventBuffer<ConsoleMessageRecord> buffer = buffers.get(page);
        if (buffer != null) {
            buffer.clear();
        }
    }

    /**
     * Removes a page from the registry when it is closed or unregistered.
     *
     * @param page page to remove
     */
    public synchronized void remove(Page page) {
        buffers.remove(page);
    }

    /**
     * Returns the number of pages with active capture buffers.
     *
     * @return capture buffer count
     */
    public synchronized int size() {
        return buffers.size();
    }
}
