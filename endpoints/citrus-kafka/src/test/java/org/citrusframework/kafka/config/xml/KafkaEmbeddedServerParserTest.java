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

package org.citrusframework.kafka.config.xml;

import org.citrusframework.kafka.embedded.EmbeddedKafkaServer;
import org.citrusframework.testng.AbstractBeanDefinitionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class KafkaEmbeddedServerParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testEmbeddedServerParser() {
        Map<String, EmbeddedKafkaServer> server = beanDefinitionContext.getBeansOfType(EmbeddedKafkaServer.class);

        Assert.assertEquals(server.size(), 2);

        // 1st server
        EmbeddedKafkaServer kafkaServer = server.get("kafkaServer1");
        Assert.assertEquals(kafkaServer.getTopics(), "citrus");
        Assert.assertEquals(kafkaServer.getPartitions(), 1);
        Assert.assertTrue(kafkaServer.isAutoDeleteLogs());
        Assert.assertNull(kafkaServer.getLogDirPath());
        Assert.assertTrue(kafkaServer.getKafkaServerPort() >= 9092);
        Assert.assertTrue(kafkaServer.getZookeeperPort() > 0);
        Assert.assertEquals(kafkaServer.getBrokerProperties().size(), 0L);

        // 2nd server
        kafkaServer = server.get("kafkaServer2");
        Assert.assertEquals(kafkaServer.getTopics(), "hello,foo");
        Assert.assertEquals(kafkaServer.getPartitions(), 2);
        Assert.assertFalse(kafkaServer.isAutoDeleteLogs());
        Assert.assertEquals(kafkaServer.getLogDirPath(), "/path/to/logs");
        Assert.assertEquals(kafkaServer.getKafkaServerPort(), 9091);
        Assert.assertEquals(kafkaServer.getZookeeperPort(), 21181);
        Assert.assertEquals(kafkaServer.getBrokerProperties().size(), 1L);
        Assert.assertEquals(kafkaServer.getBrokerProperties().get("broker.id"), "1");
    }

}
