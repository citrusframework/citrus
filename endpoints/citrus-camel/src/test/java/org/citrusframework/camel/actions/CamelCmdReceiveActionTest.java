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

package org.citrusframework.camel.actions;

import java.util.Arrays;

import org.citrusframework.camel.UnitTestSupport;
import org.citrusframework.camel.jbang.CamelJBang;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.jbang.ProcessAndOutput;
import org.citrusframework.message.Message;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class CamelCmdReceiveActionTest extends UnitTestSupport {

    @Mock
    private CamelJBang camelJBang;

    @Mock
    private ProcessAndOutput pao;

    @Mock
    private Process process;

    @BeforeTest
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(pao.getProcess()).thenReturn(process);
    }

    @Test
    public void shouldReceiveMessage() throws Exception {
        String output = """
        Starting to receive messages from existing Camel: my-route (pid: 12345)
        Waiting for messages ...
        Received Message: (1)
          Endpoint  jms://queue:foo
          Message   (DefaultMessage)
          Body      (String) (bytes: 5)
          Hello
        """;

        String[] expectedArgs = new String[] {
           "my-route",
           "--endpoint",
           "jms:queue:foo",
           "--tail",
           "0",
           "--logging-color=false"
        };
        when(process.isAlive()).thenReturn(true);
        when(process.exitValue()).thenReturn(0);

        when(pao.getOutput()).thenReturn(output);
        when(camelJBang.receive(any(String[].class))).thenAnswer(invocation -> {
            String[] args = (String[]) invocation.getRawArguments()[0];
            Assert.assertEquals(args, expectedArgs);
            return pao;
        });

        context.getReferenceResolver().bind("camelJBang", camelJBang);

        CamelCmdReceiveAction action = new CamelCmdReceiveAction.Builder()
                .integration("my-route")
                .endpoint("jms:queue:foo")
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);
    }

    @Test
    public void shouldReceiveMessageJsonOutput() throws Exception {
        String output = """
        Starting to receive messages from existing Camel: my-route (pid: 12345)
        Waiting for messages ...
        {
          "messages": [
            {
              "message": {
                "exchangeType": "org.apache.camel.support.DefaultExchange",
                "messageType": "org.apache.camel.support.DefaultMessage",
                "body": {
                  "type": "java.lang.String",
                  "value": "Hello"
                }
              },
              "uid": 1,
              "endpointUri": "jms:\\/\\/queue:foo",
              "remoteEndpoint": false,
              "timestamp": 0
            }
          ]
        }

        """;

        String[] expectedArgs = new String[] {
           "my-route",
           "--endpoint",
           "jms:queue:foo",
           "--tail",
           "0",
           "--output=json",
           "--compact=false",
           "--pretty",
           "--logging-color=false"
        };
        when(process.isAlive()).thenReturn(true);
        when(process.exitValue()).thenReturn(0);

        when(pao.getOutput()).thenReturn(output);
        when(camelJBang.receive(any(String[].class))).thenAnswer(invocation -> {
            String[] args = (String[]) invocation.getRawArguments()[0];
            Assert.assertEquals(args, expectedArgs, Arrays.toString(args));
            return pao;
        });

        context.getReferenceResolver().bind("camelJBang", camelJBang);

        CamelCmdReceiveAction action = new CamelCmdReceiveAction.Builder()
                .integration("my-route")
                .endpoint("jms:queue:foo")
                .jsonOutput(true)
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);

        Message storedMessage = context.getMessageStore().getMessage("my-route.message");
        Assert.assertNotNull(storedMessage);
        Assert.assertTrue(storedMessage.getPayload(String.class).contains("Hello"));
        Assert.assertTrue(storedMessage.getPayload(String.class).startsWith("{"));

        storedMessage = context.getMessageStore().getMessage("my-route.jms:queue:foo");
        Assert.assertNotNull(storedMessage);
        Assert.assertTrue(storedMessage.getPayload(String.class).contains("Hello"));
        Assert.assertTrue(storedMessage.getPayload(String.class).startsWith("{"));
    }

    @Test
    public void shouldStoreLastMessage() throws Exception {
        String output = """
        Starting to receive messages from existing Camel: my-route (pid: 12345)
        Waiting for messages ...
        {
          "messages": [
            {
              "message": {
                "exchangeType": "org.apache.camel.support.DefaultExchange",
                "messageType": "org.apache.camel.support.DefaultMessage",
                "body": {
                  "type": "java.lang.String",
                  "value": "1st message"
                }
              },
              "uid": 1,
              "endpointUri": "jms:\\/\\/queue:foo",
              "remoteEndpoint": false,
              "timestamp": 0
            }
          ]
        }

        {
          "messages": [
            {
              "message": {
                "exchangeType": "org.apache.camel.support.DefaultExchange",
                "messageType": "org.apache.camel.support.DefaultMessage",
                "body": {
                  "type": "java.lang.String",
                  "value": "Last message"
                }
              },
              "uid": 1,
              "endpointUri": "jms:\\/\\/queue:foo",
              "remoteEndpoint": false,
              "timestamp": 0
            }
          ]
        }

        """;

        String[] expectedArgs = new String[] {
           "my-route",
           "--endpoint",
           "jms:queue:foo",
           "--tail",
           "0",
           "--output=json",
           "--compact=false",
           "--pretty",
           "--logging-color=false"
        };
        when(process.isAlive()).thenReturn(true);
        when(process.exitValue()).thenReturn(0);

        when(pao.getOutput()).thenReturn(output);
        when(camelJBang.receive(any(String[].class))).thenAnswer(invocation -> {
            String[] args = (String[]) invocation.getRawArguments()[0];
            Assert.assertEquals(args, expectedArgs, Arrays.toString(args));
            return pao;
        });

        context.getReferenceResolver().bind("camelJBang", camelJBang);

        CamelCmdReceiveAction action = new CamelCmdReceiveAction.Builder()
                .integration("my-route")
                .endpoint("jms:queue:foo")
                .jsonOutput(true)
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);

        Message storedMessage = context.getMessageStore().getMessage("my-route.message");
        Assert.assertNotNull(storedMessage);
        Assert.assertTrue(storedMessage.getPayload(String.class).contains("Last message"));
        Assert.assertTrue(storedMessage.getPayload(String.class).startsWith("{"));
    }

    @Test(expectedExceptions = ActionTimeoutException.class)
    public void shouldTimeoutWhenNoMessage() throws Exception {
        String output = """
        Starting to receive messages from existing Camel: my-route (pid: 12345)
        Waiting for messages ...
        """;

        when(process.isAlive()).thenReturn(true);
        when(process.exitValue()).thenReturn(0);

        when(pao.getOutput()).thenReturn(output);
        when(camelJBang.receive(any(String[].class))).thenReturn(pao);

        context.getReferenceResolver().bind("camelJBang", camelJBang);

        CamelCmdReceiveAction action = new CamelCmdReceiveAction.Builder()
                .integration("my-route")
                .endpoint("jms:queue:foo")
                .maxAttempts(3)
                .delayBetweenAttempts(1000L)
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);
    }

    @Test(expectedExceptions = ActionTimeoutException.class)
    public void shouldTimeoutWhenNoMessageJsonOutput() throws Exception {
        String output = """
        Starting to receive messages from existing Camel: my-route (pid: 12345)
        Waiting for messages ...
        """;

        when(process.isAlive()).thenReturn(true);
        when(process.exitValue()).thenReturn(0);

        when(pao.getOutput()).thenReturn(output);
        when(camelJBang.receive(any(String[].class))).thenReturn(pao);

        context.getReferenceResolver().bind("camelJBang", camelJBang);

        CamelCmdReceiveAction action = new CamelCmdReceiveAction.Builder()
                .integration("my-route")
                .endpoint("jms:queue:foo")
                .maxAttempts(3)
                .delayBetweenAttempts(1000L)
                .jsonOutput(true)
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);
    }

}
