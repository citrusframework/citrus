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
public class JmsSyncEndpointConfigParserExceptionTest extends AbstractUnitTest {

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
            Arguments.of("jmsTemplateWithDestinationName", "When providing a jms-template, none of connection-factory, destination, or destination-name should be provided")
        );
    }

    @ParameterizedTest
    @MethodSource("configExceptions")
    public void testJmsSyncEndpointParser(String fieldName, String exceptionMessage) {
        JmsSyncEndpointConfig annotation = getJmsSyncEndpointConfigAnnotationFromField(fieldName);

        assertException(() -> new JmsSyncEndpointConfigParser().parse(annotation, referenceResolver, context), exceptionMessage);
    }

    private static JmsSyncEndpointConfig getJmsSyncEndpointConfigAnnotationFromField(String fieldName) {
        Field jmsEndpointField = ReflectionHelper.findField(JmsSyncEndpointConfigs.class, fieldName);
        if (jmsEndpointField == null) {
            fail("Incorrect field name [" + fieldName + "] provided for @JmsSyncEndpointConfig test data.");
        }

        return jmsEndpointField.getAnnotation(JmsSyncEndpointConfig.class);
    }

    private static void assertException(Runnable runnable, String exceptionMessage) {
        try {
            runnable.run();
            fail("Expected exception not thrown!");
        } catch (CitrusRuntimeException e) {
            Assertions.assertTrue(e.getMessage().contains(exceptionMessage));
        }
    }

    private static final class JmsSyncEndpointConfigs {
        //"Either a jms-template reference or one of destination or destination-name must be provided"

        @JmsSyncEndpointConfig
        private JmsEndpoint missingAttributes;

        //"When providing a jms-template, none of connection-factory, destination, or destination-name should be provided"

        @JmsSyncEndpointConfig(jmsTemplate = "template", connectionFactory = "factory")
        private JmsEndpoint jmsTemplateWithConnectionFactory;

        @JmsSyncEndpointConfig(jmsTemplate = "template", destination = "destination")
        private JmsEndpoint jmsTemplateWithDestination;

        @JmsSyncEndpointConfig(jmsTemplate = "template", destinationName = "destination-name")
        private JmsEndpoint jmsTemplateWithDestinationName;
    }
}
