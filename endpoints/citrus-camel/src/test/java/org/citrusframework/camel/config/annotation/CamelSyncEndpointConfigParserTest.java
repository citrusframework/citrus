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

package org.citrusframework.camel.config.annotation;

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.camel.endpoint.CamelSyncEndpoint;
import org.citrusframework.camel.message.CamelMessageConverter;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.apache.camel.CamelContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class CamelSyncEndpointConfigParserTest extends AbstractTestNGUnitTest {

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

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.isResolvable("camelContext")).thenReturn(true);
        when(referenceResolver.resolve("camelContext", CamelContext.class)).thenReturn(camelContext);
        when(referenceResolver.resolve("messageConverter", CamelMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("messageCorrelator", MessageCorrelator.class)).thenReturn(messageCorrelator);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testCamelSyncEndpointAsConsumerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message receiver
        Assert.assertNotNull(camelSyncEndpoint1.getEndpointConfiguration().getCamelContext());
        Assert.assertEquals(camelSyncEndpoint1.getEndpointConfiguration().getCamelContext(), camelContext);
        Assert.assertEquals(camelSyncEndpoint1.getEndpointConfiguration().getEndpointUri(), "direct:foo");
        Assert.assertEquals(camelSyncEndpoint1.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertEquals(camelSyncEndpoint1.getEndpointConfiguration().getPollingInterval(), 500L);
        Assert.assertEquals(camelSyncEndpoint1.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);

        // 2nd message receiver
        Assert.assertNotNull(camelSyncEndpoint2.getEndpointConfiguration().getCamelContext());
        Assert.assertEquals(camelSyncEndpoint2.getEndpointConfiguration().getCamelContext(), camelContext);
        Assert.assertEquals(camelSyncEndpoint2.getEndpointConfiguration().getEndpointUri(), "direct:bar");
        Assert.assertEquals(camelSyncEndpoint2.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertEquals(camelSyncEndpoint2.getEndpointConfiguration().getPollingInterval(), 500L);
        Assert.assertEquals(camelSyncEndpoint2.getEndpointConfiguration().getMessageConverter(), messageConverter);
        Assert.assertEquals(camelSyncEndpoint2.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);

        // 3rd message receiver
        Assert.assertNotNull(camelSyncEndpoint3.getEndpointConfiguration().getCamelContext());
        Assert.assertEquals(camelSyncEndpoint3.getEndpointConfiguration().getCamelContext(), camelContext);
        Assert.assertEquals(camelSyncEndpoint3.getEndpointConfiguration().getEndpointUri(), "direct:foo");
        Assert.assertEquals(camelSyncEndpoint3.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertEquals(camelSyncEndpoint3.getEndpointConfiguration().getPollingInterval(), 250L);
        Assert.assertEquals(camelSyncEndpoint3.getEndpointConfiguration().getCorrelator(), messageCorrelator);
        Assert.assertNotNull(camelSyncEndpoint3.getActor());
        Assert.assertEquals(camelSyncEndpoint3.getActor(), testActor);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("camel.sync").isPresent());
    }
}
