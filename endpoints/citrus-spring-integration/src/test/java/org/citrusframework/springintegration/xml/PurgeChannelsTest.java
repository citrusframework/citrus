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

package org.citrusframework.springintegration.xml;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.springintegration.actions.PurgeMessageChannelAction;
import org.citrusframework.xml.XmlTestLoader;
import org.citrusframework.xml.actions.XmlTestActionBuilder;
import org.mockito.Mock;
import org.springframework.integration.core.MessageSelector;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PurgeChannelsTest extends AbstractXmlActionTest {

    @Mock
    private DestinationResolver<MessageChannel> channelResolver;
    @Mock
    private MessageChannel messageChannel;

    @Mock
    private MessageSelector messageSelector;

    @Test
    public void shouldLoadActions() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/springintegration/xml/purge-channels.citrus.it.xml");

        context.getReferenceResolver().bind("channelResolver", channelResolver);
        context.getReferenceResolver().bind("testChannel", messageChannel);
        context.getReferenceResolver().bind("testChannel1", messageChannel);
        context.getReferenceResolver().bind("testChannel2", messageChannel);
        context.getReferenceResolver().bind("testChannel3", messageChannel);
        context.getReferenceResolver().bind("messageSelector", messageSelector);

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("PurgeChannelsTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(3L, result.getActionCount());
        Assertions.assertEquals(PurgeMessageChannelAction.class, result.getTestAction(0).getClass());
        Assertions.assertEquals("purge-channel", result.getTestAction(0).getName());

        int actionIndex = 0;

        PurgeMessageChannelAction action = (PurgeMessageChannelAction) result.getTestAction(actionIndex++);
        Assertions.assertNotNull(action.getMessageSelector());
        Assertions.assertNotNull(action.getChannelResolver());
        Assertions.assertEquals(1, action.getChannels().size());
        Assertions.assertEquals(1, action.getChannelNames().size());
        Assertions.assertEquals("testChannel1", action.getChannelNames().get(0));

        action = (PurgeMessageChannelAction) result.getTestAction(actionIndex++);
        Assertions.assertNotNull(action.getMessageSelector());
        Assertions.assertEquals(context.getReferenceResolver().resolve("channelResolver"), action.getChannelResolver());
        Assertions.assertEquals(0, action.getChannels().size());
        Assertions.assertEquals(2, action.getChannelNames().size());
        Assertions.assertEquals("testChannel1", action.getChannelNames().get(0));
        Assertions.assertEquals("testChannel2", action.getChannelNames().get(1));

        action = (PurgeMessageChannelAction) result.getTestAction(actionIndex);
        Assertions.assertEquals(context.getReferenceResolver().resolve("messageSelector"), action.getMessageSelector());
        Assertions.assertEquals(0, action.getChannels().size());
        Assertions.assertEquals(1, action.getChannelNames().size());
        Assertions.assertEquals("testChannel3", action.getChannelNames().get(0));
    }

    @Test
    public void shouldLookupTestActionBuilder() {
        Assertions.assertTrue(XmlTestActionBuilder.lookup("purge-channels").isPresent());
        Assertions.assertEquals(PurgeChannels.class, XmlTestActionBuilder.lookup("purge-channels").get().getClass());
        Assertions.assertTrue(XmlTestActionBuilder.lookup("purgeChannels").isPresent());
        Assertions.assertEquals(PurgeChannels.class, XmlTestActionBuilder.lookup("purgeChannels").get().getClass());
    }
}
