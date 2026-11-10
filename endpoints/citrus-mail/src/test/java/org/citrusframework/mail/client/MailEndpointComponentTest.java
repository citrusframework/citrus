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

package org.citrusframework.mail.client;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointComponent;
import org.citrusframework.endpoint.direct.DirectEndpointComponent;
import org.citrusframework.endpoint.context.MessageStoreEndpointComponent;
import org.citrusframework.mail.model.MailMarshaller;
import org.citrusframework.spi.ReferenceResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class MailEndpointComponentTest {

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private MailMarshaller marshaller = Mockito.mock(MailMarshaller.class);
    private TestContext context = new TestContext();

    @BeforeEach
    public void setup() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testCreateClientEndpoint() {
        MailEndpointComponent component = new MailEndpointComponent();

        Endpoint endpoint = component.createEndpoint("smtp://localhost:22000", context);

        Assertions.assertEquals(MailClient.class, endpoint.getClass());

        Assertions.assertEquals("localhost", ((MailClient)endpoint).getEndpointConfiguration().getHost());
        Assertions.assertEquals(22000, ((MailClient) endpoint).getEndpointConfiguration().getPort());
        Assertions.assertEquals(5000L, ((MailClient) endpoint).getEndpointConfiguration().getTimeout());

        endpoint = component.createEndpoint("mail:localhost:25000", context);

        Assertions.assertEquals(MailClient.class, endpoint.getClass());

        Assertions.assertEquals("localhost", ((MailClient)endpoint).getEndpointConfiguration().getHost());
        Assertions.assertEquals(25000, ((MailClient) endpoint).getEndpointConfiguration().getPort());
        Assertions.assertEquals(5000L, ((MailClient) endpoint).getEndpointConfiguration().getTimeout());

        endpoint = component.createEndpoint("mail:localhost", context);

        Assertions.assertEquals(MailClient.class, endpoint.getClass());

        Assertions.assertEquals("localhost", ((MailClient)endpoint).getEndpointConfiguration().getHost());
        Assertions.assertEquals(-1, ((MailClient) endpoint).getEndpointConfiguration().getPort());
        Assertions.assertEquals(5000L, ((MailClient) endpoint).getEndpointConfiguration().getTimeout());
    }

    @Test
    public void testCreateClientEndpointWithParameters() {
        MailEndpointComponent component = new MailEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("myMarshaller")).thenReturn(true);
        when(referenceResolver.resolve("myMarshaller", MailMarshaller.class)).thenReturn(marshaller);
        Endpoint endpoint = component.createEndpoint("smtp://localhost?timeout=10000&username=foo&password=1234&marshaller=myMarshaller", context);

        Assertions.assertEquals(MailClient.class, endpoint.getClass());

        Assertions.assertEquals("localhost", ((MailClient)endpoint).getEndpointConfiguration().getHost());
        Assertions.assertEquals(-1, ((MailClient) endpoint).getEndpointConfiguration().getPort());
        Assertions.assertEquals("foo", ((MailClient) endpoint).getEndpointConfiguration().getUsername());
        Assertions.assertEquals("1234", ((MailClient) endpoint).getEndpointConfiguration().getPassword());
        Assertions.assertEquals(marshaller, ((MailClient) endpoint).getEndpointConfiguration().getMarshaller());
        Assertions.assertEquals(10000L, ((MailClient) endpoint).getEndpointConfiguration().getTimeout());

    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assertions.assertEquals(4L, validators.size());
        Assertions.assertNotNull(validators.get("direct"));
        Assertions.assertEquals(DirectEndpointComponent.class, validators.get("direct").getClass());
        Assertions.assertNotNull(validators.get("message-store"));
        Assertions.assertEquals(MessageStoreEndpointComponent.class, validators.get("message-store").getClass());
        Assertions.assertNotNull(validators.get("mail"));
        Assertions.assertEquals(MailEndpointComponent.class, validators.get("mail").getClass());
        Assertions.assertNotNull(validators.get("smtp"));
        Assertions.assertEquals(MailEndpointComponent.class, validators.get("smtp").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(EndpointComponent.lookup("mail").isPresent());
        Assertions.assertTrue(EndpointComponent.lookup("smtp").isPresent());
    }
}
