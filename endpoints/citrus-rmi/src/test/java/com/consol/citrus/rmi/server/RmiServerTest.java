/*
 *  Copyright 2006-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.consol.citrus.rmi.server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.util.Arrays;

import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.rmi.message.RmiMessage;
import com.consol.citrus.rmi.message.RmiMessageHeaders;
import com.consol.citrus.rmi.remote.HelloService;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class RmiServerTest extends AbstractTestNGUnitTest {

    private final Registry registry = Mockito.mock(Registry.class);
    private final EndpointAdapter endpointAdapter = Mockito.mock(EndpointAdapter.class);

    @Test
    public void testServiceInvocationWithArgument() throws Exception {
        RmiServer rmiServer = new RmiServer();
        rmiServer.setRemoteInterfaces(Arrays.<Class<? extends Remote>>asList(HelloService.class));
        rmiServer.setEndpointAdapter(endpointAdapter);
        rmiServer.getEndpointConfiguration().setRegistry(registry);
        rmiServer.getEndpointConfiguration().setBinding("helloService");

        final Remote[] remote = new Remote[1];

        reset(registry, endpointAdapter);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                remote[0] = (Remote) invocationOnMock.getArguments()[1];
                return null;
            }
        }).when(registry).bind(eq("helloService"), any(Remote.class));

        doAnswer(new Answer<Message>() {
            @Override
            public Message answer(InvocationOnMock invocation) throws Throwable {
                Message message = (Message) invocation.getArguments()[0];

                Assert.assertNotNull(message.getPayload());
                Assert.assertEquals(message.getHeader(RmiMessageHeaders.RMI_INTERFACE), HelloService.class.getName());
                Assert.assertEquals(message.getHeader(RmiMessageHeaders.RMI_METHOD), "sayHello");

                try {
                    Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload(String.class)),
                            StringUtils.trimAllWhitespace(FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("service-invocation.xml",
                                    RmiServer.class).getInputStream()))));
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }

                return RmiMessage.result();
            }
        }).when(endpointAdapter).handleMessage(any(Message.class));

        rmiServer.startup();

        try {
            ((HelloService)remote[0]).sayHello("Hello RMI this is cool!");
        } catch (Throwable throwable) {
            Assert.fail("Faidled to invoke remote service", throwable);
        }
    }

    @Test
    public void testServiceInvocationWithResult() throws Exception {
        RmiServer rmiServer = new RmiServer();
        rmiServer.setRemoteInterfaces(Arrays.<Class<? extends Remote>>asList(HelloService.class));
        rmiServer.setEndpointAdapter(endpointAdapter);
        rmiServer.getEndpointConfiguration().setRegistry(registry);
        rmiServer.getEndpointConfiguration().setBinding("helloService");

        final Remote[] remote = new Remote[1];

        reset(registry, endpointAdapter);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                remote[0] = (Remote) invocationOnMock.getArguments()[1];
                return null;
            }
        }).when(registry).bind(eq("helloService"), any(Remote.class));

        doAnswer(new Answer<Message>() {
            @Override
            public Message answer(InvocationOnMock invocation) throws Throwable {
                Message message = (Message) invocation.getArguments()[0];

                Assert.assertNotNull(message.getPayload());
                Assert.assertEquals(message.getHeader(RmiMessageHeaders.RMI_INTERFACE), HelloService.class.getName());
                Assert.assertEquals(message.getHeader(RmiMessageHeaders.RMI_METHOD), "getHelloCount");

                try {
                    Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload(String.class)),
                            StringUtils.trimAllWhitespace(FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("service-invocation-2.xml",
                                    RmiServer.class).getInputStream()))));
                } catch (IOException e) {
                    Assert.fail(e.getMessage());
                }

                return new DefaultMessage(FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("service-result.xml",
                        RmiServer.class).getInputStream())));
            }
        }).when(endpointAdapter).handleMessage(any(Message.class));

        rmiServer.startup();

        try {
            Assert.assertEquals(((HelloService)remote[0]).getHelloCount(), 10);
        } catch (Throwable throwable) {
            Assert.fail("Faidled to invoke remote service", throwable);
        }
    }
}
