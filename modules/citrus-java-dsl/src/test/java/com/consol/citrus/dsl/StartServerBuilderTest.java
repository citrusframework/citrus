package com.consol.citrus.dsl;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.StartServerAction;
import com.consol.citrus.server.Server;

public class StartServerBuilderTest {
	Server main = EasyMock.createMock(Server.class);
	Server s1 = EasyMock.createMock(Server.class);
	Server s2 = EasyMock.createMock(Server.class);
	Server s3 = EasyMock.createMock(Server.class);

	@Test
	public void testStartServerBuilder(){
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
			@Override
			protected void configure(){
				startServer()
				.server(main)
				.serverList(s1, s2, s3);
			}
		};
		
		builder.configure();
		
		
		Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
		Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), StartServerAction.class);
		
		StartServerAction action = (StartServerAction)builder.getTestCase().getActions().get(0);
		Assert.assertEquals(action.getName(), StartServerAction.class.getSimpleName());
		Assert.assertEquals(action.getServer(), main);
		Assert.assertEquals(action.getServerList().size(), 3);
		Assert.assertEquals(action.getServerList().toString(), "[" + s1.toString() + ", " + s2.toString() + ", " + s3.toString() + "]");
	}
}
