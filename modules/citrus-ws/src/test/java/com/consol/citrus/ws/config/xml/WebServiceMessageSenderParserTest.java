/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.ws.config.xml;

import com.consol.citrus.TestActor;
import com.consol.citrus.message.ErrorHandlingStrategy;
import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;
import com.consol.citrus.ws.message.WebServiceMessageSender;
import org.springframework.ws.soap.SoapMessageFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public class WebServiceMessageSenderParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testFailActionParser() {
        Map<String, WebServiceMessageSender> messageSenders = beanDefinitionContext.getBeansOfType(WebServiceMessageSender.class);
        
        Assert.assertEquals(messageSenders.size(), 6);
        
        // 1st message sender
        WebServiceMessageSender messageSender = messageSenders.get("soapMessageSender1");
        Assert.assertNotNull(messageSender.getWebServiceClient().getEndpointConfiguration().getWebServiceTemplate());
        Assert.assertEquals(messageSender.getWebServiceClient().getEndpointConfiguration().getDefaultUri(), "http://localhost:8080/test");
        Assert.assertTrue(messageSender.getWebServiceClient().getEndpointConfiguration().getWebServiceTemplate().getMessageFactory() instanceof SoapMessageFactory);
        Assert.assertNull(messageSender.getCorrelator());
        Assert.assertNull(messageSender.getAddressingHeaders());
        Assert.assertEquals(messageSender.getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);
        
        // 2nd message sender
        messageSender = messageSenders.get("soapMessageSender2");
        Assert.assertNotNull(messageSender.getWebServiceClient().getEndpointConfiguration().getWebServiceTemplate());
        Assert.assertEquals(messageSender.getWebServiceClient().getEndpointConfiguration().getDefaultUri(), "http://localhost:8080/test");
        Assert.assertEquals(messageSender.getWebServiceClient().getEndpointConfiguration().getWebServiceTemplate().getMessageFactory(), beanDefinitionContext.getBean("soapMessageFactory"));
        Assert.assertNull(messageSender.getCorrelator());
        Assert.assertNull(messageSender.getAddressingHeaders());
        Assert.assertEquals(messageSender.getEndpointResolver(), beanDefinitionContext.getBean("endpointResolver"));
        Assert.assertEquals(messageSender.getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);
        
        // 3rd message sender
        messageSender = messageSenders.get("soapMessageSender3");
        Assert.assertNotNull(messageSender.getWebServiceClient().getEndpointConfiguration().getWebServiceTemplate());
        Assert.assertEquals(messageSender.getWebServiceClient().getEndpointConfiguration().getWebServiceTemplate(), beanDefinitionContext.getBean("wsTemplate"));
        Assert.assertEquals(messageSender.getWebServiceClient().getEndpointConfiguration().getDefaultUri(), "http://localhost:8080/test");
        Assert.assertNotNull(messageSender.getCorrelator());
        Assert.assertEquals(messageSender.getCorrelator(), beanDefinitionContext.getBean("replyMessageCorrelator"));
        Assert.assertNull(messageSender.getAddressingHeaders());
        Assert.assertEquals(messageSender.getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);
        
        // 4th message sender
        messageSender = messageSenders.get("soapMessageSender4");
        Assert.assertNotNull(messageSender.getWebServiceClient().getEndpointConfiguration().getWebServiceTemplate());
        Assert.assertEquals(messageSender.getWebServiceClient().getEndpointConfiguration().getDefaultUri(), "http://localhost:8080/test");
        Assert.assertTrue(messageSender.getWebServiceClient().getEndpointConfiguration().getWebServiceTemplate().getMessageFactory() instanceof SoapMessageFactory);
        Assert.assertEquals(messageSender.getAddressingHeaders(), beanDefinitionContext.getBean("wsAddressingHeaders"));
        Assert.assertEquals(messageSender.getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);
        
        // 5th message sender
        messageSender = messageSenders.get("soapMessageSender5");
        Assert.assertNotNull(messageSender.getWebServiceClient().getEndpointConfiguration().getWebServiceTemplate());
        Assert.assertEquals(messageSender.getWebServiceClient().getEndpointConfiguration().getDefaultUri(), "http://localhost:8080/test");
        Assert.assertEquals(messageSender.getErrorHandlingStrategy(), ErrorHandlingStrategy.PROPAGATE);
        
        // 5th message sender
        messageSender = messageSenders.get("soapMessageSender6");
        Assert.assertNotNull(messageSender.getActor());
        Assert.assertEquals(messageSender.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
    }
}
