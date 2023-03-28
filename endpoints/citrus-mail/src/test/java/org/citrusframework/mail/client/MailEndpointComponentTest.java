/*
 * Copyright 2006-2014 the original author or authors.
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
import org.citrusframework.mail.model.MailMarshaller;
import org.citrusframework.spi.ReferenceResolver;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


/**
 * @author Christoph Deppisch
 */
public class MailEndpointComponentTest {

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private MailMarshaller marshaller = Mockito.mock(MailMarshaller.class);
    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testCreateClientEndpoint() throws Exception {
        MailEndpointComponent component = new MailEndpointComponent();

        Endpoint endpoint = component.createEndpoint("smtp://localhost:22000", context);

        Assert.assertEquals(endpoint.getClass(), MailClient.class);

        Assert.assertEquals(((MailClient)endpoint).getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(((MailClient) endpoint).getEndpointConfiguration().getPort(), 22000);
        Assert.assertEquals(((MailClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        endpoint = component.createEndpoint("mail:localhost:25000", context);

        Assert.assertEquals(endpoint.getClass(), MailClient.class);

        Assert.assertEquals(((MailClient)endpoint).getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(((MailClient) endpoint).getEndpointConfiguration().getPort(), 25000);
        Assert.assertEquals(((MailClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        endpoint = component.createEndpoint("mail:localhost", context);

        Assert.assertEquals(endpoint.getClass(), MailClient.class);

        Assert.assertEquals(((MailClient)endpoint).getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(((MailClient) endpoint).getEndpointConfiguration().getPort(), -1);
        Assert.assertEquals(((MailClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testCreateClientEndpointWithParameters() throws Exception {
        MailEndpointComponent component = new MailEndpointComponent();

        reset(referenceResolver);
        when(referenceResolver.isResolvable("myMarshaller")).thenReturn(true);
        when(referenceResolver.resolve("myMarshaller", MailMarshaller.class)).thenReturn(marshaller);
        Endpoint endpoint = component.createEndpoint("smtp://localhost?timeout=10000&username=foo&password=1234&marshaller=myMarshaller", context);

        Assert.assertEquals(endpoint.getClass(), MailClient.class);

        Assert.assertEquals(((MailClient)endpoint).getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(((MailClient) endpoint).getEndpointConfiguration().getPort(), -1);
        Assert.assertEquals(((MailClient) endpoint).getEndpointConfiguration().getUsername(), "foo");
        Assert.assertEquals(((MailClient) endpoint).getEndpointConfiguration().getPassword(), "1234");
        Assert.assertEquals(((MailClient) endpoint).getEndpointConfiguration().getMarshaller(), marshaller);
        Assert.assertEquals(((MailClient) endpoint).getEndpointConfiguration().getTimeout(), 10000L);

    }

    @Test
    public void testLookupAll() {
        Map<String, EndpointComponent> validators = EndpointComponent.lookup();
        Assert.assertEquals(validators.size(), 3L);
        Assert.assertNotNull(validators.get("direct"));
        Assert.assertEquals(validators.get("direct").getClass(), DirectEndpointComponent.class);
        Assert.assertNotNull(validators.get("mail"));
        Assert.assertEquals(validators.get("mail").getClass(), MailEndpointComponent.class);
        Assert.assertNotNull(validators.get("smtp"));
        Assert.assertEquals(validators.get("smtp").getClass(), MailEndpointComponent.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(EndpointComponent.lookup("mail").isPresent());
        Assert.assertTrue(EndpointComponent.lookup("smtp").isPresent());
    }
}
