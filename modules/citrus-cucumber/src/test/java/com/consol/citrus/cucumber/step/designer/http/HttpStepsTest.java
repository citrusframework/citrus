/*
 * Copyright 2006-2017 the original author or authors.
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

package com.consol.citrus.cucumber.step.designer.http;

import com.consol.citrus.Citrus;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.dsl.annotations.CitrusDslAnnotations;
import com.consol.citrus.dsl.design.DefaultTestDesigner;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.message.HttpMessageContentBuilder;
import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.IOException;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class HttpStepsTest extends AbstractTestNGUnitTest {

    private Citrus citrus;
    private HttpSteps steps;

    private TestDesigner designer;

    @Autowired
    private HttpClient httpClient;

    @BeforeClass
    public void setup() {
        citrus = Citrus.newInstance(applicationContext);
    }

    @BeforeMethod
    public void injectResources() {
        steps = new HttpSteps();
        designer = new DefaultTestDesigner(applicationContext, context);
        CitrusAnnotations.injectAll(steps, citrus, context);
        CitrusDslAnnotations.injectTestDesigner(steps, designer);
    }

    @Test
    public void testSendClientRequestRaw() throws IOException {
        steps.setClient("httpClient");
        steps.sendClientRequestFull(FileUtils.readToString(new ClassPathResource("data/request.txt")));

        Assert.assertEquals(designer.getTestCase().getActionCount(), 1L);
        Assert.assertTrue(designer.getTestCase().getTestAction(0) instanceof DelegatingTestAction);
        SendMessageAction action = (SendMessageAction) ((DelegatingTestAction) designer.getTestCase().getTestAction(0)).getDelegate();

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertTrue(action.getMessageBuilder() instanceof HttpMessageContentBuilder);
        Assert.assertEquals(((HttpMessageContentBuilder) action.getMessageBuilder()).getMessage().getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE), "text/plain;charset=UTF-8");
        Assert.assertEquals(((HttpMessageContentBuilder) action.getMessageBuilder()).getMessage().getHeader(HttpMessageHeaders.HTTP_ACCEPT), "text/plain, application/xml, application/json, */*");
        Assert.assertEquals(((HttpMessageContentBuilder) action.getMessageBuilder()).getMessage().getHeader(HttpMessageHeaders.HTTP_REQUEST_URI), "http://localhost:8080/test");
        Assert.assertEquals(((HttpMessageContentBuilder) action.getMessageBuilder()).getMessage().getHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD), "POST");
        Assert.assertEquals(((HttpMessageContentBuilder) action.getMessageBuilder()).getMessage().getHeader("Accept-Charset"), "utf-8");
        Assert.assertEquals(((HttpMessageContentBuilder) action.getMessageBuilder()).getMessage().getPayload(String.class), "<TestRequestMessage>\n  <text>Hello server</text>\n</TestRequestMessage>");
    }

    @Test
    public void testReceiveClientResponseRaw() throws IOException {
        steps.setClient("httpClient");
        steps.receiveClientResponseFull(FileUtils.readToString(new ClassPathResource("data/response.txt")));

        Assert.assertEquals(designer.getTestCase().getActionCount(), 1L);
        Assert.assertTrue(designer.getTestCase().getTestAction(0) instanceof DelegatingTestAction);
        ReceiveMessageAction action = (ReceiveMessageAction) ((DelegatingTestAction) designer.getTestCase().getTestAction(0)).getDelegate();

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertTrue(action.getMessageBuilder() instanceof HttpMessageContentBuilder);
        Assert.assertEquals(((HttpMessageContentBuilder) action.getMessageBuilder()).getMessage().getHeader(HttpMessageHeaders.HTTP_STATUS_CODE), 200);
        Assert.assertEquals(((HttpMessageContentBuilder) action.getMessageBuilder()).getMessage().getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE), "text/plain;charset=utf-8");
        Assert.assertEquals(((HttpMessageContentBuilder) action.getMessageBuilder()).getMessage().getHeader("Accept-Charset"), "utf-8");
        Assert.assertEquals(((HttpMessageContentBuilder) action.getMessageBuilder()).getMessage().getPayload(String.class), "<TestResponseMessage>\n  <text>Hello Citrus</text>\n</TestResponseMessage>");
    }

}