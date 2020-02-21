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

package com.consol.citrus.rmi.client;

import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageCorrelator;
import com.consol.citrus.rmi.endpoint.RmiEndpointConfiguration;
import com.consol.citrus.rmi.message.RmiMessage;
import com.consol.citrus.rmi.remote.HelloService;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStreamReader;
import java.rmi.registry.Registry;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class RmiClientTest extends AbstractTestNGUnitTest {

    private HelloService remoteInterface = Mockito.mock(HelloService.class);
    private Registry registry = Mockito.mock(Registry.class);

    @Test
    public void testRmiClient() throws Exception {
        RmiEndpointConfiguration endpointConfiguration = new RmiEndpointConfiguration();
        RmiClient rmiClient = new RmiClient(endpointConfiguration);
        String binding = "helloService";

        final String responseBody = FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("service-result.xml",
                RmiClient.class).getInputStream()));

        endpointConfiguration.setBinding(binding);

        Message requestMessage = RmiMessage.invocation("getHelloCount");

        endpointConfiguration.setRegistry(registry);

        reset(registry, remoteInterface);

        when(registry.lookup(binding)).thenReturn(remoteInterface);
        when(remoteInterface.getHelloCount()).thenReturn(100);

        rmiClient.send(requestMessage, context);

        Message responseMessage = rmiClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(StringUtils.trimAllWhitespace(responseMessage.getPayload(String.class)),
                StringUtils.trimAllWhitespace(responseBody));
    }

    @Test
    public void testRmiClientWithArgument() throws Exception {
        RmiEndpointConfiguration endpointConfiguration = new RmiEndpointConfiguration();
        RmiClient rmiClient = new RmiClient(endpointConfiguration);
        String binding = "helloService";

        final String responseBody = FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("service-result-2.xml",
                RmiClient.class).getInputStream()));

        endpointConfiguration.setBinding(binding);

        Message requestMessage = RmiMessage.invocation("sayHello").argument("Christoph");

        endpointConfiguration.setRegistry(registry);

        reset(registry, remoteInterface);

        when(registry.lookup(binding)).thenReturn(remoteInterface);

        rmiClient.send(requestMessage, context);

        Message responseMessage = rmiClient.receive(context, endpointConfiguration.getTimeout());
        Assert.assertEquals(StringUtils.trimAllWhitespace(responseMessage.getPayload(String.class)),
                StringUtils.trimAllWhitespace(responseBody));

        verify(remoteInterface).sayHello(eq("Christoph"));
    }

    @Test
    public void testReplyMessageCorrelator() throws Exception {
        RmiEndpointConfiguration endpointConfiguration = new RmiEndpointConfiguration();
        RmiClient rmiClient = new RmiClient(endpointConfiguration);
        String binding = "helloService";

        String responseBody = FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("service-result-3.xml",
                RmiClient.class).getInputStream()));

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
        Assert.assertEquals(StringUtils.trimAllWhitespace(responseMessage.getPayload(String.class)),
                StringUtils.trimAllWhitespace(responseBody));
    }

}
