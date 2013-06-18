/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl.definition;

import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.StopServerAction;
import com.consol.citrus.server.Server;

public class StopServerDefinitionTest extends AbstractTestNGUnitTest {
	private Server testServer = EasyMock.createMock(Server.class);
	
	private Server server1 = EasyMock.createMock(Server.class);
	private Server server2 = EasyMock.createMock(Server.class);
	private Server server3 = EasyMock.createMock(Server.class);

	@Test
	public void testStopServerBuilder() {
		MockBuilder builder = new MockBuilder(applicationContext) {
			@Override
			public void configure() {
				stop(testServer);
				stop(server1, server2, server3);
			}
		};
		
		builder.run(null, null);
		
		
		Assert.assertEquals(builder.testCase().getActions().size(), 2);
		Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), StopServerAction.class);
		Assert.assertEquals(builder.testCase().getActions().get(1).getClass(), StopServerAction.class);
		
		StopServerAction action = (StopServerAction)builder.testCase().getActions().get(0);
		Assert.assertEquals(action.getName(), StopServerAction.class.getSimpleName());
		Assert.assertEquals(action.getServer(), testServer);
		
		action = (StopServerAction)builder.testCase().getActions().get(1);
        Assert.assertEquals(action.getName(), StopServerAction.class.getSimpleName());
		Assert.assertEquals(action.getServerList().size(), 3);
		Assert.assertEquals(action.getServerList().toString(), "[" + server1.toString() + ", " + server2.toString() + ", " + server3.toString() + "]");
	}
}
