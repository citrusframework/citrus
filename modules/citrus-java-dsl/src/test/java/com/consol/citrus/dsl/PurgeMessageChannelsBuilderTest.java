package com.consol.citrus.dsl;

import org.easymock.EasyMock;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.core.MessageSelector;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.PurgeMessageChannelAction;

public class PurgeMessageChannelsBuilderTest {
	BeanFactory beanFactory = EasyMock.createMock(BeanFactory.class);
	MessageSelector messageSelector = EasyMock
	        .createMock(MessageSelector.class);
	MessageChannel ch1 = EasyMock.createMock(MessageChannel.class);
	MessageChannel ch2 = EasyMock.createMock(MessageChannel.class);

	@Test
	public void testPurgeJMSQueuesBuilder() {
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
			@Override
			protected void configure() {
				purgeMessageChannels(beanFactory)
				        .channelNames("ch1", "ch2", "ch3").channels(ch1, ch2)
				        .messageSelector(messageSelector);
			}
		};

		builder.configure();

		Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
		Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), PurgeMessageChannelAction.class);

		PurgeMessageChannelAction action = (PurgeMessageChannelAction) builder.getTestCase().getActions().get(0);
		Assert.assertEquals(action.getChannelNames().size(), 3);
		Assert.assertEquals(action.getChannelNames().toString(), "[ch1, ch2, ch3]");
		Assert.assertEquals(action.getChannels().size(), 2);
		Assert.assertEquals(action.getChannels().toString(), "[" + ch1.toString() + ", " + ch2.toString() + "]");
		Assert.assertEquals(action.getMessageSelector(), messageSelector);
	}
}
