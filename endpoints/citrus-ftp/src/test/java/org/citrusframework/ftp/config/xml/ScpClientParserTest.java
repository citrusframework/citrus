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

package org.citrusframework.ftp.config.xml;

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.ftp.client.ScpClient;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class ScpClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testScpClientParser() {
        Map<String, ScpClient> clients = beanDefinitionContext.getBeansOfType(ScpClient.class);

        Assert.assertEquals(clients.size(), 4);

        // 1st scp client
        ScpClient scpClient = clients.get("scpClient1");
        Assert.assertEquals(scpClient.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(scpClient.getEndpointConfiguration().getPortOption(), "-P");
        Assert.assertEquals(scpClient.getEndpointConfiguration().getPort(), 22222);
        Assert.assertEquals(scpClient.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertTrue(scpClient.getEndpointConfiguration().isAutoReadFiles());
        Assert.assertNull(scpClient.getEndpointConfiguration().getPrivateKeyPath());
        Assert.assertNull(scpClient.getEndpointConfiguration().getPrivateKeyPassword());
        Assert.assertFalse(scpClient.getEndpointConfiguration().isStrictHostChecking());
        Assert.assertNull(scpClient.getEndpointConfiguration().getKnownHosts());
        Assert.assertEquals(scpClient.getEndpointConfiguration().getTimeout(), 5000L);
        Assert.assertEquals(scpClient.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.PROPAGATE);

        // 2nd scp client
        scpClient = clients.get("scpClient2");
        Assert.assertEquals(scpClient.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(scpClient.getEndpointConfiguration().getPortOption(), "-p");
        Assert.assertEquals(scpClient.getEndpointConfiguration().getPort(), 22222);
        Assert.assertEquals(scpClient.getEndpointConfiguration().getCorrelator().getClass(), DefaultMessageCorrelator.class);
        Assert.assertEquals(scpClient.getEndpointConfiguration().getUser(), "user");
        Assert.assertEquals(scpClient.getEndpointConfiguration().getPassword(), "consol");
        Assert.assertEquals(scpClient.getEndpointConfiguration().getPrivateKeyPath(), "classpath:org/citrusframework/scp/citrus.priv");
        Assert.assertEquals(scpClient.getEndpointConfiguration().getPrivateKeyPassword(), "consol");
        Assert.assertEquals(scpClient.getEndpointConfiguration().getTimeout(), 10000L);
        Assert.assertEquals(scpClient.getEndpointConfiguration().getErrorHandlingStrategy(), ErrorHandlingStrategy.THROWS_EXCEPTION);

        // 3rd scp client
        scpClient = clients.get("scpClient3");
        Assert.assertEquals(scpClient.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(scpClient.getEndpointConfiguration().getPort(), 22222);
        Assert.assertNotNull(scpClient.getEndpointConfiguration().getCorrelator());
        Assert.assertEquals(scpClient.getEndpointConfiguration().getCorrelator(), beanDefinitionContext.getBean("replyMessageCorrelator"));

        // 4th scp client
        scpClient = clients.get("scpClient4");
        Assert.assertNotNull(scpClient.getActor());
        Assert.assertEquals(scpClient.getActor(), beanDefinitionContext.getBean("testActor", TestActor.class));
        Assert.assertEquals(scpClient.getEndpointConfiguration().getPollingInterval(), 250L);
    }
}
