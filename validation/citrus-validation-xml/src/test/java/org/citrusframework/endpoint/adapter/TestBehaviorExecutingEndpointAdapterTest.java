/*
 * Copyright 2006-2023 the original author or authors.
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

package org.citrusframework.endpoint.adapter;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.config.CitrusSpringConfig;
import org.citrusframework.endpoint.adapter.mapping.XPathPayloadMappingKeyExtractor;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.endpoint.direct.DirectSyncEndpoint;
import org.citrusframework.endpoint.direct.DirectSyncEndpointConfiguration;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageQueue;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 1.3.1
 */
@ContextConfiguration(classes = { CitrusSpringConfig.class, TestBehaviorExecutingEndpointAdapterTest.EndpointConfig.class })
public class TestBehaviorExecutingEndpointAdapterTest extends UnitTestSupport {

    @Autowired
    private TestBehaviorExecutingEndpointAdapter endpointAdapter;

    /**
     * Test for handler routing by node content
     */
    @Test
    public void testRouteMessageByElementTextContent() throws Exception {
        XPathPayloadMappingKeyExtractor mappingNameExtractor = new XPathPayloadMappingKeyExtractor();
        mappingNameExtractor.setXpathExpression("//TestBehavior/@name");
        endpointAdapter.setMappingKeyExtractor(mappingNameExtractor);

        Message response = endpointAdapter.handleMessage(
                new DefaultMessage("<TestBehavior name=\"FooTestBehavior\"></TestBehavior>"));

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getPayload(), "<TestBehavior name=\"FooTestBehavior\">OK</TestBehavior>");

        response = endpointAdapter.handleMessage(
                new DefaultMessage("<TestBehavior name=\"BarTestBehavior\"></TestBehavior>"));

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getPayload(), "<TestBehavior name=\"BarTestBehavior\">OK</TestBehavior>");
    }

    /**
     * Test for handler routing without Xpath given (implementation takes the value of first node).
     */
    @Test
    public void testRouteMessageWithDefaultXpath() throws Exception {
        XPathPayloadMappingKeyExtractor mappingNameExtractor = new XPathPayloadMappingKeyExtractor();
        endpointAdapter.setMappingKeyExtractor(mappingNameExtractor);

        Message response = endpointAdapter.handleMessage(
                new DefaultMessage(
                        "<FooBarTestBehavior></FooBarTestBehavior>"));

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getPayload(), "<FooBarTestBehavior>OK</FooBarTestBehavior>");
    }

    /**
     * Test for Xpath which is not found --> shall raise exception
     */
    @Test
    public void testRouteMessageWithBadXpathExpression() throws Exception {
        XPathPayloadMappingKeyExtractor mappingNameExtractor = new XPathPayloadMappingKeyExtractor();
        mappingNameExtractor.setXpathExpression("//I_DO_NOT_EXIST");
        endpointAdapter.setMappingKeyExtractor(mappingNameExtractor);

        try {
            endpointAdapter.handleMessage(new DefaultMessage(
                    "<FooTestDesigner>foo test please</FooTestDesigner>"));
            Assert.fail("Missing exception due to bad XPath expression");
        } catch (CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "No result for XPath expression: '//I_DO_NOT_EXIST'");
        }
    }

    /**
     * Test for correct xpath, but no handler bean is found --> shall raise exc
     */
    @Test
    public void testRouteMessageWithBadHandlerConfiguration() throws Exception {
        XPathPayloadMappingKeyExtractor mappingNameExtractor = new XPathPayloadMappingKeyExtractor();
        mappingNameExtractor.setXpathExpression("//TestBehavior/@name");
        endpointAdapter.setMappingKeyExtractor(mappingNameExtractor);

        try {
            endpointAdapter.handleMessage(new DefaultMessage(
                    "<TestBehavior name=\"UNKNOWN_TEST\"></TestBehavior>"));
            Assert.fail("Missing exception due to unknown endpoint adapter");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof NoSuchBeanDefinitionException);
        }
    }

    @Configuration
    @ComponentScan({"org.citrusframework.endpoint.adapter.behavior"})
    public static class EndpointConfig {

        private MessageQueue inboundQueue = new DefaultMessageQueue("inboundQueue");

        @Bean
        public TestBehaviorExecutingEndpointAdapter testSimulator() {
            TestBehaviorExecutingEndpointAdapter endpointAdapter = new TestBehaviorExecutingEndpointAdapter();
            XPathPayloadMappingKeyExtractor mappingKeyExtractor = new XPathPayloadMappingKeyExtractor();
            mappingKeyExtractor.setXpathExpression("//Test/@name");
            endpointAdapter.setMappingKeyExtractor(mappingKeyExtractor);

            endpointAdapter.setResponseEndpointAdapter(directEndpointAdapter());

            return endpointAdapter;
        }

        @Bean
        public DirectEndpointAdapter directEndpointAdapter() {
            DirectSyncEndpointConfiguration endpointConfiguration = new DirectSyncEndpointConfiguration();
            endpointConfiguration.setQueue(inboundQueue());
            endpointConfiguration.setTimeout(5000L);
            return new DirectEndpointAdapter(endpointConfiguration);
        }

        @Bean
        public MessageQueue inboundQueue() {
            return inboundQueue;
        }

        @Bean
        public DirectSyncEndpoint inboundDirectEndpoint() {
            DirectSyncEndpointConfiguration endpointConfiguration = new DirectSyncEndpointConfiguration();
            endpointConfiguration.setQueue(inboundQueue());
            endpointConfiguration.setTimeout(5000L);
            return new DirectSyncEndpoint(endpointConfiguration);
        }
    }
}
