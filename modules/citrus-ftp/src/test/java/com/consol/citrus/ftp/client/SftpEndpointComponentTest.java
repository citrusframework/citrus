/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.ftp.client;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;


public class SftpEndpointComponentTest {

    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
    private TestContext context = new TestContext();

    @BeforeClass
    public void setup() {
        context.setApplicationContext(applicationContext);
    }

    @Test
    public void testCreateClientEndpoint() throws Exception {
        SftpEndpointComponent component = new SftpEndpointComponent();

        Endpoint endpoint = component.createEndpoint("sftp://localhost:2221", context);

        Assert.assertEquals(endpoint.getClass(), SftpClient.class);

        Assert.assertEquals(((SftpClient)endpoint).getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(((SftpClient)endpoint).getEndpointConfiguration().getPort(), new Integer(2221));
        Assert.assertNull(((SftpClient) endpoint).getEndpointConfiguration().getUser());
        Assert.assertNull(((SftpClient) endpoint).getEndpointConfiguration().getPassword());
        Assert.assertEquals(((SftpClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);

        endpoint = component.createEndpoint("sftp:ftp.consol.de", context);

        Assert.assertEquals(endpoint.getClass(), SftpClient.class);

        Assert.assertEquals(((SftpClient)endpoint).getEndpointConfiguration().getHost(), "ftp.consol.de");
        Assert.assertEquals(((SftpClient)endpoint).getEndpointConfiguration().getPort(), new Integer(22222));
        Assert.assertNull(((SftpClient) endpoint).getEndpointConfiguration().getUser());
        Assert.assertNull(((SftpClient) endpoint).getEndpointConfiguration().getPassword());
        Assert.assertEquals(((SftpClient) endpoint).getEndpointConfiguration().getTimeout(), 5000L);
    }

    @Test
    public void testCreateClientEndpointWithParameters() throws Exception {
        SftpEndpointComponent component = new SftpEndpointComponent();

        reset(applicationContext);
        Endpoint endpoint = component.createEndpoint("sftp:localhost:22220?user=admin&password=consol&timeout=10000", context);

        Assert.assertEquals(endpoint.getClass(), SftpClient.class);

        Assert.assertEquals(((SftpClient)endpoint).getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(((SftpClient)endpoint).getEndpointConfiguration().getPort(), new Integer(22220));
        Assert.assertEquals(((SftpClient) endpoint).getEndpointConfiguration().getUser(), "admin");
        Assert.assertEquals(((SftpClient) endpoint).getEndpointConfiguration().getPassword(), "consol");
        Assert.assertEquals(((SftpClient) endpoint).getEndpointConfiguration().getTimeout(), 10000L);

    }
}