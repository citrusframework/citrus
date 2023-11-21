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

package org.citrusframework.jms.config.annotation;

import static org.testng.Assert.fail;

import jakarta.jms.JMSException;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jms.endpoint.JmsEndpoint;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.util.ReflectionHelper;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

public class JmsEndpointConfigParserExceptionTest extends AbstractTestNGUnitTest {

    @Mock
    private ReferenceResolver referenceResolver;

    @BeforeClass
    public void setup() throws JMSException {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @DataProvider(name = "config-exceptions")
    public Object[][] configExceptionsData() {
        return new Object[][]{
            {"missingAttributes", "Either a jms-template reference or one of destination or destination-name must be provided"},
            {"jmsTemplateWithConnectionFactory", "When providing a jms-template, none of connection-factory, destination, or destination-name should be provided"},
            {"jmsTemplateWithDestination", "When providing a jms-template, none of connection-factory, destination, or destination-name should be provided"},
            {"jmsTemplateWithDestinationName", "When providing a jms-template, none of connection-factory, destination, or destination-name should be provided"},
            {"autoStartWithoutPubSubDomain", "When providing auto start enabled, pubSubDomain should also be enabled"},
            {"durableSubscriptionWithoutPubSubDomain", "When providing durable subscription enabled, pubSubDomain should also be enabled"},
        };
    }

    @Test(dataProvider = "config-exceptions")
    public void testJmsEndpointParser(String fieldName, String exceptionMessage) {
        JmsEndpointConfig annotation = getJmsEndpointConfigAnnotationFromField(fieldName);

        assertException(() -> new JmsEndpointConfigParser().parse(annotation, referenceResolver), exceptionMessage);
    }

    private static JmsEndpointConfig getJmsEndpointConfigAnnotationFromField(String fieldName) {
        Field jmsEndpointField = ReflectionHelper.findField(JmsEndpointConfigs.class, fieldName);
        if (jmsEndpointField == null) {
            fail("Incorrect field name [" + fieldName + "] provided for @JmsEndpointConfig test data.");
        }

        return jmsEndpointField.getAnnotation(JmsEndpointConfig.class);
    }

    private static void assertException(Runnable runnable, String exceptionMessage) {
        try {
            runnable.run();
            fail("Expected exception not thrown!");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains(exceptionMessage));
        }
    }

    private static final class JmsEndpointConfigs {
        //"Either a jms-template reference or one of destination or destination-name must be provided"

        @JmsEndpointConfig
        private JmsEndpoint missingAttributes;

        //"When providing a jms-template, none of connection-factory, destination, or destination-name should be provided"

        @JmsEndpointConfig(jmsTemplate = "template", connectionFactory = "factory")
        private JmsEndpoint jmsTemplateWithConnectionFactory;

        @JmsEndpointConfig(jmsTemplate = "template", destination = "destination")
        private JmsEndpoint jmsTemplateWithDestination;

        @JmsEndpointConfig(jmsTemplate = "template", destinationName = "destination-name")
        private JmsEndpoint jmsTemplateWithDestinationName;

        //"When providing auto start enabled, pubSubDomain should also be enabled"

        @JmsEndpointConfig(jmsTemplate = "template", autoStart = true)
        private JmsEndpoint autoStartWithoutPubSubDomain;

        //"When providing durable subscription enabled, pubSubDomain should also be enabled"

        @JmsEndpointConfig(destinationName = "destination-name", durableSubscription = true)
        private JmsEndpoint durableSubscriptionWithoutPubSubDomain;
    }
}
