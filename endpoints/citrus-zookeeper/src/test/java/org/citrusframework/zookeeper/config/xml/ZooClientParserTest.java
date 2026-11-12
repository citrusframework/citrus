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

package org.citrusframework.zookeeper.config.xml;

import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.citrusframework.zookeeper.client.ZooClient;
import org.citrusframework.zookeeper.client.ZooClientConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.citrusframework.zookeeper.client.ZooClientConfig.ZooKeeperClientConfigBuilder.DEFAULT_TIMEOUT;
import static org.citrusframework.zookeeper.client.ZooClientConfig.ZooKeeperClientConfigBuilder.DEFAULT_URL;

public class ZooClientParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testZooKeeperClientParser() {
        Map<String, ZooClient> clients = beanDefinitionContext.getBeansOfType(ZooClient.class);

        Assertions.assertEquals(2, clients.size());

        // 1st client
        String clientId1 = "zookeeperClient1";
        ZooClient zookeeperClient = clients.get(clientId1);
        assertConfigParsed(zookeeperClient, clientId1, DEFAULT_URL, DEFAULT_TIMEOUT);

        // 2nd client
        String clientId2 = "zookeeperClient2";
        ZooClient zookeeperClient2 = clients.get(clientId2);
        assertConfigParsed(zookeeperClient2, clientId2, "http://localhost:2376", 2000);
    }

    private void assertConfigParsed(ZooClient zookeeperClient, String expectedClientId, String expectedUrl, int expectedTimeout) {
        ZooClientConfig config = zookeeperClient.getZookeeperClientConfig();
        Assertions.assertNotNull(config);
        Assertions.assertEquals(expectedClientId, config.getId());
        Assertions.assertEquals(expectedUrl, config.getUrl());
        Assertions.assertEquals(expectedTimeout, config.getTimeout());
    }
}
