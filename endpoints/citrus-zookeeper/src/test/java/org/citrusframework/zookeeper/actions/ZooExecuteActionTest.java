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

package org.citrusframework.zookeeper.actions;

import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.zookeeper.client.ZooClient;
import org.citrusframework.zookeeper.command.Info;
import org.apache.zookeeper.ZooKeeper;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class ZooExecuteActionTest extends AbstractTestNGUnitTest {

    private final ZooKeeper zookeeper = Mockito.mock(ZooKeeper.class);

    @Test
    public void testInfo() throws Exception {
        reset(zookeeper);

        when(zookeeper.getState()).thenReturn(ZooKeeper.States.CONNECTED);

        ZooExecuteAction action = new ZooExecuteAction.Builder()
                .command(new Info())
                .client(new ZooClient(zookeeper))
                .build();
        action.execute(context);

        //Assert.assertEquals(action.getCommand().getCommandResult(), null);
    }
}
