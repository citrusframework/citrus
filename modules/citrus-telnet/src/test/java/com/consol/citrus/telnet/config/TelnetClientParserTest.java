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

package com.consol.citrus.telnet.config;

import com.consol.citrus.telnet.client.TelnetClient;
import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Magnus Karlsson
 * @since 2.6
 */
public class TelnetClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testTelnetClientParser() {
        Map<String, TelnetClient> clients = beanDefinitionContext.getBeansOfType(TelnetClient.class);

        Assert.assertEquals(clients.size(), 2);

        // 1st client
        TelnetClient client = clients.get("telnetClient1");
        Assert.assertEquals(client.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(client.getEndpointConfiguration().getPort(), 23);
        Assert.assertEquals(client.getEndpointConfiguration().getUser(), "citrus");
        Assert.assertNull(client.getEndpointConfiguration().getPassword());
        Assert.assertEquals(client.getEndpointConfiguration().getCommandTimeout(), 1000 * 60 * 5);
        Assert.assertEquals(client.getEndpointConfiguration().getConnectionTimeout(), 1000 * 60 * 1);
        Assert.assertNotNull(client.getEndpointConfiguration().getCorrelator());

        // 2nd client
        client = clients.get("telnetClient2");
        Assert.assertEquals(client.getEndpointConfiguration().getHost(), "dev7");
        Assert.assertEquals(client.getEndpointConfiguration().getPort(), 23);
        Assert.assertEquals(client.getEndpointConfiguration().getUser(), "foo");
        Assert.assertEquals(client.getEndpointConfiguration().getPassword(), "bar");
        Assert.assertEquals(client.getEndpointConfiguration().getCommandTimeout(), 10000);
        Assert.assertEquals(client.getEndpointConfiguration().getConnectionTimeout(), 5000);
        Assert.assertNotNull(client.getEndpointConfiguration().getCorrelator());
    }
}
