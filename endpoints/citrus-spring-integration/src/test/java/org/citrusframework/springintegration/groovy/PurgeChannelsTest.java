package org.citrusframework.springintegration.groovy;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.PurgeMessageChannelAction;
import org.citrusframework.groovy.GroovyTestLoader;
import org.mockito.Mock;
import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PurgeChannelsTest extends AbstractGroovyActionDslTest {

    @Mock
    private DestinationResolver<MessageChannel> channelResolver;
    @Mock
    private MessageChannel messageChannel;

    @Mock
    private MessageSelector messageSelector;

    @Test
    public void shouldLoadActions() throws Exception {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/springintegration/groovy/purge-channels.test.groovy");

        context.getReferenceResolver().bind("channelResolver", channelResolver);
        context.getReferenceResolver().bind("testChannel", messageChannel);
        context.getReferenceResolver().bind("testChannel1", messageChannel);
        context.getReferenceResolver().bind("testChannel2", messageChannel);
        context.getReferenceResolver().bind("testChannel3", messageChannel);
        context.getReferenceResolver().bind("messageSelector", messageSelector);

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "PurgeChannelsTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 3L);
        Assert.assertEquals(result.getTestAction(0).getClass(), PurgeMessageChannelAction.class);
        Assert.assertEquals(result.getTestAction(0).getName(), "purge-channel");

        int actionIndex = 0;

        PurgeMessageChannelAction action = (PurgeMessageChannelAction) result.getTestAction(actionIndex++);
        Assert.assertNotNull(action.getMessageSelector());
        Assert.assertNull(action.getChannelResolver());
        Assert.assertEquals(action.getChannels().size(), 1);
        Assert.assertEquals(action.getChannels().get(0), messageChannel);
        Assert.assertEquals(action.getChannelNames().size(), 0);

        action = (PurgeMessageChannelAction) result.getTestAction(actionIndex++);
        Assert.assertNotNull(action.getMessageSelector());
        Assert.assertEquals(action.getChannelResolver(), context.getReferenceResolver().resolve("channelResolver"));
        Assert.assertEquals(action.getChannels().size(), 0);
        Assert.assertEquals(action.getChannelNames().size(), 2);
        Assert.assertEquals(action.getChannelNames().get(0), "testChannel1");
        Assert.assertEquals(action.getChannelNames().get(1), "testChannel2");

        action = (PurgeMessageChannelAction) result.getTestAction(actionIndex);
        Assert.assertEquals(action.getMessageSelector(), context.getReferenceResolver().resolve("messageSelector"));
        Assert.assertEquals(action.getChannels().size(), 0);
        Assert.assertEquals(action.getChannelNames().size(), 1);
        Assert.assertEquals(action.getChannelNames().get(0), "testChannel3");
    }
}
