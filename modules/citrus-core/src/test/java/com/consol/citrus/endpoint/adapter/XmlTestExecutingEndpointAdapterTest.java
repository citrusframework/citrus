/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.endpoint.adapter;

import com.consol.citrus.channel.ChannelEndpointAdapter;
import com.consol.citrus.channel.ChannelSyncEndpoint;
import com.consol.citrus.channel.ChannelSyncEndpointConfiguration;
import com.consol.citrus.channel.MessageSelectingQueueChannel;
import com.consol.citrus.endpoint.adapter.mapping.XPathPayloadMappingKeyExtractor;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
@ContextConfiguration(classes = XmlTestExecutingEndpointAdapterTest.EndpointConfig.class)
public class XmlTestExecutingEndpointAdapterTest extends AbstractTestNGUnitTest {

    @Autowired
    private XmlTestExecutingEndpointAdapter endpointAdapter;

    /**
     * Test for handler routing by node content
     */
    @Test
    public void testRouteMessageByElementTextContent() throws Exception {
        XPathPayloadMappingKeyExtractor mappingNameExtractor = new XPathPayloadMappingKeyExtractor();
        mappingNameExtractor.setXpathExpression("//Test/@name");
        endpointAdapter.setMappingKeyExtractor(mappingNameExtractor);

        Message response = endpointAdapter.handleMessage(
                new DefaultMessage("<Test name=\"FooTest\"></Test>"));

        Assert.assertEquals(response.getPayload(String.class).trim(), "<Test name=\"FooTest\">OK</Test>");

        response = endpointAdapter.handleMessage(
                new DefaultMessage("<Test name=\"BarTest\"></Test>"));

        Assert.assertEquals(response.getPayload(String.class).trim(), "<Test name=\"BarTest\">OK</Test>");
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
                        "<FooBarTest></FooBarTest>"));

        Assert.assertEquals(response.getPayload(String.class).trim(), "<FooBarTest>OK</FooBarTest>");
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
                    "<FooTest>foo test please</FooTest>"));
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
        mappingNameExtractor.setXpathExpression("//Test/@name");
        endpointAdapter.setMappingKeyExtractor(mappingNameExtractor);

        try {
            endpointAdapter.handleMessage(new DefaultMessage(
                    "<Test name=\"UNKNOWN_TEST\"></Test>"));
            Assert.fail("Missing exception due to unknown endpoint adapter");
        } catch (CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Failed to load test case");
        }
    }

    @Configuration
    public static class EndpointConfig {

        private MessageSelectingQueueChannel inboundChannel = new MessageSelectingQueueChannel();

        @Bean
        public XmlTestExecutingEndpointAdapter testSimulator() {
            XmlTestExecutingEndpointAdapter endpointAdapter = new XmlTestExecutingEndpointAdapter();
            XPathPayloadMappingKeyExtractor mappingKeyExtractor = new XPathPayloadMappingKeyExtractor();
            mappingKeyExtractor.setXpathExpression("//Test/@name");
            endpointAdapter.setMappingKeyExtractor(mappingKeyExtractor);

            endpointAdapter.setResponseEndpointAdapter(channelEndpointAdapter());

            return endpointAdapter;
        }

        @Bean
        public ChannelEndpointAdapter channelEndpointAdapter() {
            ChannelSyncEndpointConfiguration endpointConfiguration = new ChannelSyncEndpointConfiguration();
            endpointConfiguration.setChannel(inboundChannel());
            endpointConfiguration.setTimeout(5000L);
            return new ChannelEndpointAdapter(endpointConfiguration);
        }

        @Bean
        public MessageSelectingQueueChannel inboundChannel() {
            return inboundChannel;
        }

        @Bean
        public ChannelSyncEndpoint inboundChannelEndpoint() {
            ChannelSyncEndpointConfiguration endpointConfiguration = new ChannelSyncEndpointConfiguration();
            endpointConfiguration.setChannel(inboundChannel());
            endpointConfiguration.setTimeout(5000L);
            return new ChannelSyncEndpoint(endpointConfiguration);
        }
    }
}
