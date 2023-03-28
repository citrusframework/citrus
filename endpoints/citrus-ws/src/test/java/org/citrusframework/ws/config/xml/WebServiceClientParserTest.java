/*
 * Copyright 2006-2014 the original author or authors.
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

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.interceptor.LoggingClientInterceptor;
import org.citrusframework.ws.message.converter.SoapMessageConverter;
import org.citrusframework.ws.message.converter.WsAddressingMessageConverter;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.ws.soap.SoapMessageFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class WebServiceClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testWebServiceClientParser() {
        Map<String, WebServiceClient> messageSenders = beanDefinitionContext.getBeansOfType(WebServiceClient.class);

        Assert.assertEquals(messageSenders.size(), 6);

        // 1st message sender
        WebServiceClient client = messageSenders.get("soapClient1");
        Assert.assertEquals(client.getEndpointConfiguration().getDefaultUri(), "http://localhost:8080/test");
        Assert.assertTrue(client.getEndpointConfiguration().getMessageFactory() instanceof SoapMessageFactory);
        Assert.assertEquals(client.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(client.getEndpointConfiguration().getInterceptors().size(), 1L);
        Assert.assertEquals(client.getEndpointConfiguration().getInterceptors().get(0).getClass(), LoggingClientInterceptor.class);
        Assert.assertTrue(client.getEndpointConfiguration().getMessageConverter() instanceof SoapMessageConverter);
        Assert.assertEquals(client.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);
        Assert.assertEquals(client.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertNotNull(client.getEndpointConfiguration().getWebServiceTemplate());

        // 2nd message sender
        client = messageSenders.get("soapClient2");
        Assert.assertEquals(client.getEndpointConfiguration().getDefaultUri(), "http://localhost:8080/test");
        Assert.assertEquals(client.getEndpointConfiguration().getMessageFactory(), beanDefinitionContext.getBean("soapMessageFactory"));
        Assert.assertEquals(client.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(client.getEndpointConfiguration().getEndpointResolver(), beanDefinitionContext.getBean("endpointResolver"));
        Assert.assertEquals(client.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);
        Assert.assertEquals(client.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertNotNull(client.getEndpointConfiguration().getWebServiceTemplate());
        Assert.assertEquals(client.getEndpointConfiguration().getWebServiceTemplate().getMessageFactory(), beanDefinitionContext.getBean("soapMessageFactory"));

        // 3rd message sender
        client = messageSenders.get("soapClient3");
        Assert.assertEquals(client.getEndpointConfiguration().getDefaultUri(), "http://localhost:8080/test");
        Assert.assertNotNull(client.getEndpointConfiguration().getCorrelator());
        Assert.assertEquals(client.getEndpointConfiguration().getCorrelator(), beanDefinitionContext.getBean("replyMessageCorrelator"));
        Assert.assertEquals(client.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);
        Assert.assertNotNull(client.getEndpointConfiguration().getWebServiceTemplate());
        Assert.assertEquals(client.getEndpointConfiguration().getWebServiceTemplate(), beanDefinitionContext.getBean("wsTemplate"));

        // 4th message sender
        client = messageSenders.get("soapClient4");
        Assert.assertEquals(client.getEndpointConfiguration().getDefaultUri(), "http://localhost:8080/test");
        Assert.assertTrue(client.getEndpointConfiguration().getMessageFactory() instanceof SoapMessageFactory);
        Assert.assertEquals(client.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);
        Assert.assertNotNull(client.getEndpointConfiguration().getMessageSender());
        Assert.assertEquals(client.getEndpointConfiguration().getMessageSender(), beanDefinitionContext.getBean("wsMessageSender"));
        Assert.assertEquals(client.getEndpointConfiguration().getInterceptors().size(), 1L);
        Assert.assertEquals(client.getEndpointConfiguration().getInterceptors().get(0), beanDefinitionContext.getBean("singleInterceptor"));
        Assert.assertNotNull(client.getEndpointConfiguration().getWebServiceTemplate());
        Assert.assertEquals(client.getEndpointConfiguration().getWebServiceTemplate().getInterceptors().length, 1L);
        Assert.assertTrue(client.getEndpointConfiguration().getMessageConverter() instanceof WsAddressingMessageConverter);

        // 5th message sender
        client = messageSenders.get("soapClient5");
        Assert.assertEquals(client.getEndpointConfiguration().getDefaultUri(), "http://localhost:8080/test");
        Assert.assertEquals(client.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.PROPAGATE);
        Assert.assertNotNull(client.getEndpointConfiguration().getInterceptors());
        Assert.assertEquals(client.getEndpointConfiguration().getInterceptors().size(), 2L);
        Assert.assertEquals(client.getEndpointConfiguration().getInterceptors().get(0), beanDefinitionContext.getBean("interceptor1"));
        Assert.assertEquals(client.getEndpointConfiguration().getInterceptors().get(1), beanDefinitionContext.getBean("interceptor2"));
        Assert.assertEquals(client.getEndpointConfiguration().getPollingInterval(), 250L);
        Assert.assertNotNull(client.getEndpointConfiguration().getWebServiceTemplate());
        Assert.assertEquals(client.getEndpointConfiguration().getWebServiceTemplate().getInterceptors().length, 2L);

        // 5th message sender
        client = messageSenders.get("soapClient6");
        Assert.assertNotNull(client.getActor());
        Assert.assertEquals(client.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
    }

    @Test
    public void testBothWebServiceTemplateAndMessageFactorySet() {
        try {
            createApplicationContext("failed1");
            Assert.fail("Missing bean creation exception due to web service template and message factory property set");
        } catch (BeanDefinitionParsingException e) {
            Assert.assertTrue(e.getMessage().contains("none of 'message-factory', 'message-sender' should be set"), e.getMessage());
        }
    }

    @Test
    public void testBothWebServiceTemplateAndMessageSenderSet() {
        try {
            createApplicationContext("failed2");
            Assert.fail("Missing bean creation exception due to web service template and message sender property set");
        } catch (BeanDefinitionParsingException e) {
            Assert.assertTrue(e.getMessage().contains("none of 'message-factory', 'message-sender' should be set"), e.getMessage());
        }
    }
}
