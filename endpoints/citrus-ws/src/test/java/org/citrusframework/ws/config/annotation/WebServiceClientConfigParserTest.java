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

package org.citrusframework.ws.config.annotation;

import java.util.Arrays;
import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.http.config.annotation.HttpClientConfigParser;
import org.citrusframework.http.config.annotation.HttpServerConfigParser;
import org.citrusframework.jms.config.annotation.JmsEndpointConfigParser;
import org.citrusframework.jms.config.annotation.JmsSyncEndpointConfigParser;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.interceptor.LoggingClientInterceptor;
import org.citrusframework.ws.message.converter.SoapMessageConverter;
import org.citrusframework.ws.message.converter.WebServiceMessageConverter;
import org.citrusframework.ws.message.converter.WsAddressingMessageConverter;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
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

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private WebServiceTemplate wsTemplate;
    @Mock
    private SoapMessageFactory messageFactory;
    @Mock
    private WsAddressingMessageConverter messageConverter;
    @Mock
    private EndpointUriResolver endpointResolver;
    @Mock
    private WebServiceMessageSender messageSender;
    @Mock
    private MessageCorrelator messageCorrelator;
    @Mock
    private ClientInterceptor clientInterceptor1;
    @Mock
    private ClientInterceptor clientInterceptor2;
    @Mock
    private TestActor testActor;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("messageFactory", WebServiceMessageFactory.class)).thenReturn(messageFactory);
        when(referenceResolver.resolve("soapMessageFactory", WebServiceMessageFactory.class)).thenReturn(messageFactory);
        when(referenceResolver.resolve("wsMessageSender", WebServiceMessageSender.class)).thenReturn(messageSender);
        when(referenceResolver.resolve("wsAddressingMessageConverter", WebServiceMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("endpointResolver", EndpointUriResolver.class)).thenReturn(endpointResolver);
        when(referenceResolver.resolve("wsTemplate", WebServiceTemplate.class)).thenReturn(wsTemplate);
        when(referenceResolver.resolve("replyMessageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
        when(referenceResolver.resolve("singleInterceptor", ClientInterceptor.class)).thenReturn(clientInterceptor1);
        when(referenceResolver.resolve("clientInterceptor1", ClientInterceptor.class)).thenReturn(clientInterceptor1);
        when(referenceResolver.resolve("clientInterceptor2", ClientInterceptor.class)).thenReturn(clientInterceptor2);
        when(referenceResolver.resolve(new String[] { "clientInterceptor1", "clientInterceptor2" }, ClientInterceptor.class)).thenReturn(Arrays.asList(clientInterceptor1, clientInterceptor2));
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testWebServiceClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message sender
        Assert.assertEquals(client1.getEndpointConfiguration().getDefaultUri(), "http://localhost:8080/test");
        Assert.assertTrue(client1.getEndpointConfiguration().getMessageFactory() instanceof SoapMessageFactory);
        Assert.assertEquals(client1.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(client1.getEndpointConfiguration().getInterceptors().size(), 1L);
        Assert.assertEquals(client1.getEndpointConfiguration().getInterceptors().get(0).getClass(), LoggingClientInterceptor.class);
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
        Assert.assertEquals(client4.getEndpointConfiguration().getInterceptors().size(), 1L);
        Assert.assertEquals(client4.getEndpointConfiguration().getInterceptors().get(0), clientInterceptor1);
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

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser> validators = AnnotationConfigParser.lookup();
        Assert.assertEquals(validators.size(), 8L);
        Assert.assertNotNull(validators.get("direct.async"));
        Assert.assertEquals(validators.get("direct.async").getClass(), DirectEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("direct.sync"));
        Assert.assertEquals(validators.get("direct.sync").getClass(), DirectSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("jms.async"));
        Assert.assertEquals(validators.get("jms.async").getClass(), JmsEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("jms.sync"));
        Assert.assertEquals(validators.get("jms.sync").getClass(), JmsSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("http.client"));
        Assert.assertEquals(validators.get("http.client").getClass(), HttpClientConfigParser.class);
        Assert.assertNotNull(validators.get("http.server"));
        Assert.assertEquals(validators.get("http.server").getClass(), HttpServerConfigParser.class);
        Assert.assertNotNull(validators.get("soap.client"));
        Assert.assertEquals(validators.get("soap.client").getClass(), WebServiceClientConfigParser.class);
        Assert.assertNotNull(validators.get("soap.server"));
        Assert.assertEquals(validators.get("soap.server").getClass(), WebServiceServerConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("soap.client").isPresent());
    }
}
