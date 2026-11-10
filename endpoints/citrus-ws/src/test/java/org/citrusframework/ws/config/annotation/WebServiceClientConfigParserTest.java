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

package org.citrusframework.ws.config.annotation;

import java.util.Arrays;
import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
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
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.interceptor.LoggingClientInterceptor;
import org.citrusframework.ws.message.converter.SoapMessageConverter;
import org.citrusframework.ws.message.converter.WebServiceMessageConverter;
import org.citrusframework.ws.message.converter.WsAddressingMessageConverter;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.citrusframework.ws.client.WsTestUtils.getInterceptors;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class WebServiceClientConfigParserTest extends AbstractUnitTest {

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

    @BeforeEach
    @Override
    public void prepareTest() {
        super.prepareTest();

        lenient().when(referenceResolver.resolve("messageFactory", WebServiceMessageFactory.class)).thenReturn(messageFactory);
        lenient().when(referenceResolver.resolve("soapMessageFactory", WebServiceMessageFactory.class)).thenReturn(messageFactory);
        lenient().when(referenceResolver.resolve("wsMessageSender", WebServiceMessageSender.class)).thenReturn(messageSender);
        lenient().when(referenceResolver.resolve("wsAddressingMessageConverter", WebServiceMessageConverter.class)).thenReturn(messageConverter);
        lenient().when(referenceResolver.resolve("endpointResolver", EndpointUriResolver.class)).thenReturn(endpointResolver);
        lenient().when(referenceResolver.resolve("wsTemplate", WebServiceTemplate.class)).thenReturn(wsTemplate);
        lenient().when(referenceResolver.resolve("replyMessageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
        lenient().when(referenceResolver.resolve("singleInterceptor", ClientInterceptor.class)).thenReturn(clientInterceptor1);
        lenient().when(referenceResolver.resolve("clientInterceptor1", ClientInterceptor.class)).thenReturn(clientInterceptor1);
        lenient().when(referenceResolver.resolve("clientInterceptor2", ClientInterceptor.class)).thenReturn(clientInterceptor2);
        lenient().when(referenceResolver.resolve(new String[] { "clientInterceptor1", "clientInterceptor2" }, ClientInterceptor.class)).thenReturn(Arrays.asList(clientInterceptor1, clientInterceptor2));

        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testWebServiceClientParser() throws NoSuchFieldException {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message sender
        Assertions.assertEquals("http://localhost:8080/test", client1.getEndpointConfiguration().getDefaultUri());
        Assertions.assertTrue(client1.getEndpointConfiguration().getMessageFactory() instanceof SoapMessageFactory);
        Assertions.assertEquals(DefaultMessageCorrelator.class, client1.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertEquals(1L, getInterceptors(client1).size());
        Assertions.assertEquals(LoggingClientInterceptor.class, getInterceptors(client1).get(0).getClass());
        Assertions.assertTrue(client1.getEndpointConfiguration().getMessageConverter() instanceof SoapMessageConverter);
        Assertions.assertEquals(ErrorHandlingStrategy.THROWS_EXCEPTION, client1.getEndpointConfiguration().getErrorHandlingStrategy());
        Assertions.assertEquals(5000L, client1.getEndpointConfiguration().getTimeout());
        Assertions.assertNotNull(client1.getEndpointConfiguration().getWebServiceTemplate());

        // 2nd message sender
        Assertions.assertEquals("http://localhost:8080/test", client2.getEndpointConfiguration().getDefaultUri());
        Assertions.assertEquals(messageFactory, client2.getEndpointConfiguration().getMessageFactory());
        Assertions.assertEquals(DefaultMessageCorrelator.class, client2.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertEquals(endpointResolver, client2.getEndpointConfiguration().getEndpointResolver());
        Assertions.assertEquals(ErrorHandlingStrategy.THROWS_EXCEPTION, client2.getEndpointConfiguration().getErrorHandlingStrategy());
        Assertions.assertEquals(10000L, client2.getEndpointConfiguration().getTimeout());
        Assertions.assertNotNull(client2.getEndpointConfiguration().getWebServiceTemplate());
        Assertions.assertEquals(messageFactory, client2.getEndpointConfiguration().getWebServiceTemplate().getMessageFactory());

        // 3rd message sender
        Assertions.assertEquals("http://localhost:8080/test", client3.getEndpointConfiguration().getDefaultUri());
        Assertions.assertNotNull(client3.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(messageCorrelator, client3.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(ErrorHandlingStrategy.THROWS_EXCEPTION, client3.getEndpointConfiguration().getErrorHandlingStrategy());
        Assertions.assertNotNull(client3.getEndpointConfiguration().getWebServiceTemplate());
        Assertions.assertEquals(wsTemplate, client3.getEndpointConfiguration().getWebServiceTemplate());

        // 4th message sender
        Assertions.assertEquals("http://localhost:8080/test", client4.getEndpointConfiguration().getDefaultUri());
        Assertions.assertTrue(client4.getEndpointConfiguration().getMessageFactory() instanceof SoapMessageFactory);
        Assertions.assertEquals(ErrorHandlingStrategy.THROWS_EXCEPTION, client4.getEndpointConfiguration().getErrorHandlingStrategy());
        Assertions.assertNotNull(client4.getEndpointConfiguration().getMessageSender());
        Assertions.assertEquals(messageSender, client4.getEndpointConfiguration().getMessageSender());
        Assertions.assertEquals(1L, getInterceptors(client4).size());
        Assertions.assertEquals(clientInterceptor1, getInterceptors(client4).get(0));
        Assertions.assertNotNull(client4.getEndpointConfiguration().getWebServiceTemplate());
        Assertions.assertEquals(1L, client4.getEndpointConfiguration().getWebServiceTemplate().getInterceptors().length);
        Assertions.assertTrue(client4.getEndpointConfiguration().getMessageConverter() instanceof WsAddressingMessageConverter);

        // 5th message sender
        Assertions.assertEquals("http://localhost:8080/test", client5.getEndpointConfiguration().getDefaultUri());
        Assertions.assertEquals(ErrorHandlingStrategy.PROPAGATE, client5.getEndpointConfiguration().getErrorHandlingStrategy());
        Assertions.assertNotNull(getInterceptors(client5));
        Assertions.assertEquals(2L, getInterceptors(client5).size());
        Assertions.assertEquals(clientInterceptor1, getInterceptors(client5).get(0));
        Assertions.assertEquals(clientInterceptor2, getInterceptors(client5).get(1));
        Assertions.assertEquals(250L, client5.getEndpointConfiguration().getPollingInterval());
        Assertions.assertNotNull(client5.getEndpointConfiguration().getWebServiceTemplate());
        Assertions.assertEquals(2L, client5.getEndpointConfiguration().getWebServiceTemplate().getInterceptors().length);

        // 5th message sender
        Assertions.assertNotNull(client6.getActor());
        Assertions.assertEquals(testActor, client6.getActor());
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser<?, ?>> validators = AnnotationConfigParser.lookup();
        Assertions.assertEquals(8L, validators.size());
        Assertions.assertNotNull(validators.get("direct.async"));
        Assertions.assertEquals(DirectEndpointConfigParser.class, validators.get("direct.async").getClass());
        Assertions.assertNotNull(validators.get("direct.sync"));
        Assertions.assertEquals(DirectSyncEndpointConfigParser.class, validators.get("direct.sync").getClass());
        Assertions.assertNotNull(validators.get("jms.async"));
        Assertions.assertEquals(JmsEndpointConfigParser.class, validators.get("jms.async").getClass());
        Assertions.assertNotNull(validators.get("jms.sync"));
        Assertions.assertEquals(JmsSyncEndpointConfigParser.class, validators.get("jms.sync").getClass());
        Assertions.assertNotNull(validators.get("http.client"));
        Assertions.assertEquals(HttpClientConfigParser.class, validators.get("http.client").getClass());
        Assertions.assertNotNull(validators.get("http.server"));
        Assertions.assertEquals(HttpServerConfigParser.class, validators.get("http.server").getClass());
        Assertions.assertNotNull(validators.get("soap.client"));
        Assertions.assertEquals(WebServiceClientConfigParser.class, validators.get("soap.client").getClass());
        Assertions.assertNotNull(validators.get("soap.server"));
        Assertions.assertEquals(WebServiceServerConfigParser.class, validators.get("soap.server").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("soap.client").isPresent());
    }
}
