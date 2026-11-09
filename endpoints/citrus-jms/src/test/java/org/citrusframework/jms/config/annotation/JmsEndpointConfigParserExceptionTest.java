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

package org.citrusframework.jms.config.annotation;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jms.endpoint.JmsEndpoint;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.util.ReflectionHelper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(MockitoExtension.class)
public class JmsEndpointConfigParserExceptionTest extends AbstractUnitTest {

    @Mock
    private ReferenceResolver referenceResolver;

    @BeforeEach
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    static Stream<Arguments> configExceptions() {
        return Stream.of(
            Arguments.of("missingAttributes", "Either a jms-template reference or one of destination or destination-name must be provided"),
            Arguments.of("jmsTemplateWithConnectionFactory", "When providing a jms-template, none of connection-factory, destination, or destination-name should be provided"),
            Arguments.of("jmsTemplateWithDestination", "When providing a jms-template, none of connection-factory, destination, or destination-name should be provided"),
            Arguments.of("jmsTemplateWithDestinationName", "When providing a jms-template, none of connection-factory, destination, or destination-name should be provided"),
            Arguments.of("autoStartWithoutPubSubDomain", "When providing auto start enabled, pubSubDomain should also be enabled"),
            Arguments.of("durableSubscriptionWithoutPubSubDomain", "When providing durable subscription enabled, pubSubDomain should also be enabled")
        );
    }

    @ParameterizedTest
    @MethodSource("configExceptions")
    public void testJmsEndpointParser(String fieldName, String exceptionMessage) {
        JmsEndpointConfig annotation = getJmsEndpointConfigAnnotationFromField(fieldName);

        assertException(() -> new JmsEndpointConfigParser().parse(annotation, referenceResolver, context), exceptionMessage);
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
            Assertions.assertTrue(e.getMessage().contains(exceptionMessage));
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
