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

package org.citrusframework.kafka.config.xml;

import java.util.Map;

import org.citrusframework.junit.jupiter.AbstractBeanDefinitionParserTest;
import org.citrusframework.kafka.embedded.EmbeddedKafkaServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 2.7
 */
public class KafkaEmbeddedServerParserTest extends AbstractBeanDefinitionParserTest {

    @Test
    public void testEmbeddedServerParser() {
        Map<String, EmbeddedKafkaServer> server = beanDefinitionContext.getBeansOfType(EmbeddedKafkaServer.class);

        Assertions.assertEquals(2, server.size());

        // 1st server
        EmbeddedKafkaServer kafkaServer = server.get("kafkaServer1");
        Assertions.assertEquals("citrus", kafkaServer.getTopics());
        Assertions.assertEquals(1, kafkaServer.getPartitions());
        Assertions.assertTrue(kafkaServer.isAutoDeleteLogs());
        Assertions.assertNull(kafkaServer.getLogDirPath());
        Assertions.assertTrue(kafkaServer.getKafkaServerPort() >= 9092);
        Assertions.assertTrue(kafkaServer.getControllerPort() > 0);
        Assertions.assertEquals(0L, kafkaServer.getBrokerProperties().size());

        // 2nd server
        kafkaServer = server.get("kafkaServer2");
        Assertions.assertEquals("hello,foo", kafkaServer.getTopics());
        Assertions.assertEquals(2, kafkaServer.getPartitions());
        Assertions.assertFalse(kafkaServer.isAutoDeleteLogs());
        Assertions.assertEquals("/path/to/logs", kafkaServer.getLogDirPath());
        Assertions.assertEquals(9091, kafkaServer.getKafkaServerPort());
        Assertions.assertEquals(21181, kafkaServer.getControllerPort());
        Assertions.assertEquals(1L, kafkaServer.getBrokerProperties().size());
        Assertions.assertEquals("1", kafkaServer.getBrokerProperties().get("broker.id"));
    }
}
