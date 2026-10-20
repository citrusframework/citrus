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
package org.citrusframework.grpc.yaml;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.context.TestContext;
import org.citrusframework.grpc.client.GrpcClient;
import org.citrusframework.grpc.client.GrpcClientEndpointConfiguration;
import org.citrusframework.grpc.message.GrpcMessage;
import org.citrusframework.grpc.message.GrpcMessageHeaders;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Producer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.yaml.YamlTestLoader;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GrpcClientTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadGrpcClientActions() {
        GrpcClientEndpointConfiguration configuration = new GrpcClientEndpointConfiguration();
        configuration.setTarget("localhost:50051");

        Producer producer = mock(Producer.class);
        SelectiveConsumer consumer = mock(SelectiveConsumer.class);
        GrpcClient client = mock(GrpcClient.class);
        when(client.getEndpointConfiguration()).thenReturn(configuration);
        when(client.createProducer()).thenReturn(producer);
        when(client.createConsumer()).thenReturn(consumer);
        when(client.getName()).thenReturn("grpcClient");
        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new GrpcMessage("{\"message\":\"Hello Ada\"}")
                        .method("helloworld.Greeter/SayHello")
                        .methodType(GrpcMessageHeaders.METHOD_TYPE_UNARY)
                        .callId("call-1")
                        .kind(GrpcMessageHeaders.KIND_RESPONSE)
                        .statusCode("OK")
                        .endOfStream(true)
                        .trailer("x-trailer", "42"));

        context.getReferenceResolver().bind("grpcClient", client);

        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/grpc/yaml/grpc-client-test.yaml");
        testLoader.load();

        TestCase result = testLoader.getTestCase();
        assertThat(result.getName()).isEqualTo("GrpcClientTest");
        assertThat(result.getMetaInfo().getStatus()).isEqualTo(TestCaseMetaInfo.Status.FINAL);
        assertThat(result.getActionCount()).isEqualTo(4L);
        assertThat(result.getTestAction(0).getName()).isEqualTo("grpc:send-request");
        assertThat(result.getTestAction(3).getName()).isEqualTo("grpc:receive-response");

        ArgumentCaptor<Message> sent = ArgumentCaptor.forClass(Message.class);
        verify(producer, times(3)).send(sent.capture(), any(TestContext.class));
        java.util.List<Message> messages = sent.getAllValues();

        Message request = messages.get(0);
        assertThat(request.getHeader(GrpcMessageHeaders.METHOD)).isEqualTo("helloworld.Greeter/SayHello");
        assertThat(request.getHeader("authorization")).isEqualTo("Bearer token");
        assertThat(request.getHeader(GrpcMessageHeaders.DEADLINE)).isEqualTo(java.time.Duration.ofSeconds(2));
        assertThat(request.getPayload(String.class)).isEqualTo("{\"name\": \"Ada\"}");
        assertThat(messages.get(1).getHeader(GrpcMessageHeaders.KIND))
                .isEqualTo(GrpcMessageHeaders.KIND_STREAM_MESSAGE);
        assertThat(messages.get(2).getHeader(GrpcMessageHeaders.KIND))
                .isEqualTo(GrpcMessageHeaders.KIND_STREAM_END);
    }
}
