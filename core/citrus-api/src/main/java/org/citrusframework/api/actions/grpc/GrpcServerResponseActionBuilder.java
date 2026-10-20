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
package org.citrusframework.api.actions.grpc;

import org.citrusframework.TestAction;
import org.citrusframework.api.actions.SendActionBuilder;
import org.citrusframework.api.actions.SendMessageBuilderFactory;

/**
 * Action builder interface for sending a gRPC response message from a simulator.
 */
public interface GrpcServerResponseActionBuilder<T extends TestAction, M extends SendMessageBuilderFactory<T, M>, B extends GrpcServerResponseActionBuilder<T, M, B>>
        extends SendActionBuilder<T, M, B> {

    /**
     * Sends a response message with status {@code OK}.
     * @return
     */
    B response();

    /**
     * Ends the call with the given status code (e.g. {@code NOT_FOUND}).
     * @param statusCode
     * @return
     */
    B response(String statusCode);

    /**
     * Sends the next message of a server streaming call.
     * @return
     */
    B streamMessage();

    /**
     * Completes a server streaming call with status {@code OK}.
     * @return
     */
    B streamCompleted();

    /**
     * Completes a server streaming call with the given status code.
     * @param statusCode
     * @return
     */
    B streamCompleted(String statusCode);

    /**
     * Sets the status description.
     * @param statusDescription
     * @return
     */
    B statusDescription(String statusDescription);

    /**
     * Sets a response trailer.
     * @param name
     * @param value
     * @return
     */
    B trailer(String name, Object value);

    /**
     * Sets a response header. In contrast to a trailer the header is sent with the response
     * headers of the call.
     * @param name
     * @param value
     * @return
     */
    B metadata(String name, Object value);

    /**
     * Targets a specific call by its call id. Use this to answer one of several calls that are
     * currently open on the simulator. By default the response is sent to the call that was
     * received last.
     * @param callId the id of the call to respond to
     * @return
     */
    B callId(String callId);
}
