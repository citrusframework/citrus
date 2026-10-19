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
package org.citrusframework.grpc.actions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import io.grpc.Status;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.grpc.client.GrpcClient;
import org.citrusframework.grpc.message.GrpcMessage;
import org.citrusframework.grpc.message.GrpcMessageHeaders;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Producer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GrpcClientActionBuilderTest extends AbstractTestNGUnitTest {

    private GrpcClient client;
    private Producer producer;
    private SelectiveConsumer consumer;

    private final List<Message> sent = new ArrayList<>();

    @BeforeMethod
    public void setUp() {
        client = mock(GrpcClient.class);
        producer = mock(Producer.class);
        consumer = mock(SelectiveConsumer.class);

        when(client.createProducer()).thenReturn(producer);
        when(client.createConsumer()).thenReturn(consumer);
        when(client.getEndpointConfiguration()).thenReturn(new org.citrusframework.grpc.client.GrpcClientEndpointConfiguration());
        doAnswer(invocation -> {
            sent.add(invocation.getArgument(0));
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        sent.clear();
    }

    @Test
    public void shouldSendUnaryRequest() {
        GrpcClientActionBuilder grpc = new GrpcClientActionBuilder(client);
        grpc.send()
                .call("helloworld.Greeter/SayHello")
                .metadata("authorization", "Bearer token")
                .deadline(Duration.ofSeconds(2))
                .message()
                .body("{ \"name\": \"Ada\" }");

        grpc.build().execute(context);

        assertThat(sent).hasSize(1);
        Message request = sent.get(0);
        assertThat(request.getHeader(GrpcMessageHeaders.METHOD)).isEqualTo("helloworld.Greeter/SayHello");
        assertThat(request.getHeader("authorization")).isEqualTo("Bearer token");
        assertThat(request.getHeader(GrpcMessageHeaders.DEADLINE)).isEqualTo(Duration.ofSeconds(2));
        assertThat(request.getPayload()).isEqualTo("{ \"name\": \"Ada\" }");
    }

    @Test
    public void shouldReceiveUnaryResponse() {
        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(response("OK"));
        when(consumer.receive(any(TestContext.class))).thenReturn(response("OK"));

        GrpcClientActionBuilder grpc = new GrpcClientActionBuilder(client);
        grpc.receive().response().message().body("{ \"message\": \"Hello Ada\" }");

        grpc.build().execute(context);
    }

    @Test
    public void shouldFailOnUnexpectedErrorStatus() {
        when(consumer.receive(any(TestContext.class), anyLong()))
                .thenReturn(response("NOT_FOUND").statusDescription("unknown user"));
        when(consumer.receive(any(TestContext.class)))
                .thenReturn(response("NOT_FOUND").statusDescription("unknown user"));

        GrpcClientActionBuilder grpc = new GrpcClientActionBuilder(client);
        grpc.receive().response();

        assertThatThrownBy(() -> grpc.build().execute(context))
                .hasMessageContaining("NOT_FOUND")
                .hasMessageContaining("unknown user");
    }

    @Test
    public void shouldReceiveExpectedErrorStatus() {
        GrpcMessage message = response("NOT_FOUND").statusDescription("unknown user");
        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(consumer.receive(any(TestContext.class))).thenReturn(message);

        GrpcClientActionBuilder grpc = new GrpcClientActionBuilder(client);
        grpc.receive()
                .response(Status.Code.NOT_FOUND)
                .statusDescription("@contains('unknown')@");

        grpc.build().execute(context);
    }

    @Test
    public void shouldReceiveStreamMessage() {
        GrpcMessage message = grpcMessage("{ \"symbol\": \"ACME\" }", GrpcMessageHeaders.KIND_STREAM_MESSAGE)
                .methodType(GrpcMessageHeaders.METHOD_TYPE_SERVER_STREAMING)
                .streamSequence(0);
        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(consumer.receive(any(TestContext.class))).thenReturn(message);

        GrpcClientActionBuilder grpc = new GrpcClientActionBuilder(client);
        grpc.receive().streamMessage().message().body("{ \"symbol\": \"ACME\" }");

        grpc.build().execute(context);
    }

    @Test
    public void shouldFailStreamMessageOnUnaryMethod() {
        GrpcMessage message = response("OK");
        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(consumer.receive(any(TestContext.class))).thenReturn(message);

        GrpcClientActionBuilder grpc = new GrpcClientActionBuilder(client);
        grpc.receive().streamMessage();

        assertThatThrownBy(() -> grpc.build().execute(context))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("is unary");
    }

    @Test
    public void shouldFailResponseOnStreamingMethod() {
        GrpcMessage message = grpcMessage("{ \"symbol\": \"ACME\" }", GrpcMessageHeaders.KIND_STREAM_MESSAGE)
                .methodType(GrpcMessageHeaders.METHOD_TYPE_SERVER_STREAMING);
        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(consumer.receive(any(TestContext.class))).thenReturn(message);

        GrpcClientActionBuilder grpc = new GrpcClientActionBuilder(client);
        grpc.receive().response();

        assertThatThrownBy(() -> grpc.build().execute(context))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("is server-streaming");
    }

    @Test
    public void shouldReceiveStreamCompletion() {
        GrpcMessage message = grpcMessage("", GrpcMessageHeaders.KIND_STREAM_END)
                .methodType(GrpcMessageHeaders.METHOD_TYPE_SERVER_STREAMING)
                .statusCode("OK")
                .endOfStream(true);
        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(consumer.receive(any(TestContext.class))).thenReturn(message);

        GrpcClientActionBuilder grpc = new GrpcClientActionBuilder(client);
        grpc.receive().streamCompleted();

        grpc.build().execute(context);
    }

    @Test
    public void shouldFailStreamCompletionWithUnreadMessages() {
        GrpcMessage message = grpcMessage("{ \"symbol\": \"ACME\" }", GrpcMessageHeaders.KIND_STREAM_MESSAGE)
                .methodType(GrpcMessageHeaders.METHOD_TYPE_SERVER_STREAMING);
        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(consumer.receive(any(TestContext.class))).thenReturn(message);

        GrpcClientActionBuilder grpc = new GrpcClientActionBuilder(client);
        grpc.receive().streamCompleted();

        assertThatThrownBy(() -> grpc.build().execute(context))
                .isInstanceOf(CitrusRuntimeException.class)
                .hasMessageContaining("unread messages");
    }

    @Test
    public void shouldCancelCurrentCall() {
        GrpcClientActionBuilder grpc = new GrpcClientActionBuilder(client);
        grpc.cancel();

        grpc.build().execute(context);

        verify(client).cancel(context);
    }

    @Test
    public void shouldNameActions() {
        assertThat(new GrpcClientActionBuilder(client).send().getName()).isEqualTo("grpc:send-request");
        assertThat(new GrpcClientActionBuilder(client).receive().getName()).isEqualTo("grpc:receive-response");
        assertThat(new GrpcClientActionBuilder(client).cancel().getName()).isEqualTo("grpc:cancel");
    }

    private static GrpcMessage response(String statusCode) {
        return grpcMessage("{ \"message\": \"Hello Ada\" }", GrpcMessageHeaders.KIND_RESPONSE)
                .statusCode(statusCode);
    }

    private static GrpcMessage grpcMessage(String payload, String kind) {
        return new GrpcMessage(payload)
                .method("helloworld.Greeter/SayHello")
                .methodType(GrpcMessageHeaders.METHOD_TYPE_UNARY)
                .callId("call-1")
                .kind(kind)
                .statusCode("OK");
    }
}
