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

import java.util.Map;

import org.apache.camel.CamelContext;
import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.endpoint.CamelEndpoint;
import org.citrusframework.camel.message.CamelMessageConverter;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
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
public class CamelEndpointConfigParserTest extends AbstractUnitTest {

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

    @BeforeEach
    @Override
    public void prepareTest() {
        super.prepareTest();

        lenient().when(referenceResolver.isResolvable(CamelSettings.getContextName())).thenReturn(true);
        lenient().when(referenceResolver.resolve(CamelSettings.getContextName(), CamelContext.class)).thenReturn(camelContext);
        lenient().when(referenceResolver.resolve("messageConverter", CamelMessageConverter.class)).thenReturn(messageConverter);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);

        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testCamelEndpointParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st message receiver
        assertNotNull(camelEndpoint1.getEndpointConfiguration().getCamelContext());
        assertEquals(camelContext, camelEndpoint1.getEndpointConfiguration().getCamelContext());
        assertEquals(CamelMessageConverter.class, camelEndpoint1.getEndpointConfiguration().getMessageConverter().getClass());
        assertEquals("direct:foo", camelEndpoint1.getEndpointConfiguration().getEndpointUri());
        assertEquals(5000L, camelEndpoint1.getEndpointConfiguration().getTimeout());

        // 2nd message receiver
        assertNotNull(camelEndpoint2.getEndpointConfiguration().getCamelContext());
        assertEquals(camelContext, camelEndpoint2.getEndpointConfiguration().getCamelContext());
        assertEquals("direct:bar", camelEndpoint2.getEndpointConfiguration().getEndpointUri());
        assertEquals(messageConverter, camelEndpoint2.getEndpointConfiguration().getMessageConverter());
        assertEquals(10000L, camelEndpoint2.getEndpointConfiguration().getTimeout());

        // 3rd message receiver
        assertNotNull(camelEndpoint3.getActor());
        assertEquals(testActor, camelEndpoint3.getActor());
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser<?, ?>> validators = AnnotationConfigParser.lookup();
        assertEquals(6L, validators.size());
        assertNotNull(validators.get("direct.async"));
        assertEquals(DirectEndpointConfigParser.class, validators.get("direct.async").getClass());
        assertNotNull(validators.get("direct.sync"));
        assertEquals(DirectSyncEndpointConfigParser.class, validators.get("direct.sync").getClass());
        assertNotNull(validators.get("camel.async"));
        assertEquals(CamelEndpointConfigParser.class, validators.get("camel.async").getClass());
        assertNotNull(validators.get("camel.sync"));
        assertEquals(CamelSyncEndpointConfigParser.class, validators.get("camel.sync").getClass());
        assertNotNull(validators.get("camel.inOnly"));
        assertEquals(CamelEndpointConfigParser.class, validators.get("camel.inOnly").getClass());
        assertNotNull(validators.get("camel.inOut"));
        assertEquals(CamelSyncEndpointConfigParser.class, validators.get("camel.inOut").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        assertTrue(AnnotationConfigParser.lookup("camel.async").isPresent());
    }
}
