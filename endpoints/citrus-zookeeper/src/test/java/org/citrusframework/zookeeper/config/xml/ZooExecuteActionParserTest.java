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

import java.util.Map;

import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.citrusframework.zookeeper.actions.ZooExecuteAction;
import org.citrusframework.zookeeper.client.ZooClient;
import org.citrusframework.zookeeper.command.Create;
import org.citrusframework.zookeeper.command.Info;
import org.springframework.beans.factory.BeanCreationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ZooExecuteActionParserTest extends AbstractActionParserTest<ZooExecuteAction> {

    @Test
    public void testZookeeperExecuteActionParser() {
        assertActionCount(2);
        assertActionClassAndName(ZooExecuteAction.class, "zookeeper-execute");

        ZooExecuteAction action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(Info.class, action.getCommand().getClass());
        Assertions.assertEquals(beanDefinitionContext.getBean("myZookeeperClient", ZooClient.class), action.getZookeeperClient());
        Assertions.assertEquals(0, action.getCommand().getParameters().size());
        Assertions.assertEquals("{a:\"some thing\"}", action.getExpectedCommandResult());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(Create.class, action.getCommand().getClass());
        Assertions.assertEquals(beanDefinitionContext.getBean("myZookeeperClient", ZooClient.class), action.getZookeeperClient());
        Assertions.assertEquals(4, action.getCommand().getParameters().size());
        assertParametersContainValue(action.getCommand().getParameters(), "path", "/some-path");
        assertParametersContainValue(action.getCommand().getParameters(), "mode", "PERSISTENT");
        assertParametersContainValue(action.getCommand().getParameters(), "acl", "OPEN_ACL_UNSAFE");
        assertParametersContainValue(action.getCommand().getParameters(), "data", "more data");
        Assertions.assertEquals("{b:\"some thing\"}", action.getExpectedCommandResult());
    }

    private void assertParametersContainValue(Map<String, Object> parameters, String key, String value) {
        Assertions.assertTrue(parameters.containsKey(key));
        Assertions.assertEquals(value, parameters.get(key));
    }

    @Test
    public void testZookeeperExecuteActionParserFailed() {
        Assertions.assertThrows(BeanCreationException.class, () -> createApplicationContext("failed"));
    }
}
