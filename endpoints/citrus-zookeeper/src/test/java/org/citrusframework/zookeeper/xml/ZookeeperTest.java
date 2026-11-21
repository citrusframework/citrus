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

package org.citrusframework.zookeeper.xml;

import java.util.Map;

import org.citrusframework.TestCase;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.xml.XmlTestLoader;
import org.citrusframework.xml.actions.XmlTestActionBuilder;
import org.citrusframework.zookeeper.actions.ZooActionTestSupport;
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

public class ZookeeperTest extends AbstractXmlActionTest {

    @Test
    public void shouldLookupZookeeperActionBuilder() throws Exception {
        Assertions.assertTrue(XmlTestActionBuilder.lookup("zookeeper").isPresent());
        Assertions.assertEquals(Zookeeper.class, XmlTestActionBuilder.lookup("zookeeper").get().getClass());
    }

    @Test
    public void shouldLoadInfoCommand() throws Exception {
        ZooClient client = bindZooClient();

        ZooExecuteAction action = loadZookeeperAction("zookeeper-info.citrus.it.xml");

        Assertions.assertEquals(Info.class, action.getCommand().getClass());
        Assertions.assertEquals("Get server info", action.getDescription());
        Assertions.assertSame(client, action.getZookeeperClient());
    }

    @Test
    public void shouldLoadCreateCommand() throws Exception {
        ZooClient client = bindZooClient();

        ZooExecuteAction action = loadZookeeperAction("zookeeper-create.citrus.it.xml");

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

        ZooExecuteAction action = loadZookeeperAction("zookeeper-create.citrus.it.xml");

        action.execute(context);

        Assertions.assertEquals("/some-path", context.getVariable("createdPath"));
    }

    @Test
    public void shouldLoadDeleteCommand() throws Exception {
        bindZooClient();

        ZooExecuteAction action = loadZookeeperAction("zookeeper-delete.citrus.it.xml");

        Assertions.assertEquals(Delete.class, action.getCommand().getClass());
        Assertions.assertEquals("/some-path", action.getCommand().getParameters().get("path"));
        Assertions.assertEquals(1, action.getCommand().getParameters().get("version"));
    }

    @Test
    public void shouldLoadExistsCommand() throws Exception {
        bindZooClient();

        ZooExecuteAction action = loadZookeeperAction("zookeeper-exists.citrus.it.xml");

        Assertions.assertEquals(Exists.class, action.getCommand().getClass());
        Assertions.assertEquals("/some-path", action.getCommand().getParameters().get("path"));
    }

    @Test
    public void shouldLoadGetCommand() throws Exception {
        bindZooClient();

        ZooExecuteAction action = loadZookeeperAction("zookeeper-get.citrus.it.xml");

        Assertions.assertEquals(GetData.class, action.getCommand().getClass());
        Assertions.assertEquals("/some-path", action.getCommand().getParameters().get("path"));
    }

    @Test
    public void shouldLoadSetCommand() throws Exception {
        bindZooClient();

        ZooExecuteAction action = loadZookeeperAction("zookeeper-set.citrus.it.xml");

        Assertions.assertEquals(SetData.class, action.getCommand().getClass());
        Map<String, Object> parameters = action.getCommand().getParameters();
        Assertions.assertEquals("/some-path", parameters.get("path"));
        Assertions.assertEquals("some-data", parameters.get("data"));
        Assertions.assertEquals(1, parameters.get("version"));
    }

    @Test
    public void shouldLoadChildrenCommand() throws Exception {
        bindZooClient();

        ZooExecuteAction action = loadZookeeperAction("zookeeper-children.citrus.it.xml");

        Assertions.assertEquals(GetChildren.class, action.getCommand().getClass());
        Assertions.assertEquals("/some-path", action.getCommand().getParameters().get("path"));
    }

    @Test
    public void shouldUseDefaultClientWhenNotConfigured() throws Exception {
        ZooClient boundClient = bindZooClient();

        ZooExecuteAction action = loadZookeeperAction("zookeeper-info-default-client.citrus.it.xml");

        Assertions.assertNotNull(action.getZookeeperClient());
        Assertions.assertNotSame(boundClient, action.getZookeeperClient());
    }

