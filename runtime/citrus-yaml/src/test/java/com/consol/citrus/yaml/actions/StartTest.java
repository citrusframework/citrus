/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.yaml.actions;

import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseMetaInfo;
import com.consol.citrus.actions.StartServerAction;
import com.consol.citrus.server.Server;
import com.consol.citrus.spi.BindToRegistry;
import com.consol.citrus.yaml.YamlTestLoader;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class StartTest extends AbstractYamlActionTest {

    @BindToRegistry
    final Server myServer = Mockito.mock(Server.class);

    @BindToRegistry
    final Server myFooServer = Mockito.mock(Server.class);

    @BindToRegistry
    final Server myBarServer = Mockito.mock(Server.class);

    @Test
    public void shouldLoadStart() {
        YamlTestLoader testLoader = createTestLoader("classpath:com/consol/citrus/yaml/actions/start-test.yaml");

        when(myServer.getName()).thenReturn("myServer");
        when(myFooServer.getName()).thenReturn("myFooServer");
        when(myBarServer.getName()).thenReturn("myBarServer");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "StartTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);

        int actionIndex = 0;

        StartServerAction action = (StartServerAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getClass(), StartServerAction.class);
        Assert.assertEquals(action.getServers().size(), 1L);
        Assert.assertEquals(action.getServers().get(0), myServer);

        action = (StartServerAction) result.getTestAction(actionIndex);
        Assert.assertEquals(action.getServers().size(), 2L);
        Assert.assertEquals(action.getServers().get(0), myFooServer);
        Assert.assertEquals(action.getServers().get(1), myBarServer);

        verify(myServer).start();
        verify(myFooServer).start();
        verify(myBarServer).start();
    }

}
