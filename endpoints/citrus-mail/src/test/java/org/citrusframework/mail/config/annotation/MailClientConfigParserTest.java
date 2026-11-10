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

import java.util.Map;
import java.util.Properties;

import org.citrusframework.TestActor;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.mail.client.MailClient;
import org.citrusframework.mail.message.MailMessageConverter;
import org.citrusframework.mail.model.MailMarshaller;
import org.citrusframework.spi.ReferenceResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class MailClientConfigParserTest extends AbstractUnitTest {

    @CitrusEndpoint(name = "mailClient1")
    @MailClientConfig(host="localhost",
            port = 25000)
    private MailClient mailClient1;

    @CitrusEndpoint
    @MailClientConfig(host="localhost",
            port = 25000,
            username="mailus",
            password="secret")
    private MailClient mailClient2;

    @CitrusEndpoint
    @MailClientConfig(host="localhost",
            port = 25000,
            actor="testActor",
            messageConverter="messageConverter",
            marshaller="marshaller",
            javaMailProperties="javaMailProperties")
    private MailClient mailClient3;

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private MailMessageConverter messageConverter;
    @Mock
    private MailMarshaller marshaller;
    @Mock
    private Properties mailProperties;
    @Mock
    private TestActor testActor;

    @BeforeEach
    @Override
    public void prepareTest() {
        super.prepareTest();

        lenient().when(referenceResolver.resolve("messageConverter", MailMessageConverter.class)).thenReturn(messageConverter);
        lenient().when(referenceResolver.resolve("marshaller", MailMarshaller.class)).thenReturn(marshaller);
        lenient().when(referenceResolver.resolve("javaMailProperties", Properties.class)).thenReturn(mailProperties);
        lenient().when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);

        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testMailClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st mail mailClient
        Assertions.assertEquals("mailClient1", mailClient1.getName());
        Assertions.assertEquals("localhost", mailClient1.getEndpointConfiguration().getHost());
        Assertions.assertEquals(25000, mailClient1.getEndpointConfiguration().getPort());
        Assertions.assertEquals("localhost", mailClient1.getEndpointConfiguration().getJavaMailProperties().getProperty("mail.smtp.host"));
        Assertions.assertEquals("25000", mailClient1.getEndpointConfiguration().getJavaMailProperties().getProperty("mail.smtp.port"));
        Assertions.assertNull(mailClient1.getActor());

        // 2nd mail mailClient
        Assertions.assertEquals("mailClient2", mailClient2.getName());
        Assertions.assertEquals("localhost", mailClient2.getEndpointConfiguration().getHost());
        Assertions.assertEquals(25000, mailClient2.getEndpointConfiguration().getPort());
        Assertions.assertEquals("mailus", mailClient2.getEndpointConfiguration().getUsername());
        Assertions.assertEquals("secret", mailClient2.getEndpointConfiguration().getPassword());
        Assertions.assertNotNull(mailClient2.getEndpointConfiguration().getAuthenticator());
        Assertions.assertEquals("true", mailClient2.getEndpointConfiguration().getJavaMailProperties().get("mail.smtp.auth"));
        Assertions.assertNull(mailClient2.getActor());

        // 3rd mail mailClient
        Assertions.assertEquals("mailClient3", mailClient3.getName());
        Assertions.assertEquals("localhost", mailClient3.getEndpointConfiguration().getHost());
        Assertions.assertEquals(25000, mailClient3.getEndpointConfiguration().getPort());
        Assertions.assertNotNull(mailClient3.getActor());
        Assertions.assertEquals(messageConverter, mailClient3.getEndpointConfiguration().getMessageConverter());
        Assertions.assertEquals(marshaller, mailClient3.getEndpointConfiguration().getMarshaller());
        Assertions.assertEquals(mailProperties, mailClient3.getEndpointConfiguration().getJavaMailProperties());
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser<?, ?>> validators = AnnotationConfigParser.lookup();
        Assertions.assertEquals(4L, validators.size());
        Assertions.assertNotNull(validators.get("direct.async"));
        Assertions.assertEquals(DirectEndpointConfigParser.class, validators.get("direct.async").getClass());
        Assertions.assertNotNull(validators.get("direct.sync"));
        Assertions.assertEquals(DirectSyncEndpointConfigParser.class, validators.get("direct.sync").getClass());
        Assertions.assertNotNull(validators.get("mail.client"));
        Assertions.assertEquals(MailClientConfigParser.class, validators.get("mail.client").getClass());
        Assertions.assertNotNull(validators.get("mail.server"));
        Assertions.assertEquals(MailServerConfigParser.class, validators.get("mail.server").getClass());
    }

    @Test
    public void testLookupByQualifier() {
        Assertions.assertTrue(AnnotationConfigParser.lookup("mail.client").isPresent());
    }
}
