package com.consol.citrus.dsl;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.StopServerAction;
import com.consol.citrus.server.Server;

public class StopServerBuilderTest {
	Server main = EasyMock.createMock(Server.class);
	Server s1 = EasyMock.createMock(Server.class);
	Server s2 = EasyMock.createMock(Server.class);
	Server s3 = EasyMock.createMock(Server.class);

	@Test
	public void testStopServerBuilder(){
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
			@Override
			protected void configure(){
				stopServer()
				.server(main)
				.serverList(s1, s2, s3);
			}
		};
		
		builder.configure();
		
		
		Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
		Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), StopServerAction.class);
		
		StopServerAction action = (StopServerAction)builder.getTestCase().getActions().get(0);
		Assert.assertEquals(action.getName(), StopServerAction.class.getSimpleName());
		Assert.assertEquals(action.getServer(), main);
		Assert.assertEquals(action.getServerList().size(), 3);
		Assert.assertEquals(action.getServerList().toString(), "[" + s1.toString() + ", " + s2.toString() + ", " + s3.toString() + "]");
	}
}
