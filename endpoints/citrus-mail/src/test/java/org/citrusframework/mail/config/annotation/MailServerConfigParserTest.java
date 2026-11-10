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

package org.citrusframework.mail.config.annotation;

import java.util.Properties;

import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.mail.message.MailMessageConverter;
import org.citrusframework.mail.model.MailMarshaller;
import org.citrusframework.mail.server.MailServer;
import org.citrusframework.spi.ReferenceResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class MailServerConfigParserTest extends AbstractUnitTest {

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

    @BeforeEach
    @Override
    public void prepareTest() {
        super.prepareTest();

        lenient().when(referenceResolver.resolve("messageConverter", MailMessageConverter.class)).thenReturn(messageConverter);
        lenient().when(referenceResolver.resolve("marshaller", MailMarshaller.class)).thenReturn(marshaller);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
        lenient().when(referenceResolver.resolve("javaMailProperties", Properties.class)).thenReturn(mailProperties);
        lenient().when(referenceResolver.resolve("endpointAdapter", EndpointAdapter.class)).thenReturn(endpointAdapter);

        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testMailServerParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st mail server
        Assertions.assertEquals("mailServer1", mailServer1.getName());
        Assertions.assertEquals(25, mailServer1.getPort());
        Assertions.assertFalse(mailServer1.isAutoStart());
        Assertions.assertFalse(mailServer1.isSplitMultipart());
        Assertions.assertTrue(mailServer1.isAuthRequired());
        Assertions.assertTrue(mailServer1.isAutoAccept());
        Assertions.assertEquals(DirectEndpointAdapter.class, mailServer1.getEndpointAdapter().getClass());
        Assertions.assertTrue(mailServer1.getJavaMailProperties().isEmpty());
        Assertions.assertTrue(mailServer1.getKnownUsers().isEmpty());

        // 2nd mail server
        Assertions.assertEquals("mailServer2", mailServer2.getName());
        Assertions.assertEquals(25000, mailServer2.getPort());
        Assertions.assertFalse(mailServer2.isAutoStart());
        Assertions.assertFalse(mailServer2.isSplitMultipart());
        Assertions.assertFalse(mailServer2.isAuthRequired());
        Assertions.assertFalse(mailServer2.isAutoAccept());
        Assertions.assertTrue(mailServer2.getJavaMailProperties().isEmpty());
        Assertions.assertTrue(mailServer2.getKnownUsers().isEmpty());

        // 3rd mail server
        Assertions.assertEquals("mailServer3", mailServer3.getName());
        Assertions.assertEquals(25, mailServer3.getPort());
        Assertions.assertFalse(mailServer3.isAutoStart());
        Assertions.assertTrue(mailServer3.isSplitMultipart());
        Assertions.assertTrue(mailServer3.isAuthRequired());
        Assertions.assertTrue(mailServer3.isAutoAccept());
        Assertions.assertEquals(endpointAdapter, mailServer3.getEndpointAdapter());
        Assertions.assertEquals(mailProperties, mailServer3.getJavaMailProperties());
        Assertions.assertEquals(messageConverter, mailServer3.getMessageConverter());
        Assertions.assertEquals(marshaller, mailServer3.getMarshaller());
        Assertions.assertFalse(mailServer3.getJavaMailProperties().isEmpty());
        Assertions.assertFalse(mailServer3.getKnownUsers().isEmpty());
        Assertions.assertEquals(2L, mailServer3.getKnownUsers().size());
        Assertions.assertEquals("foo@example.com:foo-user:secr3t", mailServer3.getKnownUsers().get(0));
        Assertions.assertEquals("bar@example.com:bar-user:secr3t", mailServer3.getKnownUsers().get(1));
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("mail.server").isPresent());
    }
}
