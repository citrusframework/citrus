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

package org.citrusframework.actions.dsl;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.StopServerAction;
import org.citrusframework.server.Server;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.StopServerAction.Builder.stop;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class StopServerTestActionBuilderTest extends UnitTestSupport {
	private final Server testServer = Mockito.mock(Server.class);

	private final Server server1 = Mockito.mock(Server.class);
	private final Server server2 = Mockito.mock(Server.class);
	private final Server server3 = Mockito.mock(Server.class);

	@Test
	public void testStopServerBuilder() {
		reset(testServer, server1, server2, server3);
		when(testServer.getName()).thenReturn("testServer");
		when(server1.getName()).thenReturn("server1");
		when(server2.getName()).thenReturn("server1");
		when(server3.getName()).thenReturn("server1");
		DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
		builder.$(stop(testServer));
		builder.$(stop(server1, server2, server3));

		TestCase test = builder.getTestCase();
		Assert.assertEquals(test.getActionCount(), 2);
		Assert.assertEquals(test.getActions().get(0).getClass(), StopServerAction.class);
		Assert.assertEquals(test.getActions().get(1).getClass(), StopServerAction.class);

		StopServerAction action = (StopServerAction)test.getActions().get(0);
		Assert.assertEquals(action.getName(), "stop-server");
		Assert.assertEquals(action.getServers().size(), 1);
		Assert.assertEquals(action.getServers().get(0), testServer);

		action = (StopServerAction)test.getActions().get(1);
        Assert.assertEquals(action.getName(), "stop-server");
		Assert.assertEquals(action.getServers().size(), 3);
		Assert.assertEquals(action.getServers().toString(), "[" + server1.toString() + ", " + server2.toString() + ", " + server3.toString() + "]");

		verify(testServer).stop();
		verify(server1).stop();
		verify(server2).stop();
		verify(server3).stop();
	}
}
