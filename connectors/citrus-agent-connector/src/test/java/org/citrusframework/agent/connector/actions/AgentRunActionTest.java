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

package org.citrusframework.agent.connector.actions;

import org.citrusframework.agent.connector.CitrusAgentSettings;
import org.citrusframework.agent.connector.UnitTestSupport;
import org.citrusframework.common.TestLoader;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.adapter.StaticEndpointAdapter;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.message.Message;
import org.citrusframework.spi.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AgentRunActionTest extends UnitTestSupport {

    @Test
    public void shouldRunYaml() {
        HttpServer agentServer = new HttpServerBuilder()
                .port(CitrusAgentSettings.getAgentServerPort())
                .endpointAdapter(runEndpointAdapter("citrus-test", MediaType.APPLICATION_YAML_VALUE))
                .build();

        try {
            agentServer.start();

            AgentRunAction action = new AgentActionBuilder()
                    .run()
                    .sourceCode("""
                    name: citrus-test
                    actions:
                      - echo:
                          message: Citrus rocks!
                    """)
                    .build();

            action.execute(context);

            action = new AgentActionBuilder()
                    .run()
                    .sourceFile(Resources.fromClasspath("citrus-test.yaml", AgentRunActionTest.class))
                    .build();

            action.execute(context);
        } finally {
            agentServer.stop();
        }
    }

    @Test
    public void shouldRunXml() {
        HttpServer agentServer = new HttpServerBuilder()
                .port(CitrusAgentSettings.getAgentServerPort())
                .endpointAdapter(runEndpointAdapter("citrus-test", MediaType.APPLICATION_XML_VALUE))
                .build();

        try {
            agentServer.start();

            AgentRunAction action = new AgentActionBuilder()
                    .run()
                    .sourceCode("""
                    <test name="citrus-test" xmlns="http://citrusframework.org/schema/xml/testcase">
                      <actions>
                        <echo>
                          <message>"Citrus rocks!"</message>
                        </echo>
                      </actions>
                    </test>
                    """)
                    .build();

            action.execute(context);

            action = new AgentActionBuilder()
                    .run()
                    .sourceFile(Resources.fromClasspath("citrus-test.xml", AgentRunActionTest.class))
                    .build();

            action.execute(context);
        } finally {
            agentServer.stop();
        }
    }

    @Test
    public void shouldRunGroovy() {
        HttpServer agentServer = new HttpServerBuilder()
                .port(CitrusAgentSettings.getAgentServerPort())
                .endpointAdapter(runEndpointAdapter("citrus-test.groovy", MediaType.TEXT_PLAIN_VALUE))
                .build();

        try {
            agentServer.start();

            AgentRunAction action = new AgentActionBuilder()
                    .run()
                    .type(TestLoader.GROOVY)
                    .sourceCode("""
                    actions {
                        $(echo().message("Citrus rocks!"))
                    }
                    """)
                    .build();

            action.execute(context);

            action = new AgentActionBuilder()
                    .run()
                    .sourceFile(Resources.fromClasspath("citrus-test.groovy", AgentRunActionTest.class))
                    .build();

            action.execute(context);
        } finally {
            agentServer.stop();
        }
    }

    private EndpointAdapter runEndpointAdapter(String testName, String contentType) {
        return new StaticEndpointAdapter() {
            @Override
            protected Message handleMessageInternal(Message message) {
                if (message instanceof HttpMessage httpRequest) {
                    if (httpRequest.getPath().equals("/health")) {
                        Assert.assertEquals(httpRequest.getRequestMethod(), RequestMethod.GET);
                        return new HttpMessage("""
                        { "status": "UP" }
                        """).status(HttpStatus.OK);
                    } else {
                        Assert.assertTrue(httpRequest.getPath().startsWith("/execute/%s".formatted(testName)));
                        Assert.assertEquals(httpRequest.getRequestMethod(), RequestMethod.POST);
                        Assert.assertEquals(httpRequest.getContentType(), contentType);
                        Assert.assertTrue(httpRequest.getPayload(String.class).contains("Citrus rocks!"));

                        return new HttpMessage("""
                        [
                          { "result": "SUCCESS" }
                        ]
                        """).status(HttpStatus.OK);
                    }
                }

                return new HttpMessage().status(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
    }
}
