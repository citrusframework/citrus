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

package org.citrusframework.ftp.config.xml;

import java.util.Map;

import org.citrusframework.TestActor;
import org.citrusframework.ftp.client.ScpClient;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 2.7.6
 */
public class ScpClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testScpClientParser() {
        Map<String, ScpClient> clients = beanDefinitionContext.getBeansOfType(ScpClient.class);

        Assertions.assertEquals(4, clients.size());

        // 1st scp client
        ScpClient scpClient = clients.get("scpClient1");
        Assertions.assertEquals("localhost", scpClient.getEndpointConfiguration().getHost());
        Assertions.assertEquals("-P", scpClient.getEndpointConfiguration().getPortOption());
        Assertions.assertEquals(22222, scpClient.getEndpointConfiguration().getPort());
        Assertions.assertEquals(DefaultMessageCorrelator.class, scpClient.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertTrue(scpClient.getEndpointConfiguration().isAutoReadFiles());
        Assertions.assertNull(scpClient.getEndpointConfiguration().getPrivateKeyPath());
        Assertions.assertNull(scpClient.getEndpointConfiguration().getPrivateKeyPassword());
        Assertions.assertFalse(scpClient.getEndpointConfiguration().isStrictHostChecking());
        Assertions.assertNull(scpClient.getEndpointConfiguration().getKnownHosts());
        Assertions.assertEquals(5000L, scpClient.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(ErrorHandlingStrategy.PROPAGATE, scpClient.getEndpointConfiguration().getErrorHandlingStrategy());

        // 2nd scp client
        scpClient = clients.get("scpClient2");
        Assertions.assertEquals("localhost", scpClient.getEndpointConfiguration().getHost());
        Assertions.assertEquals("-p", scpClient.getEndpointConfiguration().getPortOption());
        Assertions.assertEquals(22222, scpClient.getEndpointConfiguration().getPort());
        Assertions.assertEquals(DefaultMessageCorrelator.class, scpClient.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertEquals("user", scpClient.getEndpointConfiguration().getUser());
        Assertions.assertEquals("consol", scpClient.getEndpointConfiguration().getPassword());
        Assertions.assertEquals("classpath:org/citrusframework/scp/citrus.priv", scpClient.getEndpointConfiguration().getPrivateKeyPath());
        Assertions.assertEquals("consol", scpClient.getEndpointConfiguration().getPrivateKeyPassword());
        Assertions.assertEquals(10000L, scpClient.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(ErrorHandlingStrategy.THROWS_EXCEPTION, scpClient.getEndpointConfiguration().getErrorHandlingStrategy());

        // 3rd scp client
        scpClient = clients.get("scpClient3");
        Assertions.assertEquals("localhost", scpClient.getEndpointConfiguration().getHost());
        Assertions.assertEquals(22222, scpClient.getEndpointConfiguration().getPort());
        Assertions.assertNotNull(scpClient.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(beanDefinitionContext.getBean("replyMessageCorrelator"), scpClient.getEndpointConfiguration().getCorrelator());

        // 4th scp client
        scpClient = clients.get("scpClient4");
        Assertions.assertNotNull(scpClient.getActor());
        Assertions.assertEquals(beanDefinitionContext.getBean("testActor", TestActor.class), scpClient.getActor());
        Assertions.assertEquals(250L, scpClient.getEndpointConfiguration().getPollingInterval());
    }
}
