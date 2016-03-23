/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.mail.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.channel.ChannelEndpointAdapter;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.mail.message.MailMessageConverter;
import com.consol.citrus.mail.server.MailServer;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class MailServerConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint(name = "mailServer1")
    @MailServerConfig()
    private MailServer mailServer1;

    @CitrusEndpoint
    @MailServerConfig(autoStart=false,
            autoAccept=false,
            port=25000)
    private MailServer mailServer2;

    @CitrusEndpoint
    @MailServerConfig(autoStart=false,
            splitMultipart=true,
            messageConverter="messageConverter",
            javaMailProperties="javaMailProperties",
            endpointAdapter="endpointAdapter")
    private MailServer mailServer3;

    @Autowired
    private SpringBeanReferenceResolver referenceResolver;

    @Mock
    private MailMessageConverter messageConverter = Mockito.mock(MailMessageConverter.class);
    @Mock
    private Properties mailProperties = Mockito.mock(Properties.class);
    @Mock
    private EndpointAdapter endpointAdapter = Mockito.mock(EndpointAdapter.class);
    @Mock
    private TestActor testActor = Mockito.mock(TestActor.class);
    @Mock
    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);

        referenceResolver.setApplicationContext(applicationContext);

        when(applicationContext.getBean("messageConverter", MailMessageConverter.class)).thenReturn(messageConverter);
        when(applicationContext.getBean("testActor", TestActor.class)).thenReturn(testActor);
        when(applicationContext.getBean("javaMailProperties", Properties.class)).thenReturn(mailProperties);
        when(applicationContext.getBean("endpointAdapter", EndpointAdapter.class)).thenReturn(endpointAdapter);
    }

    @Test
    public void testHttpServerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st mail server
        Assert.assertEquals(mailServer1.getName(), "mailServer1");
        Assert.assertEquals(mailServer1.getPort(), 25);
        Assert.assertFalse(mailServer1.isAutoStart());
        Assert.assertFalse(mailServer1.isSplitMultipart());
        Assert.assertTrue(mailServer1.isAutoAccept());
        Assert.assertEquals(mailServer1.getEndpointAdapter().getClass(), ChannelEndpointAdapter.class);
        Assert.assertTrue(mailServer1.getJavaMailProperties().isEmpty());

        // 2nd mail server
        Assert.assertEquals(mailServer2.getName(), "mailServer2");
        Assert.assertEquals(mailServer2.getPort(), 25000);
        Assert.assertFalse(mailServer2.isAutoStart());
        Assert.assertFalse(mailServer2.isSplitMultipart());
        Assert.assertFalse(mailServer2.isAutoAccept());
        Assert.assertTrue(mailServer2.getJavaMailProperties().isEmpty());

        // 3rd mail server
        Assert.assertEquals(mailServer3.getName(), "mailServer3");
        Assert.assertEquals(mailServer3.getPort(), 25);
        Assert.assertFalse(mailServer3.isAutoStart());
        Assert.assertTrue(mailServer3.isSplitMultipart());
        Assert.assertTrue(mailServer3.isAutoAccept());
        Assert.assertEquals(mailServer3.getEndpointAdapter(), endpointAdapter);
        Assert.assertEquals(mailServer3.getJavaMailProperties(), mailProperties);
        Assert.assertEquals(mailServer3.getMessageConverter(), messageConverter);
        Assert.assertFalse(mailServer3.getJavaMailProperties().isEmpty());
    }
}
