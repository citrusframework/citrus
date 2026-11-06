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

package org.citrusframework.groovy.dsl;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.StartServerAction;
import org.citrusframework.groovy.GroovyTestLoader;
import org.citrusframework.server.Server;
import org.citrusframework.spi.BindToRegistry;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StartTest extends AbstractGroovyActionDslTest {

    @BindToRegistry
    final Server myServer = Mockito.mock(Server.class);

    @BindToRegistry
    final Server myFooServer = Mockito.mock(Server.class);

    @BindToRegistry
    final Server myBarServer = Mockito.mock(Server.class);

    @Test
    public void shouldLoadStart() {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/groovy/dsl/start.citrus.test.groovy");

        when(myServer.getName()).thenReturn("myServer");
        when(myFooServer.getName()).thenReturn("myFooServer");
        when(myBarServer.getName()).thenReturn("myBarServer");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("StartTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(3L, result.getActionCount());

        int actionIndex = 0;

        StartServerAction action = (StartServerAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(StartServerAction.class, action.getClass());
        Assertions.assertEquals(1L, action.getServers().size());
        Assertions.assertEquals(myServer, action.getServers().get(0));

        action = (StartServerAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(StartServerAction.class, action.getClass());
        Assertions.assertEquals(1L, action.getServers().size());
        Assertions.assertEquals(myServer, action.getServers().get(0));

        action = (StartServerAction) result.getTestAction(actionIndex);
        Assertions.assertEquals(2L, action.getServers().size());
        Assertions.assertEquals(myFooServer, action.getServers().get(0));
        Assertions.assertEquals(myBarServer, action.getServers().get(1));

        verify(myServer, times(2)).start();
        verify(myFooServer).start();
        verify(myBarServer).start();
    }
}
