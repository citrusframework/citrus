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

import java.util.Map;

import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.util.TypeConversionUtils;

/**
 * gRPC message with typed accessors for all gRPC related message headers.
 *
 * @since 5.1
 */
public class GrpcMessage extends DefaultMessage {

    /**
     * Empty constructor initializing with empty message payload.
     */
    public GrpcMessage() {
        super();
    }

    /**
     * Constructs copy of given message.
     *
     * @param message the base message for the copy operation
     */
    public GrpcMessage(Message message) {
        super(message);
    }

    /**
     * Constructs new message based on given message payload.
     *
     * @param payload the message payload
     */
    public GrpcMessage(Object payload) {
        super(payload);
    }

    /**
     * Constructs new message based on given message payload and headers.
     *
     * @param payload the message payload
     * @param headers the message headers
     */
    public GrpcMessage(Object payload, Map<String, Object> headers) {
        super(payload, headers);
    }

    public GrpcMessage method(String method) {
        setHeader(GrpcMessageHeaders.METHOD, method);
        return this;
    }

    public String getMethod() {
        return header(GrpcMessageHeaders.METHOD, String.class);
    }

    public GrpcMessage methodType(String methodType) {
        setHeader(GrpcMessageHeaders.METHOD_TYPE, methodType);
        return this;
    }

    public String getMethodType() {
        return header(GrpcMessageHeaders.METHOD_TYPE, String.class);
    }

    public GrpcMessage callId(String callId) {
        setHeader(GrpcMessageHeaders.CALL_ID, callId);
        return this;
    }

    public String getCallId() {
        return header(GrpcMessageHeaders.CALL_ID, String.class);
    }

    public GrpcMessage kind(String kind) {
        setHeader(GrpcMessageHeaders.KIND, kind);
        return this;
    }

    public String getKind() {
        return header(GrpcMessageHeaders.KIND, String.class);
    }

    public GrpcMessage statusCode(String statusCode) {
        setHeader(GrpcMessageHeaders.STATUS_CODE, statusCode);
        return this;
    }

    public String getStatusCode() {
        return header(GrpcMessageHeaders.STATUS_CODE, String.class);
    }

    public GrpcMessage statusDescription(String statusDescription) {
        setHeader(GrpcMessageHeaders.STATUS_DESCRIPTION, statusDescription);
        return this;
    }

    public String getStatusDescription() {
        return header(GrpcMessageHeaders.STATUS_DESCRIPTION, String.class);
    }

    public GrpcMessage streamSequence(Integer streamSequence) {
        setHeader(GrpcMessageHeaders.STREAM_SEQUENCE, streamSequence);
        return this;
    }

    public Integer getStreamSequence() {
        return header(GrpcMessageHeaders.STREAM_SEQUENCE, Integer.class);
    }

    public GrpcMessage endOfStream(Boolean endOfStream) {
        setHeader(GrpcMessageHeaders.END_OF_STREAM, endOfStream);
        return this;
    }

    public Boolean isEndOfStream() {
        return header(GrpcMessageHeaders.END_OF_STREAM, Boolean.class);
    }

    public GrpcMessage metadata(String name, Object value) {
        setHeader(name, value);
        return this;
    }

    public GrpcMessage trailer(String name, Object value) {
        setHeader(GrpcMessageHeaders.TRAILER_PREFIX + name, value);
        return this;
    }

    @Override
    public GrpcMessage setHeader(String headerName, Object headerValue) {
        super.setHeader(headerName, headerValue);
        return this;
    }

    /**
     * Reads a header value and converts it to the given type. Returns {@code null} when the header is not set.
     */
    private <T> T header(String headerName, Class<T> type) {
        Object value = getHeader(headerName);
        if (value == null) {
            return null;
        }

        return TypeConversionUtils.convertIfNecessary(value, type);
    }
}
