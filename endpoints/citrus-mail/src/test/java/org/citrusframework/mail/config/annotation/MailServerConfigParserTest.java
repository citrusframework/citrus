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

package org.citrusframework.mail.config.annotation;

import java.util.Properties;

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.mail.message.MailMessageConverter;
import org.citrusframework.mail.model.MailMarshaller;
import org.citrusframework.mail.server.MailServer;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
            authRequired = false,
            autoAccept=false,
            port=25000)
    private MailServer mailServer2;

    @CitrusEndpoint
    @MailServerConfig(autoStart=false,
            splitMultipart=true,
            messageConverter="messageConverter",
            knownUsers= { "foo@example.com:foo-user:secr3t", "bar@example.com:bar-user:secr3t" },
            marshaller="marshaller",
            javaMailProperties="javaMailProperties",
            endpointAdapter="endpointAdapter")
    private MailServer mailServer3;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private MailMessageConverter messageConverter;
    @Mock
    private MailMarshaller marshaller;
    @Mock
    private Properties mailProperties;
    @Mock
    private EndpointAdapter endpointAdapter;
    @Mock
    private TestActor testActor;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("messageConverter", MailMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("marshaller", MailMarshaller.class)).thenReturn(marshaller);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
        when(referenceResolver.resolve("javaMailProperties", Properties.class)).thenReturn(mailProperties);
        when(referenceResolver.resolve("endpointAdapter", EndpointAdapter.class)).thenReturn(endpointAdapter);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testMailServerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st mail server
        Assert.assertEquals(mailServer1.getName(), "mailServer1");
        Assert.assertEquals(mailServer1.getPort(), 25);
        Assert.assertFalse(mailServer1.isAutoStart());
        Assert.assertFalse(mailServer1.isSplitMultipart());
        Assert.assertTrue(mailServer1.isAuthRequired());
        Assert.assertTrue(mailServer1.isAutoAccept());
        Assert.assertEquals(mailServer1.getEndpointAdapter().getClass(), DirectEndpointAdapter.class);
        Assert.assertTrue(mailServer1.getJavaMailProperties().isEmpty());
        Assert.assertTrue(mailServer1.getKnownUsers().isEmpty());

        // 2nd mail server
        Assert.assertEquals(mailServer2.getName(), "mailServer2");
        Assert.assertEquals(mailServer2.getPort(), 25000);
        Assert.assertFalse(mailServer2.isAutoStart());
        Assert.assertFalse(mailServer2.isSplitMultipart());
        Assert.assertFalse(mailServer2.isAuthRequired());
        Assert.assertFalse(mailServer2.isAutoAccept());
        Assert.assertTrue(mailServer2.getJavaMailProperties().isEmpty());
        Assert.assertTrue(mailServer2.getKnownUsers().isEmpty());

        // 3rd mail server
        Assert.assertEquals(mailServer3.getName(), "mailServer3");
        Assert.assertEquals(mailServer3.getPort(), 25);
        Assert.assertFalse(mailServer3.isAutoStart());
        Assert.assertTrue(mailServer3.isSplitMultipart());
        Assert.assertTrue(mailServer3.isAuthRequired());
        Assert.assertTrue(mailServer3.isAutoAccept());
        Assert.assertEquals(mailServer3.getEndpointAdapter(), endpointAdapter);
        Assert.assertEquals(mailServer3.getJavaMailProperties(), mailProperties);
        Assert.assertEquals(mailServer3.getMessageConverter(), messageConverter);
        Assert.assertEquals(mailServer3.getMarshaller(), marshaller);
        Assert.assertFalse(mailServer3.getJavaMailProperties().isEmpty());
        Assert.assertFalse(mailServer3.getKnownUsers().isEmpty());
        Assert.assertEquals(mailServer3.getKnownUsers().size(), 2L);
        Assert.assertEquals(mailServer3.getKnownUsers().get(0), "foo@example.com:foo-user:secr3t");
        Assert.assertEquals(mailServer3.getKnownUsers().get(1), "bar@example.com:bar-user:secr3t");
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("mail.server").isPresent());
    }
}
