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

package com.consol.citrus.jdbc.config.xml;

import com.consol.citrus.TestActor;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.jdbc.server.JdbcServer;
import com.consol.citrus.message.MessageCorrelator;
import com.consol.citrus.testng.AbstractBeanDefinitionParserTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;

public class JdbcEndpointConfigurationParserTest extends AbstractBeanDefinitionParserTest {

    private JdbcServer testServer;

    @BeforeClass
    public void setUp(){
        Map<String, JdbcServer> servers = beanDefinitionContext.getBeansOfType(JdbcServer.class);

        testServer = servers.get("testServer");
    }

    @AfterClass
    public void teardown(){
        testServer.stop();
    }

    @Test
    public void testAnnotations(){
        assertEquals(
                testServer.getEndpointConfiguration().getServerConfiguration().getHost(),
                "foo.bar.test.io");
        assertEquals(
                testServer.getEndpointConfiguration().getServerConfiguration().getPort(),
                8043);
        assertEquals(
                testServer.getActor(),
                beanDefinitionContext.getBean("testActor", TestActor.class));
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
                beanDefinitionContext.getBean("replyMessageCorrelator", MessageCorrelator.class));
        assertEquals(
                testServer.getEndpointConfiguration().getServerConfiguration().getDatabaseName(),
                "foobar");
        assertEquals(
                testServer.isDebugLogging(),
                true);
        assertEquals(
                testServer.getEndpointAdapter(),
                beanDefinitionContext.getBean("endpointAdapter", EndpointAdapter.class));
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
        assertEquals(
                testServer.getEndpointConfiguration().getAutoHandleQueries().length,
                2);
    }
}
