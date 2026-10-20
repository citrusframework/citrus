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
package org.citrusframework.grpc.xml;

import java.util.List;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.context.TestContext;
import org.citrusframework.grpc.message.GrpcMessage;
import org.citrusframework.grpc.message.GrpcMessageHeaders;
import org.citrusframework.grpc.server.GrpcServer;
import org.citrusframework.grpc.server.GrpcServerEndpointConfiguration;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Producer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.xml.XmlTestLoader;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GrpcServerTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadGrpcServerActions() {
        GrpcServerEndpointConfiguration configuration = new GrpcServerEndpointConfiguration();
        configuration.setPort(50051);

        Producer producer = mock(Producer.class);
        SelectiveConsumer consumer = mock(SelectiveConsumer.class);
        GrpcServer server = mock(GrpcServer.class);
        when(server.getEndpointConfiguration()).thenReturn(configuration);
        when(server.createProducer()).thenReturn(producer);
        when(server.createConsumer()).thenReturn(consumer);
        when(server.getName()).thenReturn("grpcServer");
        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new GrpcMessage("{\"name\":\"Ada\"}")
                        .method("helloworld.Greeter/SayHello")
                        .methodType(GrpcMessageHeaders.METHOD_TYPE_UNARY)
                        .callId("call-1")
                        .kind(GrpcMessageHeaders.KIND_CALL)
                        .metadata("authorization", "Bearer token"),
                new GrpcMessage("{\"id\":2}")
                        .method("helloworld.Greeter/SayHello")
                        .methodType(GrpcMessageHeaders.METHOD_TYPE_CLIENT_STREAMING)
                        .callId("call-1")
                        .kind(GrpcMessageHeaders.KIND_STREAM_MESSAGE)
                        .streamSequence(1),
                new GrpcMessage("")
                        .method("helloworld.Greeter/SayHello")
                        .methodType(GrpcMessageHeaders.METHOD_TYPE_CLIENT_STREAMING)
                        .callId("call-1")
                        .kind(GrpcMessageHeaders.KIND_STREAM_END)
                        .statusCode("OK"));

        context.getReferenceResolver().bind("grpcServer", server);

        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/grpc/xml/grpc-server-test.xml");
        testLoader.load();

        TestCase result = testLoader.getTestCase();
        assertThat(result.getName()).isEqualTo("GrpcServerTest");
        assertThat(result.getMetaInfo().getStatus()).isEqualTo(TestCaseMetaInfo.Status.FINAL);
        assertThat(result.getActionCount()).isEqualTo(6L);
        assertThat(result.getTestAction(0).getName()).isEqualTo("grpc:receive-call");
        assertThat(result.getTestAction(3).getName()).isEqualTo("grpc:send-response");

        ArgumentCaptor<Message> sent = ArgumentCaptor.forClass(Message.class);
        verify(producer, times(3)).send(sent.capture(), any(TestContext.class));
        List<Message> messages = sent.getAllValues();

        assertThat(messages.get(0).getHeader(GrpcMessageHeaders.KIND))
                .isEqualTo(GrpcMessageHeaders.KIND_RESPONSE);
        assertThat(messages.get(0).getHeader(GrpcMessageHeaders.STATUS_CODE)).isEqualTo("OK");
        assertThat(messages.get(0).getHeader(GrpcMessageHeaders.TRAILER_PREFIX + "x-trailer")).isEqualTo("42");
        assertThat(messages.get(0).getPayload(String.class)).isEqualTo("{\"message\": \"Hello Ada\"}");

        assertThat(messages.get(1).getHeader(GrpcMessageHeaders.KIND))
                .isEqualTo(GrpcMessageHeaders.KIND_STREAM_MESSAGE);
        assertThat(messages.get(2).getHeader(GrpcMessageHeaders.KIND))
                .isEqualTo(GrpcMessageHeaders.KIND_STREAM_END);
        assertThat(messages.get(2).getHeader(GrpcMessageHeaders.STATUS_CODE)).isEqualTo("OK");
    }
}
