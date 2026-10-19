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

import org.citrusframework.message.DefaultMessage;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GrpcMessageTest {

    @Test
    public void shouldSetAndReadGrpcHeaders() {
        GrpcMessage message = new GrpcMessage("{}")
                .method("helloworld.Greeter/SayHello")
                .methodType(GrpcMessageHeaders.METHOD_TYPE_UNARY)
                .callId("call-1")
                .kind(GrpcMessageHeaders.KIND_RESPONSE)
                .statusCode("OK")
                .statusDescription("all good")
                .streamSequence(2)
                .endOfStream(true);

        assertThat(message.getMethod()).isEqualTo("helloworld.Greeter/SayHello");
        assertThat(message.getMethodType()).isEqualTo("UNARY");
        assertThat(message.getCallId()).isEqualTo("call-1");
        assertThat(message.getKind()).isEqualTo("response");
        assertThat(message.getStatusCode()).isEqualTo("OK");
        assertThat(message.getStatusDescription()).isEqualTo("all good");
        assertThat(message.getStreamSequence()).isEqualTo(2);
        assertThat(message.isEndOfStream()).isTrue();

        assertThat(message.getHeader(GrpcMessageHeaders.METHOD)).isEqualTo("helloworld.Greeter/SayHello");
        assertThat(message.getHeader(GrpcMessageHeaders.METHOD_TYPE)).isEqualTo("UNARY");
        assertThat(message.getHeader(GrpcMessageHeaders.CALL_ID)).isEqualTo("call-1");
        assertThat(message.getHeader(GrpcMessageHeaders.KIND)).isEqualTo("response");
        assertThat(message.getHeader(GrpcMessageHeaders.STATUS_CODE)).isEqualTo("OK");
        assertThat(message.getHeader(GrpcMessageHeaders.STATUS_DESCRIPTION)).isEqualTo("all good");
        assertThat(message.getHeader(GrpcMessageHeaders.STREAM_SEQUENCE)).isEqualTo(2);
        assertThat(message.getHeader(GrpcMessageHeaders.END_OF_STREAM)).isEqualTo(true);
    }

    @Test
    public void shouldAddMetadataAndTrailerHeaders() {
        GrpcMessage message = new GrpcMessage()
                .metadata("authorization", "Bearer abc")
                .trailer("x-request-id", "42");

        assertThat(message.getHeader("authorization")).isEqualTo("Bearer abc");
        assertThat(message.getHeader(GrpcMessageHeaders.TRAILER_PREFIX + "x-request-id")).isEqualTo("42");
    }

    @Test
    public void shouldCopyHeadersFromExistingMessage() {
        DefaultMessage source = new DefaultMessage("payload");
        source.setHeader("x-request-id", "42");
        source.setHeader(GrpcMessageHeaders.CALL_ID, "call-1");

        GrpcMessage message = new GrpcMessage(source);

        assertThat(message.getPayload()).isEqualTo("payload");
        assertThat(message.getHeader("x-request-id")).isEqualTo("42");
        assertThat(message.getCallId()).isEqualTo("call-1");
    }

    @Test
    public void shouldReturnNullForMissingHeaders() {
        GrpcMessage message = new GrpcMessage();

        assertThat(message.getMethod()).isNull();
        assertThat(message.getStatusCode()).isNull();
        assertThat(message.getStreamSequence()).isNull();
        assertThat(message.isEndOfStream()).isNull();
    }
}
