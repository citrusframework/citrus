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

package com.consol.citrus.dsl.runner;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.StopServerAction;
import com.consol.citrus.server.Server;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class StopServerTestRunnerTest extends AbstractTestNGUnitTest {
	private Server testServer = EasyMock.createMock(Server.class);
	
	private Server server1 = EasyMock.createMock(Server.class);
	private Server server2 = EasyMock.createMock(Server.class);
	private Server server3 = EasyMock.createMock(Server.class);

	@Test
	public void testStopServerBuilder() {
		reset(testServer, server1, server2, server3);
		testServer.stop();
		expectLastCall().once();
		expect(testServer.getName()).andReturn("testServer").once();
		server1.stop();
		expectLastCall().once();
		expect(server1.getName()).andReturn("server1").once();
		server2.stop();
		expectLastCall().once();
		expect(server2.getName()).andReturn("server1").once();
		server3.stop();
		expectLastCall().once();
		expect(server3.getName()).andReturn("server1").once();
		replay(testServer, server1, server2, server3);

		MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
			@Override
			public void execute() {
				stop(testServer);
				stop(server1, server2, server3);
			}
		};

		TestCase test = builder.getTestCase();
		Assert.assertEquals(test.getActionCount(), 2);
		Assert.assertEquals(test.getActions().get(0).getClass(), StopServerAction.class);
		Assert.assertEquals(test.getActions().get(1).getClass(), StopServerAction.class);
		
		StopServerAction action = (StopServerAction)test.getActions().get(0);
		Assert.assertEquals(action.getName(), "stop-server");
		Assert.assertEquals(action.getServer(), testServer);
		
		action = (StopServerAction)test.getActions().get(1);
        Assert.assertEquals(action.getName(), "stop-server");
		Assert.assertEquals(action.getServerList().size(), 3);
		Assert.assertEquals(action.getServerList().toString(), "[" + server1.toString() + ", " + server2.toString() + ", " + server3.toString() + "]");

		verify(testServer, server1, server2, server3);
	}
}
