/*
 * Copyright 2006-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.dsl.design;

import org.citrusframework.TestCase;
import org.citrusframework.dsl.UnitTestSupport;
import org.citrusframework.zookeeper.actions.ZooExecuteAction;
import org.citrusframework.zookeeper.command.AbstractZooCommand;
import org.citrusframework.zookeeper.command.Create;
import org.citrusframework.zookeeper.command.Delete;
import org.citrusframework.zookeeper.command.Exists;
import org.citrusframework.zookeeper.command.GetChildren;
import org.citrusframework.zookeeper.command.GetData;
import org.citrusframework.zookeeper.command.SetData;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class ZooTestDesignerTest extends UnitTestSupport {

    @Test
    public void testZooBuilder() {
        final String actionName = "zookeeper-execute";
        final String path = "my-node";
        final String data = "my-data";
        final String mode = "custom-mode";
        final String acl = "custom-acl";
        final int version = 10;

        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                zookeeper().info().validateCommandResult((result, context) -> Assert.assertNotNull(result));

                zookeeper().create(path, data);
                zookeeper().create(path, data).mode(mode).acl(acl);
                zookeeper().delete(path);
                zookeeper().delete(path).version(version);
                zookeeper().exists(path);
                zookeeper().children(path);
                zookeeper().set(path, data);
                zookeeper().get(path);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 9);
        Assert.assertEquals(test.getActions().get(0).getClass(), ZooExecuteAction.class);

        ZooExecuteAction action = (ZooExecuteAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), actionName);
        Assert.assertEquals(action.getCommand().getClass(), org.citrusframework.zookeeper.command.Info.class);
        Assert.assertNotNull(action.getCommand().getResultCallback());

        action = (ZooExecuteAction) test.getActions().get(1);
        Assert.assertEquals(action.getName(), actionName);
        Assert.assertEquals(action.getCommand().getClass(), Create.class);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractZooCommand.PATH), path);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractZooCommand.DATA), data);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractZooCommand.ACL), ZooExecuteAction.Builder.DEFAULT_ACL);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractZooCommand.MODE), ZooExecuteAction.Builder.DEFAULT_MODE);

        action = (ZooExecuteAction) test.getActions().get(2);
        Assert.assertEquals(action.getName(), actionName);
        Assert.assertEquals(action.getCommand().getClass(), Create.class);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractZooCommand.PATH), path);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractZooCommand.DATA), data);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractZooCommand.ACL), acl);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractZooCommand.MODE), mode);

        action = (ZooExecuteAction) test.getActions().get(3);
        Assert.assertEquals(action.getName(), actionName);
        Assert.assertEquals(action.getCommand().getClass(), Delete.class);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractZooCommand.PATH), path);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractZooCommand.VERSION), ZooExecuteAction.Builder.DEFAULT_VERSION);

        action = (ZooExecuteAction) test.getActions().get(4);
        Assert.assertEquals(action.getName(), actionName);
        Assert.assertEquals(action.getCommand().getClass(), Delete.class);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractZooCommand.PATH), path);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractZooCommand.VERSION), version);

        action = (ZooExecuteAction) test.getActions().get(5);
        Assert.assertEquals(action.getName(), actionName);
        Assert.assertEquals(action.getCommand().getClass(), Exists.class);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractZooCommand.PATH), path);

        action = (ZooExecuteAction) test.getActions().get(6);
        Assert.assertEquals(action.getName(), actionName);
        Assert.assertEquals(action.getCommand().getClass(), GetChildren.class);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractZooCommand.PATH), path);

        action = (ZooExecuteAction) test.getActions().get(7);
        Assert.assertEquals(action.getName(), actionName);
        Assert.assertEquals(action.getCommand().getClass(), SetData.class);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractZooCommand.PATH), path);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractZooCommand.DATA), data);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractZooCommand.VERSION), ZooExecuteAction.Builder.DEFAULT_VERSION);

        action = (ZooExecuteAction) test.getActions().get(8);
        Assert.assertEquals(action.getName(), actionName);
        Assert.assertEquals(action.getCommand().getClass(), GetData.class);
        Assert.assertEquals(action.getCommand().getParameters().get(AbstractZooCommand.PATH), path);
    }
}
