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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class AgentRunTest extends AbstractYamlActionTest {

    @Mock
    private HttpClient httpClient;

    @Test
    public void shouldLoadAgentRunActions() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/agent/connector/yaml/agent.citrus.it.yaml");

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
                     Assertions.assertEquals(RequestMethod.GET, httpRequest.getRequestMethod());
                 } else {
                     Assertions.assertEquals("/execute/AgentRunTest", httpRequest.getPath());
                     Assertions.assertEquals(RequestMethod.POST, httpRequest.getRequestMethod());
                     Assertions.assertEquals(MediaType.APPLICATION_YAML_VALUE, httpRequest.getContentType());
                     Assertions.assertTrue(httpRequest.getPayload(String.class).startsWith("name: AgentRunTest"));
                 }
             } else {
                 Assertions.fail("Not a Http message");
             }
            return null;
        }).when(httpClient).send(any(Message.class), eq(context));

        when(httpClient.receive(context)).thenReturn(healthResponse).thenReturn(response);

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("AgentRunTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(3L, result.getActionCount());
        Assertions.assertEquals(AgentConnectAction.class, result.getTestAction(0).getClass());
        Assertions.assertEquals(AgentRunAction.class, result.getTestAction(1).getClass());
    }

    @Test
    public void shouldLookupTestActionBuilder() {
        Assertions.assertTrue(YamlTestActionBuilder.lookup("agent").isPresent());
        Assertions.assertEquals(Agent.class, YamlTestActionBuilder.lookup("agent").get().getClass());
    }
}
