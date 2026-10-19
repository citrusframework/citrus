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

import io.grpc.Status;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.grpc.message.GrpcMessage;
import org.citrusframework.grpc.message.GrpcMessageHeaders;
import org.citrusframework.grpc.server.GrpcServer;
import org.citrusframework.grpc.server.GrpcServerEndpointConfiguration;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Producer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GrpcServerActionBuilderTest extends AbstractTestNGUnitTest {

    private GrpcServer server;
    private Producer producer;
    private SelectiveConsumer consumer;

    @BeforeMethod
    public void setupServer() {
        producer = mock(Producer.class);
        consumer = mock(SelectiveConsumer.class);
        server = mock(GrpcServer.class);
        when(server.getEndpointConfiguration()).thenReturn(new GrpcServerEndpointConfiguration());
        when(server.createProducer()).thenReturn(producer);
        when(server.createConsumer()).thenReturn(consumer);
        when(server.getName()).thenReturn("grpcServer");
    }

    @Test
    public void shouldReceiveCallAndValidateMethodMetadataAndBody() {
        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(new GrpcMessage("{ \"name\": \"Ada\" }")
                .method("helloworld.Greeter/SayHello")
                .methodType(GrpcMessageHeaders.METHOD_TYPE_UNARY)
                .callId("call-1")
                .kind(GrpcMessageHeaders.KIND_CALL)
                .setHeader("authorization", "Bearer abc"));

        new GrpcServerActionBuilder(server).receive()
                .call("helloworld.Greeter/SayHello")
                .metadata("authorization", "@startsWith('Bearer ')@")
                .message().body("{ \"name\": \"@ignore@\" }")
                .build().execute(context);
    }

    @Test
    public void shouldFailWhenCallMethodDiffers() {
        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(new GrpcMessage("{ \"name\": \"Ada\" }")
                .method("helloworld.Greeter/SayHello")
                .methodType(GrpcMessageHeaders.METHOD_TYPE_UNARY)
                .callId("call-1")
                .kind(GrpcMessageHeaders.KIND_CALL));

        assertThatThrownBy(() -> new GrpcServerActionBuilder(server).receive()
                .call("helloworld.Greeter/GetUser")
                .build().execute(context))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining(GrpcMessageHeaders.METHOD);
    }

    @Test
    public void shouldSendResponse() {
        new GrpcServerActionBuilder(server).send()
                .response()
                .message().body("{ \"message\": \"Hi\" }")
                .build().execute(context);

        Message sent = sentMessage();
        assertThat(sent.getPayload(String.class)).isEqualTo("{ \"message\": \"Hi\" }");
        assertThat(sent.getHeader(GrpcMessageHeaders.KIND)).isEqualTo(GrpcMessageHeaders.KIND_RESPONSE);
        assertThat(sent.getHeader(GrpcMessageHeaders.STATUS_CODE)).isEqualTo("OK");
    }

    @Test
    public void shouldSendErrorStatusWithDescriptionAndTrailer() {
        new GrpcServerActionBuilder(server).send()
                .response(Status.Code.NOT_FOUND)
                .statusDescription("unknown user")
                .trailer("x-request-id", "42")
                .build().execute(context);

        Message sent = sentMessage();
        assertThat(sent.getHeader(GrpcMessageHeaders.KIND)).isEqualTo(GrpcMessageHeaders.KIND_RESPONSE);
        assertThat(sent.getHeader(GrpcMessageHeaders.STATUS_CODE)).isEqualTo("NOT_FOUND");
        assertThat(sent.getHeader(GrpcMessageHeaders.STATUS_DESCRIPTION)).isEqualTo("unknown user");
        assertThat(sent.getHeader(GrpcMessageHeaders.TRAILER_PREFIX + "x-request-id")).isEqualTo("42");
    }

    @Test
    public void shouldStreamMessagesAndCompleteWithStatus() {
        GrpcServerActionBuilder builder = new GrpcServerActionBuilder(server);
        builder.send().streamMessage().message().body("{ \"symbol\": \"ACME\" }").build().execute(context);
        builder.send().streamCompleted(Status.Code.UNAVAILABLE).statusDescription("market closed").build().execute(context);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(producer, org.mockito.Mockito.times(2)).send(captor.capture(), any(TestContext.class));

        assertThat(captor.getAllValues().get(0).getHeader(GrpcMessageHeaders.KIND))
                .isEqualTo(GrpcMessageHeaders.KIND_STREAM_MESSAGE);
        assertThat(captor.getAllValues().get(1).getHeader(GrpcMessageHeaders.KIND))
                .isEqualTo(GrpcMessageHeaders.KIND_STREAM_END);
        assertThat(captor.getAllValues().get(1).getHeader(GrpcMessageHeaders.STATUS_CODE)).isEqualTo("UNAVAILABLE");
        assertThat(captor.getAllValues().get(1).getHeader(GrpcMessageHeaders.STATUS_DESCRIPTION)).isEqualTo("market closed");
    }

    @Test
    public void shouldNameActions() {
        assertThat(new GrpcServerActionBuilder(server).receive().build().getName()).isEqualTo("grpc:receive-call");
        assertThat(new GrpcServerActionBuilder(server).send().response().build().getName()).isEqualTo("grpc:send-response");
    }

    private Message sentMessage() {
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(producer).send(captor.capture(), any(TestContext.class));
        return captor.getValue();
    }
}
