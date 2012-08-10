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
	     
	     Assert.assertEquals(((ReceiveTimeoutAction)builder.getTestCase().getActions().get(0)).getName(), ReceiveTimeoutAction.class.getSimpleName());
	     
	     Assert.assertEquals(((ReceiveTimeoutAction)builder.getTestCase().getActions().get(0)).getMessageReceiver(), messageReceiver);
	     Assert.assertEquals(((ReceiveTimeoutAction)builder.getTestCase().getActions().get(0)).getMessageSelector(),"TestMessageSelectorString"); 
	     Assert.assertEquals(((ReceiveTimeoutAction)builder.getTestCase().getActions().get(0)).getTimeout(), 5000);
	}
}
