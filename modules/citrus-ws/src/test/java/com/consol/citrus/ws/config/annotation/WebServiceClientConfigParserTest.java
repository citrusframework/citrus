/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.ws.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.endpoint.resolver.EndpointUriResolver;
import com.consol.citrus.message.*;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.message.converter.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class WebServiceClientConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint(name = "wsClient1")
    @WebServiceClientConfig(requestUrl = "http://localhost:8080/test")
    private WebServiceClient client1;

    @CitrusEndpoint
    @WebServiceClientConfig(requestUrl = "http://localhost:8080/test",
            timeout=10000L,
            messageFactory="soapMessageFactory",
            endpointResolver="endpointResolver")
    private WebServiceClient client2;

    @CitrusEndpoint
    @WebServiceClientConfig(requestUrl = "http://localhost:8080/test",
            webServiceTemplate="wsTemplate",
            correlator="replyMessageCorrelator")
    private WebServiceClient client3;

    @CitrusEndpoint
    @WebServiceClientConfig(requestUrl = "http://localhost:8080/test",
            messageSender="wsMessageSender",
            interceptor="singleInterceptor",
            messageConverter="wsAddressingMessageConverter")
    private WebServiceClient client4;

    @CitrusEndpoint
    @WebServiceClientConfig(requestUrl = "http://localhost:8080/test",
            faultStrategy=ErrorHandlingStrategy.PROPAGATE,
            interceptors={ "clientInterceptor1", "clientInterceptor2" },
            pollingInterval=250)
    private WebServiceClient client5;

    @CitrusEndpoint
    @WebServiceClientConfig(requestUrl = "http://localhost:8080/test",
            actor="testActor")
    private WebServiceClient client6;

    @Autowired
    private SpringBeanReferenceResolver referenceResolver;

    @Mock
    private WebServiceTemplate wsTemplate = Mockito.mock(WebServiceTemplate.class);
    @Mock
    private SoapMessageFactory messageFactory = Mockito.mock(SoapMessageFactory.class);
    @Mock
    private WsAddressingMessageConverter messageConverter = Mockito.mock(WsAddressingMessageConverter.class);
    @Mock
    private EndpointUriResolver endpointResolver = Mockito.mock(EndpointUriResolver.class);
    @Mock
    private WebServiceMessageSender messageSender = Mockito.mock(WebServiceMessageSender.class);
    @Mock
    private MessageCorrelator messageCorrelator = Mockito.mock(MessageCorrelator.class);
    @Mock
    private ClientInterceptor clientInterceptor1 = Mockito.mock(ClientInterceptor.class);
    @Mock
    private ClientInterceptor clientInterceptor2 = Mockito.mock(ClientInterceptor.class);
    @Mock
    private TestActor testActor = Mockito.mock(TestActor.class);
    @Mock
    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);

        referenceResolver.setApplicationContext(applicationContext);

        when(applicationContext.getBean("messageFactory", WebServiceMessageFactory.class)).thenReturn(messageFactory);
        when(applicationContext.getBean("soapMessageFactory", WebServiceMessageFactory.class)).thenReturn(messageFactory);
        when(applicationContext.getBean("wsMessageSender", WebServiceMessageSender.class)).thenReturn(messageSender);
        when(applicationContext.getBean("wsAddressingMessageConverter", WebServiceMessageConverter.class)).thenReturn(messageConverter);
        when(applicationContext.getBean("endpointResolver", EndpointUriResolver.class)).thenReturn(endpointResolver);
        when(applicationContext.getBean("wsTemplate", WebServiceTemplate.class)).thenReturn(wsTemplate);
        when(applicationContext.getBean("replyMessageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        when(applicationContext.getBean("testActor", TestActor.class)).thenReturn(testActor);
        when(applicationContext.getBean("singleInterceptor", ClientInterceptor.class)).thenReturn(clientInterceptor1);
        when(applicationContext.getBean("clientInterceptor1", ClientInterceptor.class)).thenReturn(clientInterceptor1);
        when(applicationContext.getBean("clientInterceptor2", ClientInterceptor.class)).thenReturn(clientInterceptor2);
    }

    @Test
    public void testWebServiceClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message sender
        Assert.assertEquals(client1.getEndpointConfiguration().getDefaultUri(), "http://localhost:8080/test");
        Assert.assertTrue(client1.getEndpointConfiguration().getMessageFactory() instanceof SoapMessageFactory);
        Assert.assertEquals(client1.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertNull(client1.getEndpointConfiguration().getInterceptor());
        Assert.assertTrue(client1.getEndpointConfiguration().getMessageConverter() instanceof SoapMessageConverter);
        Assert.assertEquals(client1.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);
        Assert.assertEquals(client1.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertNotNull(client1.getEndpointConfiguration().getWebServiceTemplate());

        // 2nd message sender
        Assert.assertEquals(client2.getEndpointConfiguration().getDefaultUri(), "http://localhost:8080/test");
        Assert.assertEquals(client2.getEndpointConfiguration().getMessageFactory(), messageFactory);
        Assert.assertEquals(client2.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(client2.getEndpointConfiguration().getEndpointResolver(), endpointResolver);
        Assert.assertEquals(client2.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);
        Assert.assertEquals(client2.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertNotNull(client2.getEndpointConfiguration().getWebServiceTemplate());
        Assert.assertEquals(client2.getEndpointConfiguration().getWebServiceTemplate().getMessageFactory(), messageFactory);

        // 3rd message sender
        Assert.assertEquals(client3.getEndpointConfiguration().getDefaultUri(), "http://localhost:8080/test");
        Assert.assertNotNull(client3.getEndpointConfiguration().getCorrelator());
        Assert.assertEquals(client3.getEndpointConfiguration().getCorrelator(), messageCorrelator);
        Assert.assertEquals(client3.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);
        Assert.assertNotNull(client3.getEndpointConfiguration().getWebServiceTemplate());
        Assert.assertEquals(client3.getEndpointConfiguration().getWebServiceTemplate(), wsTemplate);

        // 4th message sender
        Assert.assertEquals(client4.getEndpointConfiguration().getDefaultUri(), "http://localhost:8080/test");
        Assert.assertTrue(client4.getEndpointConfiguration().getMessageFactory() instanceof SoapMessageFactory);
        Assert.assertEquals(client4.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);
        Assert.assertNotNull(client4.getEndpointConfiguration().getMessageSender());
        Assert.assertEquals(client4.getEndpointConfiguration().getMessageSender(), messageSender);
        Assert.assertNotNull(client4.getEndpointConfiguration().getInterceptor());
        Assert.assertEquals(client4.getEndpointConfiguration().getInterceptor(), clientInterceptor1);
        Assert.assertNotNull(client4.getEndpointConfiguration().getWebServiceTemplate());
        Assert.assertEquals(client4.getEndpointConfiguration().getWebServiceTemplate().getInterceptors().length, 1L);
        Assert.assertTrue(client4.getEndpointConfiguration().getMessageConverter() instanceof WsAddressingMessageConverter);

        // 5th message sender
        Assert.assertEquals(client5.getEndpointConfiguration().getDefaultUri(), "http://localhost:8080/test");
        Assert.assertEquals(client5.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.PROPAGATE);
        Assert.assertNotNull(client5.getEndpointConfiguration().getInterceptors());
        Assert.assertEquals(client5.getEndpointConfiguration().getInterceptors().size(), 2L);
        Assert.assertEquals(client5.getEndpointConfiguration().getInterceptors().get(0), clientInterceptor1);
        Assert.assertEquals(client5.getEndpointConfiguration().getInterceptors().get(1), clientInterceptor2);
        Assert.assertEquals(client5.getEndpointConfiguration().getPollingInterval(), 250L);
        Assert.assertNotNull(client5.getEndpointConfiguration().getWebServiceTemplate());
        Assert.assertEquals(client5.getEndpointConfiguration().getWebServiceTemplate().getInterceptors().length, 2L);

        // 5th message sender
        Assert.assertNotNull(client6.getActor());
        Assert.assertEquals(client6.getActor(), testActor);
    }
}
