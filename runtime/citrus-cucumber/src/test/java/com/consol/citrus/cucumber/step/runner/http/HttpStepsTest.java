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

package com.consol.citrus.cucumber.step.runner.http;

import java.io.IOException;

import com.consol.citrus.DefaultTestCaseRunner;
import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.cucumber.UnitTestSupport;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.client.HttpEndpointConfiguration;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.http.message.HttpMessageBuilder;
import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.message.Message;
import com.consol.citrus.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class HttpStepsTest extends UnitTestSupport {

    private HttpSteps steps;

    private TestCaseRunner runner;

    @Autowired
    private HttpClient httpClient;

    @BeforeMethod
    public void injectResources() {
        steps = new HttpSteps();
        runner = new DefaultTestCaseRunner(context);
        CitrusAnnotations.injectAll(steps, citrus, context);
        CitrusAnnotations.injectTestRunner(steps, runner);

        reset(httpClient);
        when(httpClient.getEndpointConfiguration()).thenReturn(new HttpEndpointConfiguration());
        when(httpClient.createProducer()).thenReturn(httpClient);
        when(httpClient.createConsumer()).thenReturn(httpClient);
    }

    @Test
    public void testSendClientRequestRaw() throws IOException {
        steps.setClient("httpClient");
        steps.sendClientRequestFull(FileUtils.readToString(new ClassPathResource("data/request.txt")));

        TestCase testCase = runner.getTestCase();
        Assert.assertEquals(testCase.getActionCount(), 1L);
        Assert.assertTrue(testCase.getTestAction(0) instanceof SendMessageAction);
        SendMessageAction action = (SendMessageAction) testCase.getTestAction(0);

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertTrue(action.getMessageBuilder() instanceof HttpMessageBuilder);
        Assert.assertEquals(((HttpMessageBuilder) action.getMessageBuilder()).getMessage().getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE), "text/plain;charset=UTF-8");
        Assert.assertEquals(((HttpMessageBuilder) action.getMessageBuilder()).getMessage().getHeader(HttpMessageHeaders.HTTP_ACCEPT), "text/plain, application/xml, application/json, */*");
        Assert.assertEquals(((HttpMessageBuilder) action.getMessageBuilder()).getMessage().getHeader(HttpMessageHeaders.HTTP_REQUEST_URI), "http://localhost:8080/test");
        Assert.assertEquals(((HttpMessageBuilder) action.getMessageBuilder()).getMessage().getHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD), "POST");
        Assert.assertEquals(((HttpMessageBuilder) action.getMessageBuilder()).getMessage().getHeader("Accept-Charset"), "utf-8");
        Assert.assertEquals(((HttpMessageBuilder) action.getMessageBuilder()).getMessage().getPayload(String.class), "<TestRequestMessage>\n  <text>Hello server</text>\n</TestRequestMessage>");

        verify(httpClient).send(any(Message.class), eq(context));
    }

    @Test
    public void testReceiveClientResponseRaw() throws IOException {
        String body = String.format("<TestResponseMessage>%n  <text>Hello Citrus</text>%n</TestResponseMessage>");
        when(httpClient.receive(eq(context), eq(5000L))).thenReturn(new HttpMessage(body)
                .header("Date", "Thu, 02 Mar 2017 16")
                .header("Accept-Charset", "utf-8")
                .header("Server", "Jetty(9.4.1.v20170120)")
                .contentType("text/plain;charset=utf-8")
                .version("HTTP/1.1")
                .status(HttpStatus.OK));

        steps.setClient("httpClient");
        steps.receiveClientResponseFull(FileUtils.readToString(new ClassPathResource("data/response.txt")));

        TestCase testCase = runner.getTestCase();
        Assert.assertEquals(testCase.getActionCount(), 1L);
        Assert.assertTrue(testCase.getTestAction(0) instanceof ReceiveMessageAction);
        ReceiveMessageAction action = (ReceiveMessageAction) testCase.getTestAction(0);

        Assert.assertEquals(action.getEndpoint(), httpClient);
        Assert.assertTrue(action.getMessageBuilder() instanceof HttpMessageBuilder);
        Assert.assertEquals(((HttpMessageBuilder) action.getMessageBuilder()).getMessage().getHeader(HttpMessageHeaders.HTTP_STATUS_CODE), 200);
        Assert.assertEquals(((HttpMessageBuilder) action.getMessageBuilder()).getMessage().getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE), "text/plain;charset=utf-8");
        Assert.assertEquals(((HttpMessageBuilder) action.getMessageBuilder()).getMessage().getHeader("Accept-Charset"), "utf-8");
        Assert.assertEquals(((HttpMessageBuilder) action.getMessageBuilder()).getMessage().getPayload(String.class), body);
    }

}
