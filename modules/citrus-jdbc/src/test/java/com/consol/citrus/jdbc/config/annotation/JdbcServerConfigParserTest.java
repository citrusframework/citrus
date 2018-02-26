/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.jdbc.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.jdbc.server.JdbcServer;
import com.consol.citrus.message.MessageCorrelator;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class JdbcServerConfigParserTest extends AbstractTestNGUnitTest {

    @CitrusEndpoint
    @JdbcServerConfig(
            host = "foo.bar.test.io",
            port = 8042,
            actor = "testActor",
            autoConnect = false,
            autoCreateStatement = false,
            autoStart = true,
            correlator = "replyMessageCorrelator",
            databaseName = "foobar",
            debugLogging = true,
            endpointAdapter = "endpointAdapter",
            maxConnections = 50,
            pollingInterval = 0,
            timeout = 10L,
            autoTransactionHandling = false
    )
    private JdbcServer testServer;

    @Autowired
    private SpringBeanReferenceResolver referenceResolver;

    @Mock
    private MessageCorrelator messageCorrelator = Mockito.mock(MessageCorrelator.class);
    @Mock
    private TestActor testActor = Mockito.mock(TestActor.class);
    @Mock
    private EndpointAdapter endpointAdapter = Mockito.mock(EndpointAdapter.class);
    @Mock
    private ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);

        referenceResolver.setApplicationContext(applicationContext);

        when(applicationContext.getBean("replyMessageCorrelator", MessageCorrelator.class))
                .thenReturn(messageCorrelator);

        when(applicationContext.getBean("testActor", TestActor.class))
                .thenReturn(testActor);

        when(applicationContext.getBean("endpointAdapter", EndpointAdapter.class))
                .thenReturn(endpointAdapter);


    }

    @AfterClass
    public void teardown(){
        testServer.stop();
    }

    @Test
    public void testAnnotations(){
        CitrusAnnotations.injectEndpoints(this, context);


        assertEquals(
                testServer.getEndpointConfiguration().getServerConfiguration().getHost(),
                "foo.bar.test.io");
        assertEquals(
                testServer.getEndpointConfiguration().getServerConfiguration().getPort(),
                8042);
        assertEquals(
                testServer.getActor(),
                testActor);
        assertEquals(
                testServer.getEndpointConfiguration().isAutoConnect(),
                false);
        assertEquals(
                testServer.getEndpointConfiguration().isAutoCreateStatement(),
                false);
        assertEquals(
                testServer.isAutoStart(),
                true);
        assertEquals(
                testServer.getEndpointConfiguration().getCorrelator(),
                messageCorrelator);
        assertEquals(
                testServer.getEndpointConfiguration().getServerConfiguration().getDatabaseName(),
                "foobar");
        assertEquals(
                testServer.isDebugLogging(),
                true);
        assertEquals(
                testServer.getEndpointAdapter(),
                endpointAdapter);
        assertEquals(
                testServer.getEndpointConfiguration().getServerConfiguration().getMaxConnections(),
                50);
        assertEquals(
                testServer.getEndpointConfiguration().getPollingInterval(),
                0);
        assertEquals(
                testServer.getEndpointConfiguration().getTimeout(),
                10L);
        assertEquals(
                testServer.getEndpointConfiguration().isAutoTransactionHandling(),
                false);
    }
}