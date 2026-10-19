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

package org.citrusframework.grpc.message;

import org.citrusframework.message.MessageHeaders;

/**
 * Special set of gRPC related message headers.
 *
 * @since 5.1
 */
public class GrpcMessageHeaders {

    private GrpcMessageHeaders() {
        // prevent instantiation
    }

    /** gRPC header prefix */
    public static final String GRPC_PREFIX = MessageHeaders.PREFIX + "grpc_";

    /** Full name of the gRPC method, e.g. {@code helloworld.Greeter/SayHello} */
    public static final String METHOD = GRPC_PREFIX + "method";

    /** Kind of the gRPC method, {@code UNARY} or {@code SERVER_STREAMING} */
    public static final String METHOD_TYPE = GRPC_PREFIX + "method_type";

    /** Unique identifier of a gRPC call */
    public static final String CALL_ID = GRPC_PREFIX + "call_id";

    /** Kind of the gRPC message, one of {@code call}, {@code response}, {@code stream_message} or {@code stream_end} */
    public static final String KIND = GRPC_PREFIX + "kind";

    /** gRPC status code name, e.g. {@code OK} or {@code NOT_FOUND} */
    public static final String STATUS_CODE = GRPC_PREFIX + "status_code";

    /** gRPC status description */
    public static final String STATUS_DESCRIPTION = GRPC_PREFIX + "status_description";

    /** 0-based sequence number of a streamed message */
    public static final String STREAM_SEQUENCE = GRPC_PREFIX + "stream_sequence";

    /** Marks the final message of a call or stream */
    public static final String END_OF_STREAM = GRPC_PREFIX + "end_of_stream";

    /** Call deadline for a single client send, overriding the endpoint default */
    public static final String DEADLINE = GRPC_PREFIX + "deadline";

    /** Prefix for gRPC trailer headers */
    public static final String TRAILER_PREFIX = GRPC_PREFIX + "trailer_";

    /** Method type value for unary calls */
    public static final String METHOD_TYPE_UNARY = "UNARY";

    /** Method type value for server streaming calls */
    public static final String METHOD_TYPE_SERVER_STREAMING = "SERVER_STREAMING";

    /** Message kind value for an incoming simulator call */
    public static final String KIND_CALL = "call";

    /** Message kind value for a unary response */
    public static final String KIND_RESPONSE = "response";

    /** Message kind value for a streamed message */
    public static final String KIND_STREAM_MESSAGE = "stream_message";

    /** Message kind value for the final status of a stream */
    public static final String KIND_STREAM_END = "stream_end";
}
