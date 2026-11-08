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

package org.citrusframework.camel.config.annotation;

import org.apache.camel.CamelContext;
import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.endpoint.CamelSyncEndpoint;
import org.citrusframework.camel.message.CamelMessageConverter;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.spi.ReferenceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CamelSyncEndpointConfigParserTest extends AbstractUnitTest {

    @CitrusEndpoint
    @CamelSyncEndpointConfig(endpointUri="direct:foo")
    private CamelSyncEndpoint camelSyncEndpoint1;

    @CitrusEndpoint
    @CamelSyncEndpointConfig(endpointUri="direct:bar",
            timeout=10000L,
            messageConverter="messageConverter",
            camelContext="camelContext")
    private CamelSyncEndpoint camelSyncEndpoint2;

    @CitrusEndpoint
    @CamelSyncEndpointConfig(endpointUri="direct:foo",
            pollingInterval=250,
            actor="testActor",
            correlator="messageCorrelator")
    private CamelSyncEndpoint camelSyncEndpoint3;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private CamelContext camelContext;
    @Mock
    private CamelMessageConverter messageConverter;
    @Mock
    private MessageCorrelator messageCorrelator;
    @Mock
    private TestActor testActor;

    @BeforeEach
    @Override
    public void prepareTest() {
        super.prepareTest();

        lenient().when(referenceResolver.isResolvable(CamelSettings.getContextName())).thenReturn(true);
        lenient().when(referenceResolver.resolve(CamelSettings.getContextName(), CamelContext.class)).thenReturn(camelContext);
        lenient().when(referenceResolver.resolve("messageConverter", CamelMessageConverter.class)).thenReturn(messageConverter);
        lenient().when(referenceResolver.resolve("messageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);

        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testCamelSyncEndpointAsConsumerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message receiver
        assertNotNull(camelSyncEndpoint1.getEndpointConfiguration().getCamelContext());
        assertEquals(camelContext, camelSyncEndpoint1.getEndpointConfiguration().getCamelContext());
        assertEquals("direct:foo", camelSyncEndpoint1.getEndpointConfiguration().getEndpointUri());
        assertEquals(5000L, camelSyncEndpoint1.getEndpointConfiguration().getTimeout());
        assertEquals(500L, camelSyncEndpoint1.getEndpointConfiguration().getPollingInterval());
        assertEquals(DefaultMessageCorrelator.class, camelSyncEndpoint1.getEndpointConfiguration().getCorrelator().getClass());

        // 2nd message receiver
        assertNotNull(camelSyncEndpoint2.getEndpointConfiguration().getCamelContext());
        assertEquals(camelContext, camelSyncEndpoint2.getEndpointConfiguration().getCamelContext());
        assertEquals("direct:bar", camelSyncEndpoint2.getEndpointConfiguration().getEndpointUri());
        assertEquals(10000L, camelSyncEndpoint2.getEndpointConfiguration().getTimeout());
        assertEquals(500L, camelSyncEndpoint2.getEndpointConfiguration().getPollingInterval());
        assertEquals(messageConverter, camelSyncEndpoint2.getEndpointConfiguration().getMessageConverter());
        assertEquals(DefaultMessageCorrelator.class, camelSyncEndpoint2.getEndpointConfiguration().getCorrelator().getClass());

        // 3rd message receiver
        assertNotNull(camelSyncEndpoint3.getEndpointConfiguration().getCamelContext());
        assertEquals(camelContext, camelSyncEndpoint3.getEndpointConfiguration().getCamelContext());
        assertEquals("direct:foo", camelSyncEndpoint3.getEndpointConfiguration().getEndpointUri());
        assertEquals(5000L, camelSyncEndpoint3.getEndpointConfiguration().getTimeout());
        assertEquals(250L, camelSyncEndpoint3.getEndpointConfiguration().getPollingInterval());
        assertEquals(messageCorrelator, camelSyncEndpoint3.getEndpointConfiguration().getCorrelator());
        assertNotNull(camelSyncEndpoint3.getActor());
        assertEquals(testActor, camelSyncEndpoint3.getActor());
    }

    @Test
    public void testLookupByQualifier() {
        assertTrue(AnnotationConfigParser.lookup("camel.sync").isPresent());
    }
}
