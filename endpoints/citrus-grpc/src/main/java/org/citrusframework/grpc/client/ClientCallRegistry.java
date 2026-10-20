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

import com.google.protobuf.DynamicMessage;
import io.grpc.ClientCall;
import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Keeps track of the live calls of one gRPC client endpoint. Each call is registered under its
 * Citrus call id and removed when it completes, is cancelled or when the test ends. Live calls that
 * keep their request side open - client streaming and bidirectional streaming - can send further
 * messages and complete the request stream through this registry.
 *
 * @since 5.1
 */
public class ClientCallRegistry {

    private final Map<String, ClientCallQueue> calls = new ConcurrentHashMap<>();
    private final Map<String, Runnable> cancellations = new ConcurrentHashMap<>();
    private final Map<String, LiveCall> liveCalls = new ConcurrentHashMap<>();

    /**
     * Registers a new call.
     *
     * @param callId the Citrus call id
     * @param queue the queue receiving the call's messages
     * @param cancellation cancels the underlying gRPC call
     */
    public void register(String callId, ClientCallQueue queue, Runnable cancellation) {
        register(callId, queue, null, null, cancellation);
    }

    /**
     * Registers a new call that keeps its request side open for further messages.
     *
     * @param callId the Citrus call id
     * @param queue the queue receiving the call's messages
     * @param call the live gRPC client call
     * @param method the Protobuf method descriptor of the call
     * @param cancellation cancels the underlying gRPC call
     */
    public void register(String callId,
                         ClientCallQueue queue,
                         ClientCall<DynamicMessage, DynamicMessage> call,
                         com.google.protobuf.Descriptors.MethodDescriptor method,
                         Runnable cancellation) {
        calls.put(callId, queue);
        cancellations.put(callId, cancellation);

        if (call != null) {
            liveCalls.put(callId, new LiveCall(call, method));
        }
    }

    /**
     * @param callId the Citrus call id
     * @return the queue of the call or {@code null} when the call is unknown
     */
    public ClientCallQueue get(String callId) {
        return calls.get(callId);
    }

    /**
     * @param callId the Citrus call id
     * @return the live call that is still open for request messages or {@code null}
     */
    public LiveCall getLiveCall(String callId) {
        return liveCalls.get(callId);
    }

    /**
     * Sends another request message on an open live call.
     *
     * @param callId the Citrus call id
     * @param message the request message
     */
    public void sendMessage(String callId, DynamicMessage message) {
        LiveCall liveCall = requireLiveCall(callId);
        liveCall.call().sendMessage(message);
    }

    /**
     * Completes the request side of an open live call.
     *
     * @param callId the Citrus call id
     */
    public void complete(String callId) {
        LiveCall liveCall = requireLiveCall(callId);
        liveCall.call().halfClose();
    }

    /**
     * Removes the call from the registry.
     *
     * @param callId the Citrus call id
     */
    public void remove(String callId) {
        calls.remove(callId);
        cancellations.remove(callId);
        liveCalls.remove(callId);
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
        liveCalls.remove(callId);

        if (queue != null) {
            queue.clear();
        }

        if (cancellation != null) {
            cancellation.run();
        }
    }

    private LiveCall requireLiveCall(String callId) {
        LiveCall liveCall = liveCalls.get(callId);
        if (liveCall == null) {
            throw new CitrusRuntimeException(
                    "gRPC call '%s' is not open for sending - open a client streaming call first".formatted(callId));
        }

        return liveCall;
    }

    /**
     * A live gRPC client call that keeps its request side open.
     *
     * @param call the gRPC client call
     * @param method the Protobuf method descriptor of the call
     */
    public record LiveCall(ClientCall<DynamicMessage, DynamicMessage> call,
                           com.google.protobuf.Descriptors.MethodDescriptor method) {
    }
}
