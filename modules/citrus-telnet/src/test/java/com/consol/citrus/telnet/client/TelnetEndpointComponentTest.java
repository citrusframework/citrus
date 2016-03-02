/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.telnet.client;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Magnus Karlsson
 * @since 2.6
 */
public class TelnetEndpointComponentTest {

    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        context.setApplicationContext(applicationContext);
    }

    @Test
    public void testCreateEndpoint() throws Exception {
        TelnetEndpointComponent component = new TelnetEndpointComponent();

        //telnet://<user>:<password>@<host>[:<port>/]

        Endpoint endpoint = component.createEndpoint("telnet://user:password@localhost:23", context);

        Assert.assertEquals(endpoint.getClass(), TelnetClient.class);

        Assert.assertEquals(((TelnetClient)endpoint).getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(((TelnetClient)endpoint).getEndpointConfiguration().getPort(), 23);
        Assert.assertEquals(((TelnetClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testCreateEndpointWithoutPort() throws Exception {
    	TelnetEndpointComponent component = new TelnetEndpointComponent();

    	Endpoint endpoint = component.createEndpoint("telnet://user:password@127.0.0.1", context);

        Assert.assertEquals(endpoint.getClass(), TelnetClient.class);

        Assert.assertEquals(((TelnetClient)endpoint).getEndpointConfiguration().getHost(), "127.0.0.1");
        Assert.assertEquals(((TelnetClient)endpoint).getEndpointConfiguration().getPort(), 23);
        Assert.assertEquals(((TelnetClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testCreateEndpointWithParameters() throws Exception {
    	TelnetEndpointComponent component = new TelnetEndpointComponent();

        Endpoint endpoint = component.createEndpoint("telnet://user:password@localhost:23?timeout=10000&user=foo&password=12345678", context);

        Assert.assertEquals(endpoint.getClass(), TelnetClient.class);

        Assert.assertEquals(((TelnetClient)endpoint).getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(((TelnetClient)endpoint).getEndpointConfiguration().getPort(), 23);
        Assert.assertEquals(((TelnetClient)endpoint).getEndpointConfiguration().getUser(), "foo");
        Assert.assertEquals(((TelnetClient)endpoint).getEndpointConfiguration().getPassword(), "12345678");
        Assert.assertEquals(((TelnetClient) endpoint).getEndpointConfiguration().getTimeout(), 10000L);
    }
}
