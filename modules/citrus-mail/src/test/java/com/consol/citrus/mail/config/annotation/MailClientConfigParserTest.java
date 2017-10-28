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
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.mail.client.MailClient;
import com.consol.citrus.mail.message.MailMessageConverter;
import com.consol.citrus.mail.model.MailMarshaller;
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

    @Autowired
    private SpringBeanReferenceResolver referenceResolver;

    @Mock
    private MailMessageConverter messageConverter = Mockito.mock(MailMessageConverter.class);
    @Mock
    private MailMarshaller marshaller = Mockito.mock(MailMarshaller.class);
    @Mock
    private Properties mailProperties = Mockito.mock(Properties.class);
    @Mock
    private TestActor testActor = Mockito.mock(TestActor.class);
    @Mock
    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);

        referenceResolver.setApplicationContext(applicationContext);

        when(applicationContext.getBean("messageConverter", MailMessageConverter.class)).thenReturn(messageConverter);
        when(applicationContext.getBean("marshaller", MailMarshaller.class)).thenReturn(marshaller);
        when(applicationContext.getBean("javaMailProperties", Properties.class)).thenReturn(mailProperties);
        when(applicationContext.getBean("testActor", TestActor.class)).thenReturn(testActor);
    }

    @Test
    public void testMailClientParser() {
        CitrusAnnotations.injectEndpoints(this, context);

        // 1st mail mailClient
        Assert.assertEquals(mailClient1.getName(), "mailClient1");
        Assert.assertEquals(mailClient1.getEndpointConfiguration().getJavaMailSender().getHost(), "localhost");
        Assert.assertEquals(mailClient1.getEndpointConfiguration().getJavaMailSender().getPort(), 25000);
        Assert.assertNull(mailClient1.getActor());

        // 2nd mail mailClient
        Assert.assertEquals(mailClient2.getName(), "mailClient2");
        Assert.assertEquals(mailClient2.getEndpointConfiguration().getJavaMailSender().getHost(), "localhost");
        Assert.assertEquals(mailClient2.getEndpointConfiguration().getJavaMailSender().getPort(), 25000);
        Assert.assertEquals(mailClient2.getEndpointConfiguration().getJavaMailSender().getUsername(), "mailus");
        Assert.assertEquals(mailClient2.getEndpointConfiguration().getJavaMailSender().getPassword(), "secret");
        Assert.assertEquals(mailClient2.getEndpointConfiguration().getJavaMailSender().getJavaMailProperties().get("mail.smtp.auth"), "true");
        Assert.assertNull(mailClient2.getActor());

        // 3rd mail mailClient
        Assert.assertEquals(mailClient3.getName(), "mailClient3");
        Assert.assertEquals(mailClient3.getEndpointConfiguration().getJavaMailSender().getHost(), "localhost");
        Assert.assertEquals(mailClient3.getEndpointConfiguration().getJavaMailSender().getPort(), 25000);
        Assert.assertNotNull(mailClient3.getActor());
        Assert.assertEquals(mailClient3.getEndpointConfiguration().getMessageConverter(), messageConverter);
        Assert.assertEquals(mailClient3.getEndpointConfiguration().getMarshaller(), marshaller);
        Assert.assertEquals(mailClient3.getEndpointConfiguration().getJavaMailSender().getJavaMailProperties(), mailProperties);
    }
}
