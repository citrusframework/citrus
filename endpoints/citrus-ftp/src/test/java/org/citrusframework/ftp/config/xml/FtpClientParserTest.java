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
import org.citrusframework.ftp.client.FtpClient;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 2.0
 */
public class FtpClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testFtpClientParser() {
        Map<String, FtpClient> clients = beanDefinitionContext.getBeansOfType(FtpClient.class);

        Assertions.assertEquals(4, clients.size());

        // 1st ftp client
        FtpClient ftpClient = clients.get("ftpClient1");
        Assertions.assertEquals("localhost", ftpClient.getEndpointConfiguration().getHost());
        Assertions.assertEquals(22222, ftpClient.getEndpointConfiguration().getPort());
        Assertions.assertEquals(DefaultMessageCorrelator.class, ftpClient.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertTrue(ftpClient.getEndpointConfiguration().isAutoReadFiles());
        Assertions.assertTrue(ftpClient.getEndpointConfiguration().isLocalPassiveMode());
        Assertions.assertEquals(5000L, ftpClient.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(ErrorHandlingStrategy.PROPAGATE, ftpClient.getEndpointConfiguration().getErrorHandlingStrategy());

        // 2nd ftp client
        ftpClient = clients.get("ftpClient2");
        Assertions.assertEquals("localhost", ftpClient.getEndpointConfiguration().getHost());
        Assertions.assertEquals(22222, ftpClient.getEndpointConfiguration().getPort());
        Assertions.assertEquals(DefaultMessageCorrelator.class, ftpClient.getEndpointConfiguration().getCorrelator().getClass());
        Assertions.assertEquals("user", ftpClient.getEndpointConfiguration().getUser());
        Assertions.assertEquals("consol", ftpClient.getEndpointConfiguration().getPassword());
        Assertions.assertFalse(ftpClient.getEndpointConfiguration().isAutoReadFiles());
        Assertions.assertFalse(ftpClient.getEndpointConfiguration().isLocalPassiveMode());
        Assertions.assertEquals(10000L, ftpClient.getEndpointConfiguration().getTimeout());
        Assertions.assertEquals(ErrorHandlingStrategy.THROWS_EXCEPTION, ftpClient.getEndpointConfiguration().getErrorHandlingStrategy());

        // 3rd ftp client
        ftpClient = clients.get("ftpClient3");
        Assertions.assertEquals("localhost", ftpClient.getEndpointConfiguration().getHost());
        Assertions.assertEquals(22222, ftpClient.getEndpointConfiguration().getPort());
        Assertions.assertNotNull(ftpClient.getEndpointConfiguration().getCorrelator());
        Assertions.assertEquals(beanDefinitionContext.getBean("replyMessageCorrelator"), ftpClient.getEndpointConfiguration().getCorrelator());

        // 4th ftp client
        ftpClient = clients.get("ftpClient4");
        Assertions.assertNotNull(ftpClient.getActor());
        Assertions.assertEquals(beanDefinitionContext.getBean("testActor", TestActor.class), ftpClient.getActor());
        Assertions.assertEquals(250L, ftpClient.getEndpointConfiguration().getPollingInterval());
    }
}
