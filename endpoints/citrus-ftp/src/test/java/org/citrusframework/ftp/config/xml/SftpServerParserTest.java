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

import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.ftp.client.SftpEndpointConfiguration;
import org.citrusframework.ftp.server.SftpServer;
import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SftpServerParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testSftpServerParser() {
        Map<String, SftpServer> servers = beanDefinitionContext.getBeansOfType(SftpServer.class);

        Assertions.assertEquals(3, servers.size());

        // 1st server
        SftpServer server = servers.get("sftpServer1");
        Assertions.assertEquals("sftpServer1", server.getName());
        Assertions.assertEquals(22, server.getPort());
        Assertions.assertFalse(server.isAutoStart());
        Assertions.assertTrue(((SftpEndpointConfiguration) server.getEndpointConfiguration()).isAutoLogin());
        Assertions.assertTrue(((SftpEndpointConfiguration) server.getEndpointConfiguration()).isAutoConnect());
        Assertions.assertNull(server.getAllowedKeyPath());
        Assertions.assertNull(server.getHostKeyPath());
        Assertions.assertNull(server.getUserHomePath());
        Assertions.assertNull(server.getUser());
        Assertions.assertNull(server.getPassword());
        Assertions.assertTrue(server.getEndpointAdapter() instanceof DirectEndpointAdapter);
        Assertions.assertNotNull(server.getMessageConverter());
        Assertions.assertNull(server.getActor());

        // 2nd server
        server = servers.get("sftpServer2");
        Assertions.assertEquals("sftpServer2", server.getName());
        Assertions.assertEquals(10022, server.getPort());
        Assertions.assertFalse(server.isAutoStart());
        Assertions.assertFalse(((SftpEndpointConfiguration) server.getEndpointConfiguration()).isAutoLogin());
        Assertions.assertFalse(((SftpEndpointConfiguration) server.getEndpointConfiguration()).isAutoConnect());
        Assertions.assertEquals("classpath:org/citrusframework/sftp/citrus_pub.pem", server.getAllowedKeyPath());
        Assertions.assertEquals("classpath:org/citrusframework/sftp/citrus.pem", server.getHostKeyPath());
        Assertions.assertEquals("/home/user", server.getUserHomePath());
        Assertions.assertEquals("foo", server.getUser());
        Assertions.assertEquals("bar", server.getPassword());
        Assertions.assertTrue(server.getEndpointAdapter() instanceof DirectEndpointAdapter);
        Assertions.assertNull(server.getActor());

        // 3rd server
        server = servers.get("sftpServer3");
        Assertions.assertEquals("sftpServer3", server.getName());
        Assertions.assertEquals(22, server.getPort());
        Assertions.assertFalse(server.isAutoStart());
        Assertions.assertNull(server.getAllowedKeyPath());
        Assertions.assertNull(server.getHostKeyPath());
        Assertions.assertNull(server.getUser());
        Assertions.assertNull(server.getPassword());
        Assertions.assertEquals(beanDefinitionContext.getBean("sftpServerAdapter"), server.getEndpointAdapter());
        Assertions.assertNull(server.getActor());
    }
}
