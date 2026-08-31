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

import org.citrusframework.playwright.model.NetworkRecord;
import org.citrusframework.playwright.model.SecretPatternRedactor;

/**
 * Per-page registry for bounded network-event capture.
 */
public class NetworkCaptureRegistry {

    private final Map<Page, BoundedEventBuffer<NetworkRecord>> buffers = new IdentityHashMap<>();

    /**
     * Starts request, response, and failed-request capture for a page.
     *
     * @param page page to capture
     * @param limit maximum retained records
     */
    public synchronized void capture(Page page, int limit) {
        capture(page, limit, new SecretPatternRedactor());
    }

    /**
     * Starts request, response, and failed-request capture for a page.
     *
     * @param page page to capture
     * @param limit maximum retained records
     * @param redactor diagnostic redactor used when records are created
     */
    public synchronized void capture(Page page, int limit, SecretPatternRedactor redactor) {
        if (buffers.containsKey(page)) {
            return;
        }
        SecretPatternRedactor effectiveRedactor = redactor == null ? new SecretPatternRedactor() : redactor;
        BoundedEventBuffer<NetworkRecord> buffer = new BoundedEventBuffer<>(limit);
        buffers.put(page, buffer);
        page.onRequest(request -> buffer.add(NetworkRecord.request(request, effectiveRedactor)));
        page.onResponse(response -> buffer.add(NetworkRecord.response(response, effectiveRedactor)));
        page.onRequestFailed(request -> buffer.add(NetworkRecord.failed(request, effectiveRedactor)));
    }

    /**
     * Returns captured network records for a page.
     *
     * @param page captured page
     * @return retained record snapshot
     */
    public synchronized List<NetworkRecord> records(Page page) {
        BoundedEventBuffer<NetworkRecord> buffer = buffers.get(page);
        return buffer == null ? List.of() : buffer.snapshot();
    }

    /**
     * Clears captured network records for a page while keeping capture active.
     *
     * @param page captured page
     */
    public synchronized void clear(Page page) {
        BoundedEventBuffer<NetworkRecord> buffer = buffers.get(page);
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
