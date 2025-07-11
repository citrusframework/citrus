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

package org.citrusframework.rmi.server;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.util.List;

import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.rmi.message.RmiMessage;
import org.citrusframework.rmi.message.RmiMessageHeaders;
import org.citrusframework.rmi.remote.HelloService;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.util.FileUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

/**
 * @since 2.5
 */
public class RmiServerTest extends AbstractTestNGUnitTest {

    @Mock
    private Registry registry;

    @Mock
    private EndpointAdapter endpointAdapter;

    private AutoCloseable mockitoContext;

    @BeforeMethod
    public void setup() {
        mockitoContext = MockitoAnnotations.openMocks(this);
    }

    @AfterMethod
    public void teardown() throws Exception {
        mockitoContext.close();
    }

    @Test(enabled = false) // unstable in local environments
    public void testServiceInvocationWithArgument() throws Exception {
        RmiServer rmiServer = new RmiServer();
        rmiServer.setRemoteInterfaces(List.of(HelloService.class));
        rmiServer.setEndpointAdapter(endpointAdapter);
        rmiServer.getEndpointConfiguration().setRegistry(registry);
        rmiServer.getEndpointConfiguration().setBinding("helloService");

        final Remote[] remote = new Remote[1];

        reset(registry, endpointAdapter);

        doAnswer(invocationOnMock -> {
            remote[0] = (Remote) invocationOnMock.getArguments()[1];
            return null;
        }).when(registry).bind(eq("helloService"), any(Remote.class));

        doAnswer((Answer<Message>) invocation -> {
            Message message = (Message) invocation.getArguments()[0];

            assertNotNull(message.getPayload());
            assertEquals(message.getHeader(RmiMessageHeaders.RMI_INTERFACE), HelloService.class.getName());
            assertEquals(message.getHeader(RmiMessageHeaders.RMI_METHOD), "sayHello");

            try {
                assertEquals(
                    message.getPayload(String.class).replaceAll("\\s", ""),
                    FileUtils.readToString(Resources.create("service-invocation.xml", RmiServer.class)).replaceAll("\\s", "")
                );
            } catch (IOException e) {
                fail(e.getMessage());
            }

            return RmiMessage.result();
        }).when(endpointAdapter).handleMessage(any(Message.class));

        rmiServer.startup();

        try {
            ((HelloService)remote[0]).sayHello("Hello RMI this is cool!");
        } catch (Throwable throwable) {
            fail("Failed to invoke remote service", throwable);
        }
    }

    @Test(enabled = false) // unstable in local environments
    public void testServiceInvocationWithResult() throws Exception {
        RmiServer rmiServer = new RmiServer();
        rmiServer.setRemoteInterfaces(List.of(HelloService.class));
        rmiServer.setEndpointAdapter(endpointAdapter);
        rmiServer.getEndpointConfiguration().setRegistry(registry);
        rmiServer.getEndpointConfiguration().setBinding("helloService");

        final Remote[] remote = new Remote[1];

        reset(registry, endpointAdapter);

        doAnswer(invocationOnMock -> {
            remote[0] = (Remote) invocationOnMock.getArguments()[1];
            return null;
        }).when(registry).bind(eq("helloService"), any(Remote.class));

        doAnswer((Answer<Message>) invocation -> {
            Message message = (Message) invocation.getArguments()[0];

            assertNotNull(message.getPayload());
            assertEquals(message.getHeader(RmiMessageHeaders.RMI_INTERFACE), HelloService.class.getName());
            assertEquals(message.getHeader(RmiMessageHeaders.RMI_METHOD), "getHelloCount");

            try {
                assertEquals(
                    message.getPayload(String.class).replaceAll("\\s", ""),
                    FileUtils.readToString(Resources.create("service-invocation-2.xml", RmiServer.class)).replaceAll("\\s", "")
                );
            } catch (IOException e) {
                fail(e.getMessage());
            }

            return new DefaultMessage(FileUtils.readToString(Resources.create("service-result.xml", RmiServer.class)));
        }).when(endpointAdapter).handleMessage(any(Message.class));

        rmiServer.startup();

        try {
            assertEquals(((HelloService)remote[0]).getHelloCount(), 10);
        } catch (Throwable throwable) {
            fail("Failed to invoke remote service", throwable);
        }
    }
}
