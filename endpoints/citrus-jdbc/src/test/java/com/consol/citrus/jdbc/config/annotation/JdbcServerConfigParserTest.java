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

import java.util.Map;

import com.consol.citrus.TestActor;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.config.annotation.AnnotationConfigParser;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.endpoint.direct.annotation.DirectEndpointConfigParser;
import com.consol.citrus.endpoint.direct.annotation.DirectSyncEndpointConfigParser;
import com.consol.citrus.jdbc.server.JdbcServer;
import com.consol.citrus.message.MessageCorrelator;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
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

    @Mock
    private ReferenceResolver referenceResolver;
    @Mock
    private MessageCorrelator messageCorrelator;
    @Mock
    private TestActor testActor;
    @Mock
    private EndpointAdapter endpointAdapter;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(referenceResolver.resolve("replyMessageCorrelator", MessageCorrelator.class))
                .thenReturn(messageCorrelator);

        when(referenceResolver.resolve("testActor", TestActor.class))
                .thenReturn(testActor);

        when(referenceResolver.resolve("endpointAdapter", EndpointAdapter.class))
                .thenReturn(endpointAdapter);


    }

    @BeforeMethod
    public void setMocks() {
        context.setReferenceResolver(referenceResolver);
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

    @Test
    public void testLookupAll() {
        Map<String, AnnotationConfigParser> validators = AnnotationConfigParser.lookup();
        Assert.assertEquals(validators.size(), 3L);
        Assert.assertNotNull(validators.get("direct.async"));
        Assert.assertEquals(validators.get("direct.async").getClass(), DirectEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("direct.sync"));
        Assert.assertEquals(validators.get("direct.sync").getClass(), DirectSyncEndpointConfigParser.class);
        Assert.assertNotNull(validators.get("jdbc.server"));
        Assert.assertEquals(validators.get("jdbc.server").getClass(), JdbcServerConfigParser.class);
    }

    @Test
    public void testLookupByQualifier() {
        Assert.assertTrue(AnnotationConfigParser.lookup("jdbc.server").isPresent());
    }
}
