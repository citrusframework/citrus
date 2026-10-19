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
package org.citrusframework.grpc.config.annotation;

import java.time.Duration;

import org.citrusframework.context.TestContext;
import org.citrusframework.grpc.client.GrpcClient;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GrpcClientConfigParserTest {

    @Test
    public void shouldParseClientConfiguration() {
        GrpcClientConfig config = mock(GrpcClientConfig.class);
        when(config.target()).thenReturn("localhost:50051");
        when(config.descriptors()).thenReturn(new String[] { "classpath:descriptors/test-protos.binpb" });
        when(config.protoPaths()).thenReturn(new String[] { "classpath:proto" });
        when(config.deadline()).thenReturn(2000L);
        when(config.tls()).thenReturn(true);
        when(config.trustCertificate()).thenReturn("classpath:certs/ca.pem");
        when(config.omitDefaultValues()).thenReturn(true);
        when(config.maxBufferedMessages()).thenReturn(25);
        when(config.timeout()).thenReturn(1000L);
        when(config.pollingInterval()).thenReturn(100L);
        when(config.actor()).thenReturn("");

        GrpcClient client = new GrpcClientConfigParser()
                .parse(config, new SimpleReferenceResolver(), new TestContext());

        assertThat(client.getEndpointConfiguration().getTarget()).isEqualTo("localhost:50051");
        assertThat(client.getEndpointConfiguration().getDescriptors())
                .containsExactly("classpath:descriptors/test-protos.binpb");
        assertThat(client.getEndpointConfiguration().getProtoPaths()).containsExactly("classpath:proto");
        assertThat(client.getEndpointConfiguration().getDeadline()).isEqualTo(Duration.ofSeconds(2));
        assertThat(client.getEndpointConfiguration().isTls()).isTrue();
        assertThat(client.getEndpointConfiguration().getTrustCertificate()).isEqualTo("classpath:certs/ca.pem");
        assertThat(client.getEndpointConfiguration().isOmitDefaultValues()).isTrue();
        assertThat(client.getEndpointConfiguration().getMaxBufferedMessages()).isEqualTo(25);
        assertThat(client.getEndpointConfiguration().getTimeout()).isEqualTo(1000L);
        assertThat(client.getEndpointConfiguration().getPollingInterval()).isEqualTo(100L);
    }
}
