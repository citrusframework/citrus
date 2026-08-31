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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread-safe fixed-size FIFO buffer for Playwright event snapshots.
 *
 * @param <T> event record type
 */
public class BoundedEventBuffer<T> {

    private final int limit;
    private final ArrayDeque<T> events = new ArrayDeque<>();

    /**
     * Creates a bounded buffer. Values below one are normalized to one.
     *
     * @param limit maximum number of retained events
     */
    public BoundedEventBuffer(int limit) {
        this.limit = Math.max(1, limit);
    }

    /**
     * Adds an event and drops oldest events when the buffer exceeds its limit.
     *
     * @param event event to retain
     */
    public synchronized void add(T event) {
        events.addLast(event);
        while (events.size() > limit) {
            events.removeFirst();
        }
    }

    /**
     * Returns a stable immutable snapshot of retained events in insertion order.
     *
     * @return retained event snapshot
     */
    public synchronized List<T> snapshot() {
        return List.copyOf(new ArrayList<>(events));
    }

    /**
     * Removes all retained events.
     */
    public synchronized void clear() {
        events.clear();
    }
}
