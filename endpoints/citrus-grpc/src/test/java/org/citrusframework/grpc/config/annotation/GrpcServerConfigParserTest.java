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

import org.citrusframework.context.TestContext;
import org.citrusframework.grpc.server.GrpcServer;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GrpcServerConfigParserTest {

    @Test
    public void shouldParseServerConfiguration() {
        GrpcServerConfig config = mock(GrpcServerConfig.class);
        when(config.port()).thenReturn(50051);
        when(config.services()).thenReturn(new String[] { "helloworld.Greeter" });
        when(config.descriptors()).thenReturn(new String[] { "classpath:descriptors/test-protos.binpb" });
        when(config.protoPaths()).thenReturn(new String[0]);
        when(config.tls()).thenReturn(false);
        when(config.certificateChain()).thenReturn("");
        when(config.privateKey()).thenReturn("");
        when(config.omitDefaultValues()).thenReturn(true);
        when(config.timeout()).thenReturn(2000L);
        when(config.autoStart()).thenReturn(false);
        when(config.endpointAdapter()).thenReturn("");
        when(config.actor()).thenReturn("");

        GrpcServer server = new GrpcServerConfigParser()
                .parse(config, new SimpleReferenceResolver(), new TestContext());

        assertThat(server.getEndpointConfiguration().getPort()).isEqualTo(50051);
        assertThat(server.getEndpointConfiguration().getServices()).containsExactly("helloworld.Greeter");
        assertThat(server.getEndpointConfiguration().getDescriptors())
                .containsExactly("classpath:descriptors/test-protos.binpb");
        assertThat(server.getEndpointConfiguration().isOmitDefaultValues()).isTrue();
        assertThat(server.getEndpointConfiguration().getTimeout()).isEqualTo(2000L);
        assertThat(server.isAutoStart()).isFalse();
        assertThat(server.isRunning()).isFalse();
        assertThat(server.getEndpointAdapter()).isNotNull();
    }
}
