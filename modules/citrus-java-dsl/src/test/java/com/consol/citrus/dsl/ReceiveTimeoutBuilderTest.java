package com.consol.citrus.dsl;


import org.easymock.EasyMock;
import org.testng.annotations.Test;
import org.testng.Assert;

import com.consol.citrus.actions.ReceiveTimeoutAction;
import com.consol.citrus.message.MessageReceiver;

public class ReceiveTimeoutBuilderTest {
	
	 private MessageReceiver messageReceiver = EasyMock.createMock(MessageReceiver.class);
	 
	@Test
	public void TestReceiveTimeoutBuilder(){
		 TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
	            @Override
	            protected void configure() {
	            	receiveTimeout()
	            		.timeout(5000)
	            		.messageReceiver(messageReceiver)
	            		.messageSelector("TestMessageSelectorString");
	            }
		 };
		 
		 builder.configure();
		 
		 Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
	     Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveTimeoutAction.class);
	     
	     ReceiveTimeoutAction action = (ReceiveTimeoutAction)builder.getTestCase().getActions().get(0);
	     Assert.assertEquals(action.getName(), ReceiveTimeoutAction.class.getSimpleName());
	     Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
	     Assert.assertEquals(action.getMessageSelector(),"TestMessageSelectorString"); 
	     Assert.assertEquals(action.getTimeout(), 5000);
	}
}
