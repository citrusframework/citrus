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

package com.consol.citrus.mail.client;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.mail.model.MailMarshaller;
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class MailEndpointComponentTest {

    private ApplicationContext applicationContext = EasyMock.createMock(ApplicationContext.class);
    private MailMarshaller marshaller = EasyMock.createMock(MailMarshaller.class);
    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        context.setApplicationContext(applicationContext);
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

        reset(applicationContext);
        expect(applicationContext.containsBean("myMarshaller")).andReturn(true).once();
        expect(applicationContext.getBean("myMarshaller")).andReturn(marshaller).once();
        replay(applicationContext);

        Endpoint endpoint = component.createEndpoint("smtp://localhost?timeout=10000&username=foo&password=1234&mailMarshaller=myMarshaller", context);

        Assert.assertEquals(endpoint.getClass(), MailClient.class);

        Assert.assertEquals(((MailClient)endpoint).getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(((MailClient) endpoint).getEndpointConfiguration().getPort(), -1);
        Assert.assertEquals(((MailClient) endpoint).getEndpointConfiguration().getUsername(), "foo");
        Assert.assertEquals(((MailClient) endpoint).getEndpointConfiguration().getPassword(), "1234");
        Assert.assertEquals(((MailClient) endpoint).getEndpointConfiguration().getMailMarshaller(), marshaller);
        Assert.assertEquals(((MailClient) endpoint).getEndpointConfiguration().getTimeout(), 10000L);

        verify(applicationContext);
    }
}
