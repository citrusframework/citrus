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

package org.citrusframework.xml.actions;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.StopServerAction;
import org.citrusframework.server.Server;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.xml.XmlTestLoader;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class StopTest extends AbstractXmlActionTest {

    @BindToRegistry
    final Server myServer = Mockito.mock(Server.class);

    @BindToRegistry
    final Server myFooServer = Mockito.mock(Server.class);

    @BindToRegistry
    final Server myBarServer = Mockito.mock(Server.class);

    @Test
    public void shouldLoadStop() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/xml/actions/stop-test.xml");

        when(myServer.getName()).thenReturn("myServer");
        when(myServer.isRunning()).thenReturn(true);
        when(myFooServer.getName()).thenReturn("myFooServer");
        when(myFooServer.isRunning()).thenReturn(true);
        when(myBarServer.getName()).thenReturn("myBarServer");
        when(myBarServer.isRunning()).thenReturn(true);

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "StopTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);

        int actionIndex = 0;

        StopServerAction action = (StopServerAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getClass(), StopServerAction.class);
        Assert.assertEquals(action.getServers().size(), 1L);
        Assert.assertEquals(action.getServers().get(0), myServer);

        action = (StopServerAction) result.getTestAction(actionIndex);
        Assert.assertEquals(action.getServers().size(), 2L);
        Assert.assertEquals(action.getServers().get(0), myFooServer);
        Assert.assertEquals(action.getServers().get(1), myBarServer);

        verify(myServer).stop();
        verify(myFooServer).stop();
        verify(myBarServer).stop();
    }

}
