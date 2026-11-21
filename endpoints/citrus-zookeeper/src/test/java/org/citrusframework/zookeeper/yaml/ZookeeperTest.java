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

package org.citrusframework.zookeeper.yaml;

import java.util.Map;

import org.citrusframework.TestCase;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.yaml.YamlTestLoader;
import org.citrusframework.yaml.actions.YamlTestActionBuilder;
import org.citrusframework.zookeeper.actions.ZooExecuteAction;
import org.citrusframework.zookeeper.client.ZooClient;
import org.citrusframework.zookeeper.command.Create;
import org.citrusframework.zookeeper.command.Delete;
import org.citrusframework.zookeeper.command.Exists;
import org.citrusframework.zookeeper.command.GetChildren;
import org.citrusframework.zookeeper.command.GetData;
import org.citrusframework.zookeeper.command.Info;
import org.citrusframework.zookeeper.command.SetData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ZookeeperTest extends AbstractYamlActionTest {

    @Test
    public void shouldLookupZookeeperActionBuilder() throws Exception {
        Assertions.assertTrue(YamlTestActionBuilder.lookup("zookeeper").isPresent());
        Assertions.assertEquals(Zookeeper.class, YamlTestActionBuilder.lookup("zookeeper").get().getClass());
    }

    @Test
    public void shouldLoadInfoCommand() throws Exception {
        ZooClient client = bindZooClient();

        ZooExecuteAction action = loadZookeeperAction("zookeeper-info.citrus.it.yaml");

        Assertions.assertEquals(Info.class, action.getCommand().getClass());
        Assertions.assertEquals("Get server info", action.getDescription());
        Assertions.assertSame(client, action.getZookeeperClient());
    }

    @Test
    public void shouldLoadCreateCommand() throws Exception {
        ZooClient client = bindZooClient();

        ZooExecuteAction action = loadZookeeperAction("zookeeper-create.citrus.it.yaml");

        Assertions.assertEquals(Create.class, action.getCommand().getClass());
        Map<String, Object> parameters = action.getCommand().getParameters();
        Assertions.assertEquals("/some-path", parameters.get("path"));
        Assertions.assertEquals("some-data", parameters.get("data"));
        Assertions.assertEquals("PERSISTENT", parameters.get("mode"));
        Assertions.assertEquals("OPEN_ACL_UNSAFE", parameters.get("acl"));
        Assertions.assertEquals("Create a node", action.getDescription());
        Assertions.assertSame(client, action.getZookeeperClient());
        Assertions.assertEquals("{\"responseData\":{\"path\":\"/some-path\"}}", action.getExpectedCommandResult());
        Assertions.assertEquals("/some-path",
                action.getJsonPathMessageValidationContext().getJsonPathExpressions().get("$.responseData.path"));
    }

    @Test
    public void shouldExecuteCreateCommandAndExtractVariable() throws Exception {
        bindZooClient();

        ZooExecuteAction action = loadZookeeperAction("zookeeper-create.citrus.it.yaml");

        action.execute(context);

        Assertions.assertEquals("/some-path", context.getVariable("createdPath"));
    }

    @Test
    public void shouldLoadDeleteCommand() throws Exception {
        bindZooClient();

        ZooExecuteAction action = loadZookeeperAction("zookeeper-delete.citrus.it.yaml");

        Assertions.assertEquals(Delete.class, action.getCommand().getClass());
        Assertions.assertEquals("/some-path", action.getCommand().getParameters().get("path"));
        Assertions.assertEquals(1, action.getCommand().getParameters().get("version"));
    }

    @Test
    public void shouldLoadExistsCommand() throws Exception {
        bindZooClient();

        ZooExecuteAction action = loadZookeeperAction("zookeeper-exists.citrus.it.yaml");

        Assertions.assertEquals(Exists.class, action.getCommand().getClass());
        Assertions.assertEquals("/some-path", action.getCommand().getParameters().get("path"));
    }

    @Test
    public void shouldLoadGetCommand() throws Exception {
        bindZooClient();

        ZooExecuteAction action = loadZookeeperAction("zookeeper-get.citrus.it.yaml");

        Assertions.assertEquals(GetData.class, action.getCommand().getClass());
        Assertions.assertEquals("/some-path", action.getCommand().getParameters().get("path"));
    }

    @Test
    public void shouldLoadSetCommand() throws Exception {
        bindZooClient();

        ZooExecuteAction action = loadZookeeperAction("zookeeper-set.citrus.it.yaml");

        Assertions.assertEquals(SetData.class, action.getCommand().getClass());
        Map<String, Object> parameters = action.getCommand().getParameters();
        Assertions.assertEquals("/some-path", parameters.get("path"));
        Assertions.assertEquals("some-data", parameters.get("data"));
        Assertions.assertEquals(1, parameters.get("version"));
    }

    @Test
    public void shouldLoadChildrenCommand() throws Exception {
        bindZooClient();

        ZooExecuteAction action = loadZookeeperAction("zookeeper-children.citrus.it.yaml");

        Assertions.assertEquals(GetChildren.class, action.getCommand().getClass());
        Assertions.assertEquals("/some-path", action.getCommand().getParameters().get("path"));
    }

    @Test
    public void shouldUseDefaultClientWhenNotConfigured() throws Exception {
        ZooClient boundClient = bindZooClient();

        ZooExecuteAction action = loadZookeeperAction("zookeeper-info-default-client.citrus.it.yaml");

        Assertions.assertNotNull(action.getZookeeperClient());
        Assertions.assertNotSame(boundClient, action.getZookeeperClient());
    }

    @Test
    public void shouldRejectUnknownCommand() throws Exception {
        bindZooClient();

        YamlTestLoader testLoader = createTestLoader(
                "classpath:org/citrusframework/zookeeper/yaml/zookeeper-unknown.citrus.it.yaml");

        Assertions.assertThrows(CitrusRuntimeException.class, testLoader::load);
    }

    @Test
    public void shouldRejectNonNumericVersion() throws Exception {
        bindZooClient();

        YamlTestLoader testLoader = createTestLoader(
                "classpath:org/citrusframework/zookeeper/yaml/zookeeper-delete-invalid-version.citrus.it.yaml");

        Assertions.assertThrows(CitrusRuntimeException.class, testLoader::load);
    }

    @Test
    public void shouldRejectExtractWithoutVariable() throws Exception {
        bindZooClient();

        YamlTestLoader testLoader = createTestLoader(
                "classpath:org/citrusframework/zookeeper/yaml/zookeeper-extract-no-variable.citrus.it.yaml");

        Assertions.assertThrows(CitrusRuntimeException.class, testLoader::load);
    }

    private ZooExecuteAction loadZookeeperAction(String source) {
        YamlTestLoader testLoader = createTestLoader(
                "classpath:org/citrusframework/zookeeper/yaml/" + source);

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        return (ZooExecuteAction) result.getTestAction(0);
    }
}
