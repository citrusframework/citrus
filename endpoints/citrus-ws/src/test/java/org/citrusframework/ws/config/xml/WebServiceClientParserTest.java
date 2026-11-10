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

package org.citrusframework.ws.config.xml;

import static org.citrusframework.ws.client.WsTestUtils.getInterceptors;

import java.util.Map;
import org.citrusframework.TestActor;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.interceptor.LoggingClientInterceptor;
import org.citrusframework.ws.message.converter.SoapMessageConverter;
import org.citrusframework.ws.message.converter.WsAddressingMessageConverter;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.ws.soap.SoapMessageFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WebServiceClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testWebServiceClientParser() throws NoSuchFieldException {
        Map<String, WebServiceClient> messageSenders = beanDefinitionContext.getBeansOfType(WebServiceClient.class);

        Assertions.assertEquals(6, messageSenders.size());

        // 1st message sender
        WebServiceClient client = messageSenders.get("soapClient1");
        Assertions.assertEquals("http://localhost:8080/test", client.getEndpointConfiguration().getDefaultUri());
        Assertions.assertTrue(client.getEndpointConfiguration().getMessageFactory() instanceof SoapMessageFactory);
        Assertions.assertEquals(DefaultMessageCorrelator.class, client.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertEquals(1L, getInterceptors(client).size());
        Assertions.assertEquals(LoggingClientInterceptor.class, getInterceptors(client).get(0).getClass());
        Assertions.assertTrue(client.getEndpointConfiguration().getMessageConverter() instanceof SoapMessageConverter);
        Assertions.assertEquals(ErrorHandlingStrategy.THROWS_EXCEPTION, client.getEndpointConfiguration().getErrorHandlingStrategy());
        Assertions.assertEquals(5000L, client.getEndpointConfiguration().getTimeout());
        Assertions.assertNotNull(client.getEndpointConfiguration().getWebServiceTemplate());

        // 2nd message sender
        client = messageSenders.get("soapClient2");
        Assertions.assertEquals("http://localhost:8080/test", client.getEndpointConfiguration().getDefaultUri());
        Assertions.assertEquals(client.getEndpointConfiguration().getMessageFactory(), beanDefinitionContext.getBean("soapMessageFactory"));
        Assertions.assertEquals(DefaultMessageCorrelator.class, client.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertEquals(client.getEndpointConfiguration().getEndpointResolver(), beanDefinitionContext.getBean("endpointResolver"));
        Assertions.assertEquals(ErrorHandlingStrategy.THROWS_EXCEPTION, client.getEndpointConfiguration().getErrorHandlingStrategy());
        Assertions.assertEquals(10000L, client.getEndpointConfiguration().getTimeout());
        Assertions.assertNotNull(client.getEndpointConfiguration().getWebServiceTemplate());
        Assertions.assertEquals(client.getEndpointConfiguration().getWebServiceTemplate().getMessageFactory(), beanDefinitionContext.getBean("soapMessageFactory"));

        // 3rd message sender
        client = messageSenders.get("soapClient3");
        Assertions.assertEquals("http://localhost:8080/test", client.getEndpointConfiguration().getDefaultUri());
        Assertions.assertNotNull(client.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(client.getEndpointConfiguration().getCorrelator(), beanDefinitionContext.getBean("replyMessageCorrelator"));
        Assertions.assertEquals(ErrorHandlingStrategy.THROWS_EXCEPTION, client.getEndpointConfiguration().getErrorHandlingStrategy());
        Assertions.assertNotNull(client.getEndpointConfiguration().getWebServiceTemplate());
        Assertions.assertEquals(client.getEndpointConfiguration().getWebServiceTemplate(), beanDefinitionContext.getBean("wsTemplate"));

        // 4th message sender
        client = messageSenders.get("soapClient4");
        Assertions.assertEquals("http://localhost:8080/test", client.getEndpointConfiguration().getDefaultUri());
        Assertions.assertTrue(client.getEndpointConfiguration().getMessageFactory() instanceof SoapMessageFactory);
        Assertions.assertEquals(ErrorHandlingStrategy.THROWS_EXCEPTION, client.getEndpointConfiguration().getErrorHandlingStrategy());
        Assertions.assertNotNull(client.getEndpointConfiguration().getMessageSender());
        Assertions.assertEquals(client.getEndpointConfiguration().getMessageSender(), beanDefinitionContext.getBean("wsMessageSender"));
        Assertions.assertEquals(1L, getInterceptors(client).size());
        Assertions.assertEquals(getInterceptors(client).get(0), beanDefinitionContext.getBean("singleInterceptor"));
        Assertions.assertNotNull(client.getEndpointConfiguration().getWebServiceTemplate());
        Assertions.assertEquals(1L, client.getEndpointConfiguration().getWebServiceTemplate().getInterceptors().length);
        Assertions.assertTrue(client.getEndpointConfiguration().getMessageConverter() instanceof WsAddressingMessageConverter);

        // 5th message sender
        client = messageSenders.get("soapClient5");
        Assertions.assertEquals("http://localhost:8080/test", client.getEndpointConfiguration().getDefaultUri());
        Assertions.assertEquals(ErrorHandlingStrategy.PROPAGATE, client.getEndpointConfiguration().getErrorHandlingStrategy());
        Assertions.assertNotNull(getInterceptors(client));
        Assertions.assertEquals(2L, getInterceptors(client).size());
        Assertions.assertEquals(getInterceptors(client).get(0), beanDefinitionContext.getBean("interceptor1"));
        Assertions.assertEquals(getInterceptors(client).get(1), beanDefinitionContext.getBean("interceptor2"));
        Assertions.assertEquals(250L, client.getEndpointConfiguration().getPollingInterval());
        Assertions.assertNotNull(client.getEndpointConfiguration().getWebServiceTemplate());
        Assertions.assertEquals(2L, client.getEndpointConfiguration().getWebServiceTemplate().getInterceptors().length);

        // 5th message sender
        client = messageSenders.get("soapClient6");
        Assertions.assertNotNull(client.getActor());
        Assertions.assertEquals(client.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
    }

    @Test
    public void testBothWebServiceTemplateAndMessageFactorySet() {
        try {
            createApplicationContext("failed1");
            Assertions.fail("Missing bean creation exception due to web service template and message factory property set");
        } catch (BeanDefinitionParsingException e) {
            Assertions.assertTrue(e.getMessage().contains("none of 'message-factory', 'message-sender' should be set"), e.getMessage());
        }
    }

    @Test
    public void testBothWebServiceTemplateAndMessageSenderSet() {
        try {
            createApplicationContext("failed2");
            Assertions.fail("Missing bean creation exception due to web service template and message sender property set");
        } catch (BeanDefinitionParsingException e) {
            Assertions.assertTrue(e.getMessage().contains("none of 'message-factory', 'message-sender' should be set"), e.getMessage());
        }
    }
}
