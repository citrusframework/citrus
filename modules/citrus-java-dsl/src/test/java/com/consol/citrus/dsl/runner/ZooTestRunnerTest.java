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

package com.consol.citrus.dsl.runner;

import com.consol.citrus.TestCase;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.zookeeper.actions.ZooExecuteAction;
import com.consol.citrus.zookeeper.command.AbstractZooCommand;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class ZooTestRunnerTest extends AbstractTestNGUnitTest {

    private ZooKeeper zookeeperClientMock = Mockito.mock(ZooKeeper.class);
    private Stat statMock = prepareStatMock();

    @Test
    public void testZookeeperBuilder() throws KeeperException, InterruptedException {
        final String pwd = "SomePwd";
        final String path = "my-node";
        final String data = "my-data";
        final List<String> children = Arrays.asList("child1", "child2");
        final String newPath = "the-created-node";

        reset(zookeeperClientMock);

        //  prepare info
        when(zookeeperClientMock.getState()).thenReturn(ZooKeeper.States.CONNECTED);
        when(zookeeperClientMock.getSessionId()).thenReturn(100L);
        when(zookeeperClientMock.getSessionPasswd()).thenReturn(pwd.getBytes());
        when(zookeeperClientMock.getSessionTimeout()).thenReturn(200);

        //  prepare create
        when(zookeeperClientMock.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL)).thenReturn(newPath);

        //  prepare exists
        when(zookeeperClientMock.exists(path, false)).thenReturn(statMock);

        //  prepare get-children
        when(zookeeperClientMock.getChildren(path, false)).thenReturn(children);

        //  prepare get-data
        when(zookeeperClientMock.getData(path, false, null)).thenReturn(data.getBytes());

        //  prepare set-data
        when(zookeeperClientMock.setData(path, data.getBytes(), 0)).thenReturn(statMock);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                zookeeper(builder -> builder.client(new com.consol.citrus.zookeeper.client.ZooClient(zookeeperClientMock))
                        .validate("$.responseData.state", ZooKeeper.States.CONNECTED.name())
                        .extract("$.responseData.state","state")
                        .extract("$.responseData.sessionId","sessionId")
                        .extract("$.responseData.sessionPwd","sessionPwd")
                        .extract("$.responseData.sessionTimeout","sessionTimeout")
                        .info()
                        .validateCommandResult((result, context) -> {
                            Assert.assertNotNull(result);
                            Assert.assertEquals(result.getResponseData().get("state"), ZooKeeper.States.CONNECTED.name());
                            Assert.assertEquals(result.getResponseData().get("sessionId"), 100L);
                            Assert.assertEquals(result.getResponseData().get("sessionPwd"), pwd.getBytes());
                            Assert.assertEquals(result.getResponseData().get("sessionTimeout"), 200);
                        }));

                zookeeper(builder -> builder.client(new com.consol.citrus.zookeeper.client.ZooClient(zookeeperClientMock))
                        .create(path, data)
                        .validateCommandResult((result, context) -> {
                            Assert.assertNotNull(result);
                            Assert.assertEquals(result.getResponseData().get(AbstractZooCommand.PATH), newPath);
                        }));

                zookeeper(builder -> builder.client(new com.consol.citrus.zookeeper.client.ZooClient(zookeeperClientMock))
                        .delete(path)
                        .validateCommandResult((result, context) -> verify(zookeeperClientMock).delete(eq(path), eq(0), any(AsyncCallback.VoidCallback.class), isNull())));

                zookeeper(builder -> builder.client(new com.consol.citrus.zookeeper.client.ZooClient(zookeeperClientMock))
                        .exists(path)
                        .validateCommandResult((result, context) -> {
                            Assert.assertNotNull(result);
                            for (Object o : result.getResponseData().values()) {
                                Assert.assertEquals(o.toString(), "1");
                            }
                        }));

                zookeeper(builder -> builder.client(new com.consol.citrus.zookeeper.client.ZooClient(zookeeperClientMock))
                        .children(path)
                        .validateCommandResult((result, context) -> {
                            Assert.assertNotNull(result);
                            Assert.assertEquals(result.getResponseData().get(AbstractZooCommand.CHILDREN), children);
                        }));

                zookeeper(builder -> builder.client(new com.consol.citrus.zookeeper.client.ZooClient(zookeeperClientMock))
                        .get(path)
                        .validateCommandResult((result, context) -> {
                            Assert.assertNotNull(result);
                            Assert.assertEquals(result.getResponseData().get(AbstractZooCommand.DATA), data);
                        }));

                zookeeper(builder -> builder.client(new com.consol.citrus.zookeeper.client.ZooClient(zookeeperClientMock))
                        .set(path, data)
                        .validateCommandResult((result, context) -> {
                            Assert.assertNotNull(result);
                            for (Object o : result.getResponseData().values()) {
                                Assert.assertEquals(o.toString(), "1");
                            }
                        }));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 7);
        Assert.assertEquals(test.getActions().get(0).getClass(), ZooExecuteAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), ZooExecuteAction.class);

        String actionName = "zookeeper-execute";

        ZooExecuteAction action = (ZooExecuteAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), actionName);
        Assert.assertEquals(action.getCommand().getClass(), com.consol.citrus.zookeeper.command.Info.class);

        action = (ZooExecuteAction) test.getActions().get(1);
        Assert.assertEquals(action.getName(), actionName);
        Assert.assertEquals(action.getCommand().getClass(), com.consol.citrus.zookeeper.command.Create.class);

        action = (ZooExecuteAction) test.getActions().get(2);
        Assert.assertEquals(action.getName(), actionName);
        Assert.assertEquals(action.getCommand().getClass(), com.consol.citrus.zookeeper.command.Delete.class);

        action = (ZooExecuteAction) test.getActions().get(3);
        Assert.assertEquals(action.getName(), actionName);
        Assert.assertEquals(action.getCommand().getClass(), com.consol.citrus.zookeeper.command.Exists.class);

        action = (ZooExecuteAction) test.getActions().get(4);
        Assert.assertEquals(action.getName(), actionName);
        Assert.assertEquals(action.getCommand().getClass(), com.consol.citrus.zookeeper.command.GetChildren.class);

        action = (ZooExecuteAction) test.getActions().get(5);
        Assert.assertEquals(action.getName(), actionName);
        Assert.assertEquals(action.getCommand().getClass(), com.consol.citrus.zookeeper.command.GetData.class);

        action = (ZooExecuteAction) test.getActions().get(6);
        Assert.assertEquals(action.getName(), actionName);
        Assert.assertEquals(action.getCommand().getClass(), com.consol.citrus.zookeeper.command.SetData.class);
    }


    private Stat prepareStatMock() {
        Stat stat = Mockito.mock(Stat.class);
        when(stat.getAversion()).thenReturn(1);
        when(stat.getCtime()).thenReturn(1L);
        when(stat.getCversion()).thenReturn(1);
        when(stat.getCzxid()).thenReturn(1L);
        when(stat.getDataLength()).thenReturn(1);
        when(stat.getEphemeralOwner()).thenReturn(1L);
        when(stat.getMtime()).thenReturn(1L);
        when(stat.getMzxid()).thenReturn(1L);
        when(stat.getNumChildren()).thenReturn(1);
        when(stat.getPzxid()).thenReturn(1L);
        when(stat.getVersion()).thenReturn(1);
        return stat;
    }
}
