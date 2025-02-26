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

package org.citrusframework.agent.connector.yaml;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.agent.connector.actions.AgentConnectAction;
import org.citrusframework.agent.connector.actions.AgentRunAction;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.citrusframework.yaml.YamlTestLoader;
import org.citrusframework.yaml.actions.YamlTestActionBuilder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class AgentRunTest extends AbstractYamlActionTest {

    @Mock
    private HttpClient httpClient;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldLoadAgentRunActions() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/agent/connector/yaml/agent-test.yaml");

        context.getReferenceResolver().bind("citrus-agent.client", httpClient);

        HttpMessage healthResponse = new HttpMessage();
        healthResponse.status(HttpStatus.OK);
        healthResponse.setPayload("""
        { "status": "UP" }
        """);

        HttpMessage response = new HttpMessage();
        response.status(HttpStatus.OK);
        response.setPayload("""
        [
          { "result": "SUCCESS" }
        ]
        """);
        doAnswer(invocationOnMock -> {
            Message request = invocationOnMock.getArgument(0, Message.class);
             if (request instanceof HttpMessage httpRequest) {
                 if (httpRequest.getPath().equals("/health")) {
                     Assert.assertEquals(httpRequest.getRequestMethod(), RequestMethod.GET);
                 } else {
                     Assert.assertEquals(httpRequest.getPath(), "/execute/AgentRunTest");
                     Assert.assertEquals(httpRequest.getRequestMethod(), RequestMethod.POST);
                     Assert.assertEquals(httpRequest.getContentType(), MediaType.APPLICATION_YAML_VALUE);
                     Assert.assertTrue(httpRequest.getPayload(String.class).startsWith("name: AgentRunTest"));
                 }
             } else {
                 Assert.fail("Not a Http message");
             }
            return null;
        }).when(httpClient).send(any(Message.class), eq(context));

        when(httpClient.receive(context)).thenReturn(healthResponse).thenReturn(response);

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "AgentRunTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 3L);
        Assert.assertEquals(result.getTestAction(0).getClass(), AgentConnectAction.class);
        Assert.assertEquals(result.getTestAction(1).getClass(), AgentRunAction.class);
    }

    @Test
    public void shouldLookupTestActionBuilder() {
        Assert.assertTrue(YamlTestActionBuilder.lookup("agent").isPresent());
        Assert.assertEquals(YamlTestActionBuilder.lookup("agent").get().getClass(), Agent.class);
    }
}
