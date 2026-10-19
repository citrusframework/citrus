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

package org.citrusframework.grpc.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps track of the live calls of one gRPC client endpoint. Each call is registered under its
 * Citrus call id and removed when it completes, is cancelled or when the test ends.
 *
 * @since 5.1
 */
public class ClientCallRegistry {

    private final Map<String, ClientCallQueue> calls = new ConcurrentHashMap<>();
    private final Map<String, Runnable> cancellations = new ConcurrentHashMap<>();

    /**
     * Registers a new call.
     *
     * @param callId the Citrus call id
     * @param queue the queue receiving the call's messages
     * @param cancellation cancels the underlying gRPC call
     */
    public void register(String callId, ClientCallQueue queue, Runnable cancellation) {
        calls.put(callId, queue);
        cancellations.put(callId, cancellation);
    }

    /**
     * @param callId the Citrus call id
     * @return the queue of the call or {@code null} when the call is unknown
     */
    public ClientCallQueue get(String callId) {
        return calls.get(callId);
    }

    /**
     * Removes the call from the registry.
     *
     * @param callId the Citrus call id
     */
    public void remove(String callId) {
        calls.remove(callId);
        cancellations.remove(callId);
    }

    /**
     * @return the ids of all currently open calls
     */
    public List<String> getCallIds() {
        return new ArrayList<>(calls.keySet());
    }

    /**
     * Cancels and removes all open calls.
     */
    public void cancelAll() {
        for (String callId : getCallIds()) {
            cancel(callId);
        }
    }

    /**
     * Cancels and removes a single call.
     *
     * @param callId the Citrus call id
     */
    public void cancel(String callId) {
        Runnable cancellation = cancellations.remove(callId);
        ClientCallQueue queue = calls.remove(callId);

        if (queue != null) {
            queue.clear();
        }

        if (cancellation != null) {
            cancellation.run();
        }
    }
}
