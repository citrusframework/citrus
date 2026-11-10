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

package org.citrusframework.vertx.endpoint;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.endpoint.context.MessageStoreEndpointComponent;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.vertx.factory.VertxInstanceFactory;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @since 1.4.1
 */
public class VertxEndpointComponentTest {

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private VertxInstanceFactory instanceFactory = Mockito.mock(VertxInstanceFactory.class);
    private TestContext context = new TestContext();

    @BeforeEach
    public void setup() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testCreateEndpoint() {
        VertxEndpointComponent component = new VertxEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("vertxInstanceFactory")).thenReturn(true);
        when(referenceResolver.resolve("vertxInstanceFactory", VertxInstanceFactory.class)).thenReturn(instanceFactory);
        Endpoint endpoint = component.createEndpoint("vertx:news", context);

        Assertions.assertEquals(VertxEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("news", ((VertxEndpoint)endpoint).getEndpointConfiguration().getAddress());
        Assertions.assertFalse(((VertxEndpoint) endpoint).getEndpointConfiguration().isPubSubDomain());
        Assertions.assertEquals(instanceFactory, ((VertxEndpoint) endpoint).getVertxInstanceFactory());
        Assertions.assertEquals(5000L, ((VertxEndpoint) endpoint).getEndpointConfiguration().getTimeout());

        endpoint = component.createEndpoint("eventbus:news-feed", context);

        Assertions.assertEquals(VertxEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("news-feed", ((VertxEndpoint)endpoint).getEndpointConfiguration().getAddress());
        Assertions.assertFalse(((VertxEndpoint) endpoint).getEndpointConfiguration().isPubSubDomain());
        Assertions.assertEquals(instanceFactory, ((VertxEndpoint) endpoint).getVertxInstanceFactory());
        Assertions.assertEquals(5000L, ((VertxEndpoint) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testCreateEndpointWithParameters() {
        VertxEndpointComponent component = new VertxEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("vertxInstanceFactory")).thenReturn(true);
        when(referenceResolver.resolve("vertxInstanceFactory", VertxInstanceFactory.class)).thenReturn(instanceFactory);
        Endpoint endpoint = component.createEndpoint("vertx:news-feed?port=10105&timeout=10000&pubSubDomain=true", context);

        Assertions.assertEquals(VertxEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("news-feed", ((VertxEndpoint)endpoint).getEndpointConfiguration().getAddress());
        Assertions.assertEquals(10105, ((VertxEndpoint)endpoint).getEndpointConfiguration().getPort());
        Assertions.assertTrue(((VertxEndpoint) endpoint).getEndpointConfiguration().isPubSubDomain());
        Assertions.assertEquals(instanceFactory, ((VertxEndpoint) endpoint).getVertxInstanceFactory());
        Assertions.assertEquals(10000L, ((VertxEndpoint) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testCreateEndpointCustomInstanceFactory() {
        VertxEndpointComponent component = new VertxEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.resolve("vertxFactory", VertxInstanceFactory.class)).thenReturn(instanceFactory);
        Endpoint endpoint = component.createEndpoint("vertx:news?vertxInstanceFactory=vertxFactory", context);

        Assertions.assertEquals(VertxEndpoint.class, endpoint.getClass());

        Assertions.assertEquals("news", ((VertxEndpoint)endpoint).getEndpointConfiguration().getAddress());
        Assertions.assertFalse(((VertxEndpoint) endpoint).getEndpointConfiguration().isPubSubDomain());
        Assertions.assertEquals(instanceFactory, ((VertxEndpoint) endpoint).getVertxInstanceFactory());
        Assertions.assertEquals(5000L, ((VertxEndpoint) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testInvalidEndpointUri() {
        VertxEndpointComponent component = new VertxEndpointComponent();

        reset(referenceResolver);
        CitrusRuntimeException exception = Assertions.assertThrows(CitrusRuntimeException.class,
                () -> component.createEndpoint("vertx:news?param1=&param2=value2", context));
        Assertions.assertTrue(exception.getMessage().startsWith("Unable to find parameter"), exception.getMessage());
    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assertions.assertEquals(4L, validators.size());
        Assertions.assertNotNull(validators.get("direct"));
        Assertions.assertEquals(DirectEndpointComponent.class, validators.get("direct").getClass());
        Assertions.assertNotNull(validators.get("message-store"));
        Assertions.assertEquals(MessageStoreEndpointComponent.class, validators.get("message-store").getClass());
        Assertions.assertNotNull(validators.get("vertx"));
        Assertions.assertEquals(VertxEndpointComponent.class, validators.get("vertx").getClass());
        Assertions.assertNotNull(validators.get("eventbus"));
        Assertions.assertEquals(VertxEndpointComponent.class, validators.get("eventbus").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(EndpointComponent.lookup("vertx").isPresent());
        Assertions.assertTrue(EndpointComponent.lookup("eventbus").isPresent());
    }
}