    @Test
    public void shouldRejectUnknownCommand() throws Exception {
        bindZooClient();

        XmlTestLoader testLoader = createTestLoader(
                "classpath:org/citrusframework/zookeeper/xml/zookeeper-unknown.citrus.it.xml");

        Assertions.assertThrows(CitrusRuntimeException.class, testLoader::load);
    }

    @Test
    public void shouldRejectNonNumericVersion() throws Exception {
        bindZooClient();

        XmlTestLoader testLoader = createTestLoader(
                "classpath:org/citrusframework/zookeeper/xml/zookeeper-delete-invalid-version.citrus.it.xml");

        Assertions.assertThrows(CitrusRuntimeException.class, testLoader::load);
    }

    @Test
    public void shouldRejectExtractWithoutVariable() throws Exception {
        bindZooClient();

        XmlTestLoader testLoader = createTestLoader(
                "classpath:org/citrusframework/zookeeper/xml/zookeeper-extract-no-variable.citrus.it.xml");

        Assertions.assertThrows(CitrusRuntimeException.class, testLoader::load);
    }

    @Test
    public void shouldRejectMultipleCommands() {
        Zookeeper builder = new Zookeeper();
        org.citrusframework.zookeeper.xml.Create create = new org.citrusframework.zookeeper.xml.Create();
        create.setPath("/some-path");
        builder.setCreate(create);
        org.citrusframework.zookeeper.xml.Delete delete = new org.citrusframework.zookeeper.xml.Delete();
        delete.setPath("/some-path");
        builder.setDelete(delete);

        Assertions.assertThrows(CitrusRuntimeException.class, builder::build);
    }

    @Test
    public void shouldBuildTwiceWithoutDuplication() throws Exception {
        Zookeeper builder = new Zookeeper();
        org.citrusframework.zookeeper.xml.Create create = new org.citrusframework.zookeeper.xml.Create();
        create.setPath("/some-path");
        builder.setCreate(create);
        Zookeeper.Extraction extraction = new Zookeeper.Extraction();
        extraction.setPath("$.responseData.path");
        extraction.setVariable("extractedPath");
        builder.setExtract(java.util.List.of(extraction));

        builder.build();
        ZooExecuteAction second = builder.build();

        Assertions.assertEquals(1, ZooActionTestSupport.messageProcessorCount(second));
    }

    @Test
    public void shouldRejectBlankValidatePath() {
        Zookeeper builder = new Zookeeper();
        org.citrusframework.zookeeper.xml.Create create = new org.citrusframework.zookeeper.xml.Create();
        create.setPath("/some-path");
        builder.setCreate(create);
        Zookeeper.Validation validation = new Zookeeper.Validation();
        validation.setPath("  ");
        validation.setValue("/some-path");
        builder.setValidate(java.util.List.of(validation));

        Assertions.assertThrows(CitrusRuntimeException.class, builder::build);
    }

    @Test
    public void shouldRejectUnresolvableClient() {
        ReferenceResolver resolver = org.mockito.Mockito.mock(ReferenceResolver.class);
        org.mockito.Mockito.when(resolver.resolve("unknownClient", ZooClient.class))
                .thenThrow(new CitrusRuntimeException("Unable to find bean reference for name 'unknownClient'"));

        Zookeeper builder = new Zookeeper();
        builder.setReferenceResolver(resolver);
        builder.setClient("unknownClient");
        builder.setInfo(new org.citrusframework.zookeeper.xml.Info());

        Assertions.assertThrows(CitrusRuntimeException.class, builder::build);
    }

    private ZooExecuteAction loadZookeeperAction(String source) {
        XmlTestLoader testLoader = createTestLoader(
                "classpath:org/citrusframework/zookeeper/xml/" + source);

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        return (ZooExecuteAction) result.getTestAction(0);
    }
}
