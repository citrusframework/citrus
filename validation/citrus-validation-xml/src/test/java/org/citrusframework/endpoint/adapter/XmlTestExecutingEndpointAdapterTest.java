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

package org.citrusframework.endpoint.adapter;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.config.CitrusSpringConfig;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.context.TestContextFactoryBean;
import org.citrusframework.endpoint.adapter.mapping.XPathPayloadMappingKeyExtractor;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.endpoint.direct.DirectSyncEndpoint;
import org.citrusframework.endpoint.direct.DirectSyncEndpointConfiguration;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.MessageType;
import org.citrusframework.validation.MessageValidator;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @since 1.4
 */
@ContextConfiguration(classes = { CitrusSpringConfig.class, XmlTestExecutingEndpointAdapterTest.EndpointConfig.class })
public class XmlTestExecutingEndpointAdapterTest extends UnitTestSupport {

    @Autowired
    private XmlTestExecutingEndpointAdapter endpointAdapter;

    @Mock
    private MessageValidator<?> xmlMessageValidator;

    @Mock
    private MessageValidator<?> plaintextMessageValidator;

    @BeforeClass
    public void setupMocks() {
        MockitoAnnotations.openMocks(this);
        when(xmlMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.XML.name()));
        when(plaintextMessageValidator.supportsMessageType(any(String.class), any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.PLAINTEXT.name()));
    }

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory contextFactory = super.createTestContextFactory();
        contextFactory.getMessageValidatorRegistry().addMessageValidator("xml", xmlMessageValidator);
        contextFactory.getMessageValidatorRegistry().addMessageValidator("text", plaintextMessageValidator);
        return contextFactory;
    }

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

        private MessageQueue inboundQueue = new DefaultMessageQueue("inboundQueue");

        @Bean
        public XmlTestExecutingEndpointAdapter testSimulator(TestContextFactoryBean testContextFactoryBean) {
            XmlTestExecutingEndpointAdapter endpointAdapter = new XmlTestExecutingEndpointAdapter();
            XPathPayloadMappingKeyExtractor mappingKeyExtractor = new XPathPayloadMappingKeyExtractor();
            mappingKeyExtractor.setXpathExpression("//Test/@name");
            endpointAdapter.setMappingKeyExtractor(mappingKeyExtractor);

            endpointAdapter.setResponseEndpointAdapter(directEndpointAdapter());
            endpointAdapter.setTestContextFactory(testContextFactoryBean);

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
