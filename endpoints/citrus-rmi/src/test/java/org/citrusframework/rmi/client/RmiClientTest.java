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

package org.citrusframework.rmi.client;

import java.rmi.registry.Registry;

import org.citrusframework.message.Message;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.rmi.endpoint.RmiEndpointConfiguration;
import org.citrusframework.rmi.message.RmiMessage;
import org.citrusframework.rmi.remote.HelloService;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.util.FileUtils;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class RmiClientTest extends AbstractTestNGUnitTest {

    @Mock
    private HelloService remoteInterface;
    @Mock
    private Registry registry;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRmiClient() throws Exception {
        RmiEndpointConfiguration endpointConfiguration = new RmiEndpointConfiguration();
        RmiClient rmiClient = new RmiClient(endpointConfiguration);
        String binding = "helloService";

        final String responseBody = FileUtils.readToString(Resources.create("service-result.xml", RmiClient.class));

        endpointConfiguration.setBinding(binding);

        Message requestMessage = RmiMessage.invocation("getHelloCount");

        endpointConfiguration.setRegistry(registry);

        reset(registry, remoteInterface);

        when(registry.lookup(binding)).thenReturn(remoteInterface);
        when(remoteInterface.getHelloCount()).thenReturn(100);

        rmiClient.send(requestMessage, context);

        Message responseMessage = rmiClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(
                responseMessage.getPayload(String.class).replaceAll("\\s", ""),
                responseBody.replaceAll("\\s", "")
        );
    }

    @Test
    public void testRmiClientWithArgument() throws Exception {
        RmiEndpointConfiguration endpointConfiguration = new RmiEndpointConfiguration();
        RmiClient rmiClient = new RmiClient(endpointConfiguration);
        String binding = "helloService";

        final String responseBody = FileUtils.readToString(Resources.create("service-result-2.xml", RmiClient.class));

        endpointConfiguration.setBinding(binding);

        Message requestMessage = RmiMessage.invocation("sayHello").argument("Christoph");

        endpointConfiguration.setRegistry(registry);

        reset(registry, remoteInterface);

        when(registry.lookup(binding)).thenReturn(remoteInterface);

        rmiClient.send(requestMessage, context);

        Message responseMessage = rmiClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(
                responseMessage.getPayload(String.class).replaceAll("\\s", ""),
                responseBody.replaceAll("\\s", "")
        );

        verify(remoteInterface).sayHello(eq("Christoph"));
    }

    @Test
    public void testReplyMessageCorrelator() throws Exception {
        RmiEndpointConfiguration endpointConfiguration = new RmiEndpointConfiguration();
        RmiClient rmiClient = new RmiClient(endpointConfiguration);
        String binding = "helloService";

        String responseBody = FileUtils.readToString(Resources.create("service-result-3.xml", RmiClient.class));

        endpointConfiguration.setBinding(binding);

        MessageCorrelator correlator = Mockito.mock(MessageCorrelator.class);
        endpointConfiguration.setCorrelator(correlator);

        Message requestMessage = RmiMessage.invocation("getHelloCount");

        endpointConfiguration.setRegistry(registry);

        reset(registry, remoteInterface, correlator);

        when(registry.lookup(binding)).thenReturn(remoteInterface);

        when(remoteInterface.getHelloCount())
                .thenReturn(99);

        when(correlator.getCorrelationKey(requestMessage)).thenReturn("correlationKey");
        when(correlator.getCorrelationKeyName(any(String.class))).thenReturn("correlationKeyName");

        rmiClient.send(requestMessage, context);

        Message responseMessage = rmiClient.receive("correlationKey", context, endpointConfiguration.getTimeout());
        Assert.assertEquals(
                responseMessage.getPayload(String.class).replaceAll("\\s", ""),
                responseBody.replaceAll("\\s", "")
        );
    }

}
