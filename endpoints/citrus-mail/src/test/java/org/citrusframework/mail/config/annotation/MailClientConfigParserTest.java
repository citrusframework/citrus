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

import java.util.Map;
import java.util.Properties;

import org.citrusframework.TestActor;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectEndpointConfigParser;
import org.citrusframework.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import org.citrusframework.mail.client.MailClient;
import org.citrusframework.mail.message.MailMessageConverter;
import org.citrusframework.mail.model.MailMarshaller;
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
public class MailClientConfigParserTest extends AbstractTestNGUnitTest {

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

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("messageConverter", MailMessageConverter.class)).thenReturn(messageConverter);
        when(referenceResolver.resolve("marshaller", MailMarshaller.class)).thenReturn(marshaller);
        when(referenceResolver.resolve("javaMailProperties", Properties.class)).thenReturn(mailProperties);
        when(referenceResolver.resolve("testActor", TestActor.class)).thenReturn(testActor);
    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
    }

    @Test
    public void testMailClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st mail mailClient
        Assert.assertEquals(mailClient1.getName(), "mailClient1");
        Assert.assertEquals(mailClient1.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(mailClient1.getEndpointConfiguration().getPort(), 25000);
        Assert.assertEquals(mailClient1.getEndpointConfiguration().getJavaMailProperties().getProperty("mail.smtp.host"), "localhost");
        Assert.assertEquals(mailClient1.getEndpointConfiguration().getJavaMailProperties().getProperty("mail.smtp.port"), "25000");
        Assert.assertNull(mailClient1.getActor());

        // 2nd mail mailClient
        Assert.assertEquals(mailClient2.getName(), "mailClient2");
        Assert.assertEquals(mailClient2.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(mailClient2.getEndpointConfiguration().getPort(), 25000);
        Assert.assertEquals(mailClient2.getEndpointConfiguration().getUsername(), "mailus");
        Assert.assertEquals(mailClient2.getEndpointConfiguration().getPassword(), "secret");
        Assert.assertNotNull(mailClient2.getEndpointConfiguration().getAuthenticator());
        Assert.assertEquals(mailClient2.getEndpointConfiguration().getJavaMailProperties().get("mail.smtp.auth"), "true");
        Assert.assertNull(mailClient2.getActor());

        // 3rd mail mailClient
        Assert.assertEquals(mailClient3.getName(), "mailClient3");
        Assert.assertEquals(mailClient3.getEndpointConfiguration().getHost(), "localhost");
        Assert.assertEquals(mailClient3.getEndpointConfiguration().getPort(), 25000);
        Assert.assertNotNull(mailClient3.getActor());
        Assert.assertEquals(mailClient3.getEndpointConfiguration().getMessageConverter(), messageConverter);
        Assert.assertEquals(mailClient3.getEndpointConfiguration().getMarshaller(), marshaller);
        Assert.assertEquals(mailClient3.getEndpointConfiguration().getJavaMailProperties(), mailProperties);
    }

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser> validators = AnnotationConfigParser.lookup();
        Assert.assertEquals(validators.size(), 4L);
        Assert.assertNotNull(validators.get("direct.async"));
        Assert.assertEquals(validators.get("direct.async").getClass(), DirectEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("direct.sync"));
        Assert.assertEquals(validators.get("direct.sync").getClass(), DirectSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("mail.client"));
        Assert.assertEquals(validators.get("mail.client").getClass(), MailClientConfigParser.class);
        Assert.assertNotNull(validators.get("mail.server"));
        Assert.assertEquals(validators.get("mail.server").getClass(), MailServerConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("mail.client").isPresent());
    }
}
