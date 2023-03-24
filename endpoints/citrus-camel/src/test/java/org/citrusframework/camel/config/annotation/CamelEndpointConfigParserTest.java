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

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.camel.endpoint.CamelEndpoint;
import org.citrusframework.camel.message.CamelMessageConverter;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
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
public class CamelEndpointConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint
    @CamelEndpointConfig(endpointUri="direct:foo")
    private CamelEndpoint camelEndpoint1;

    @CitrusEndpoint
    @CamelEndpointConfig(endpointUri="direct:bar",
            timeout=10000L,
            messageConverter="messageConverter",
            camelContext="camelContext")
    private CamelEndpoint camelEndpoint2;

    @CitrusEndpoint
    @CamelEndpointConfig(endpointUri="direct:foo",
            actor="testActor")
    private CamelEndpoint camelEndpoint3;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private CamelContext camelContext;
    @Mock
    private CamelMessageConverter messageConverter;
    @Mock
    private TestActor testActor;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.isResolvable("camelContext")).thenReturn(true);
        when(referenceResolver.resolve("camelContext", CamelContext.class)).thenReturn(camelContext);
        when(referenceResolver.resolve("messageConverter", CamelMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testCamelEndpointParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message receiver
        Assert.assertNotNull(camelEndpoint1.getEndpointConfiguration().getCamelContext());
        Assert.assertEquals(camelEndpoint1.getEndpointConfiguration().getCamelContext(), camelContext);
        Assert.assertEquals(camelEndpoint1.getEndpointConfiguration().getMessageConverter().getClass(), CamelMessageConverter.class);
        Assert.assertEquals(camelEndpoint1.getEndpointConfiguration().getEndpointUri(), "direct:foo");
        Assert.assertEquals(camelEndpoint1.getEndpointConfiguration().getTimeout(), 5000L);

        // 2nd message receiver
        Assert.assertNotNull(camelEndpoint2.getEndpointConfiguration().getCamelContext());
        Assert.assertEquals(camelEndpoint2.getEndpointConfiguration().getCamelContext(), camelContext);
        Assert.assertEquals(camelEndpoint2.getEndpointConfiguration().getEndpointUri(), "direct:bar");
        Assert.assertEquals(camelEndpoint2.getEndpointConfiguration().getMessageConverter(), messageConverter);
        Assert.assertEquals(camelEndpoint2.getEndpointConfiguration().getTimeout(), 10000L);

        // 3rd message receiver
        Assert.assertNotNull(camelEndpoint3.getActor());
        Assert.assertEquals(camelEndpoint3.getActor(), testActor);
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser> validators = AnnotationConfigParser.lookup();
        Assert.assertEquals(validators.size(), 6L);
        Assert.assertNotNull(validators.get("direct.async"));
        Assert.assertEquals(validators.get("direct.async").getClass(), DirectEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("direct.sync"));
        Assert.assertEquals(validators.get("direct.sync").getClass(), DirectSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("camel.async"));
        Assert.assertEquals(validators.get("camel.async").getClass(), CamelEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("camel.sync"));
        Assert.assertEquals(validators.get("camel.sync").getClass(), CamelSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("camel.inOnly"));
        Assert.assertEquals(validators.get("camel.inOnly").getClass(), CamelEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("camel.inOut"));
        Assert.assertEquals(validators.get("camel.inOut").getClass(), CamelSyncEndpointConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("camel.async").isPresent());
    }
}
