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

package com.consol.citrus.zookeeper.config.xml;

import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;
import com.consol.citrus.zookeeper.client.ZookeeperClient;
import com.consol.citrus.zookeeper.client.ZookeeperClientConfig;
import org.apache.zookeeper.ZooKeeper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

import static com.consol.citrus.zookeeper.client.ZookeeperClientConfig.ZooKeeperClientConfigBuilder.DEFAULT_TIMEOUT;
import static com.consol.citrus.zookeeper.client.ZookeeperClientConfig.ZooKeeperClientConfigBuilder.DEFAULT_URL;

/**
 * @author Martin Maher
 */
public class ZookeeperClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testZooKeeperClientParser() throws IOException {
        Map<String, ZookeeperClient> clients = beanDefinitionContext.getBeansOfType(ZookeeperClient.class);

        Assert.assertEquals(clients.size(), 2);

        // 1st client
        String clientId1 = "zookeeperClient1";
        ZookeeperClient zookeeperClient = clients.get(clientId1);
        assertConfigParsed(zookeeperClient, clientId1, DEFAULT_URL, DEFAULT_TIMEOUT);

        // 2nd client
        String clientId2 = "zookeeperClient2";
        ZookeeperClient zookeeperClient2 = clients.get(clientId2);
        assertConfigParsed(zookeeperClient2, clientId2, "http://localhost:2376", 2000);
    }

    private void assertConfigParsed(ZookeeperClient zookeeperClient, String expectedClientId, String expectedUrl, int expectedTimeout) {
        ZookeeperClientConfig config = zookeeperClient.getZookeeperClientConfig();
        Assert.assertNotNull(config);
        Assert.assertEquals(config.getId(), expectedClientId);
        Assert.assertEquals(config.getUrl(), expectedUrl);
        Assert.assertEquals(config.getTimeout(), expectedTimeout);
    }
}
