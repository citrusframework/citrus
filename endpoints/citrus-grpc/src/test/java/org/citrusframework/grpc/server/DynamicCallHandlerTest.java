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

package org.citrusframework.grpc.server;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import io.grpc.MethodDescriptor;
import io.grpc.ServerMethodDefinition;
import org.citrusframework.grpc.descriptor.GrpcDescriptorRegistry;
import org.citrusframework.grpc.message.GrpcMessageCodec;
import org.citrusframework.message.DefaultMessageQueue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DynamicCallHandlerTest {

    private ScheduledExecutorService scheduler;

    private GrpcServerEndpointConfiguration configuration;
    private GrpcDescriptorRegistry descriptorRegistry;
    private GrpcMessageCodec codec;

    @BeforeMethod
    public void setUp() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        configuration = new GrpcServerEndpointConfiguration();
        configuration.setDescriptors(List.of("classpath:descriptors/test-protos.binpb"));

        descriptorRegistry = new GrpcDescriptorRegistry(configuration.getDescriptors());
        codec = new GrpcMessageCodec(descriptorRegistry);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        scheduler.shutdownNow();
    }

    @Test
    public void shouldReturnNullForServiceThatIsNotConfigured() {
        configuration.setServices(List.of("helloworld.Greeter"));

        DynamicCallHandler handler = handler();

        assertThat(handler.lookupMethod("market.Prices/Subscribe", null)).isNull();
    }

    @Test
    public void shouldReturnNullForUnknownMethod() {
        DynamicCallHandler handler = handler();

        assertThat(handler.lookupMethod("helloworld.Greeter/SayGoodbye", null)).isNull();
    }

    @Test
    public void shouldReturnUnaryMethodDefinition() {
        ServerMethodDefinition<?, ?> definition = handler().lookupMethod("helloworld.Greeter/SayHello", null);

        assertThat(definition).isNotNull();
        assertThat(definition.getMethodDescriptor().getFullMethodName()).isEqualTo("helloworld.Greeter/SayHello");
        assertThat(definition.getMethodDescriptor().getType()).isEqualTo(MethodDescriptor.MethodType.UNARY);
        assertThat(definition.getServerCallHandler()).isNotNull();
    }

    @Test
    public void shouldReturnStreamingMethodDefinition() {
        ServerMethodDefinition<?, ?> definition = handler().lookupMethod("market.Prices/Subscribe", null);

        assertThat(definition).isNotNull();
        assertThat(definition.getMethodDescriptor().getFullMethodName()).isEqualTo("market.Prices/Subscribe");
        assertThat(definition.getMethodDescriptor().getType()).isEqualTo(MethodDescriptor.MethodType.SERVER_STREAMING);
    }

    @Test
    public void shouldServeAllDescriptorServicesByDefault() {
        assertThat(handler().lookupMethod("market.Prices/Subscribe", null)).isNotNull();
        assertThat(handler().lookupMethod("helloworld.Greeter/SayHello", null)).isNotNull();
    }

    private DynamicCallHandler handler() {
        return new DynamicCallHandler(configuration, descriptorRegistry, codec, new ServerCallRegistry(),
                new DefaultMessageQueue("test.inbound"), null, false, scheduler);
    }
}
