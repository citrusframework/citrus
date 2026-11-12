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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.citrusframework.camel.UnitTestSupport;
import org.citrusframework.camel.cli.CamelCli;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.ProcessAndOutput;
import org.citrusframework.message.Message;
import org.mockito.Mock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CamelCmdReceiveActionTest extends UnitTestSupport {

    @Mock
    private CamelCli camelCli;

    @Mock
    private ProcessAndOutput pao;

    @Mock
    private Process process;

    @BeforeEach
    public void setUp() {
        lenient().when(pao.getProcess()).thenReturn(process);
    }

    @Test
    public void shouldReceiveMessage() {
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
        lenient().when(process.isAlive()).thenReturn(true);
        lenient().when(process.exitValue()).thenReturn(0);

        when(pao.getOutput()).thenReturn(output);
        when(camelCli.receive(any(String[].class))).thenAnswer(invocation -> {
            String[] args = (String[]) invocation.getRawArguments()[0];
            Assertions.assertArrayEquals(expectedArgs, args);
            return pao;
        });

        context.getReferenceResolver().bind("camelCli", camelCli);

        CamelCmdReceiveAction action = new CamelCmdReceiveAction.Builder()
                .integration("my-route")
                .endpoint("jms:queue:foo")
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);
    }

    @Test
    public void shouldReceiveMessageJsonOutput() {
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
        lenient().when(process.isAlive()).thenReturn(true);
        lenient().when(process.exitValue()).thenReturn(0);

        when(pao.getOutput()).thenReturn(output);
        when(camelCli.receive(any(String[].class))).thenAnswer(invocation -> {
            String[] args = (String[]) invocation.getRawArguments()[0];
            Assertions.assertArrayEquals(expectedArgs, args, Arrays.toString(args));
            return pao;
        });

        context.getReferenceResolver().bind("camelCli", camelCli);

        CamelCmdReceiveAction action = new CamelCmdReceiveAction.Builder()
                .integration("my-route")
                .endpoint("jms:queue:foo")
                .jsonOutput(true)
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);

        Message storedMessage = context.getMessageStore().getMessage("my-route.message");
        Assertions.assertNotNull(storedMessage);
        Assertions.assertTrue(storedMessage.getPayload(String.class).contains("Hello"));
        Assertions.assertTrue(storedMessage.getPayload(String.class).startsWith("{"));

        storedMessage = context.getMessageStore().getMessage("my-route.jms:queue:foo");
        Assertions.assertNotNull(storedMessage);
        Assertions.assertTrue(storedMessage.getPayload(String.class).contains("Hello"));
        Assertions.assertTrue(storedMessage.getPayload(String.class).startsWith("{"));
    }

    @Test
    public void shouldStoreLastMessage() {
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
        lenient().when(process.isAlive()).thenReturn(true);
        lenient().when(process.exitValue()).thenReturn(0);

        when(pao.getOutput()).thenReturn(output);
        when(camelCli.receive(any(String[].class))).thenAnswer(invocation -> {
            String[] args = (String[]) invocation.getRawArguments()[0];
            Assertions.assertArrayEquals(expectedArgs, args, Arrays.toString(args));
            return pao;
        });

        context.getReferenceResolver().bind("camelCli", camelCli);

        CamelCmdReceiveAction action = new CamelCmdReceiveAction.Builder()
                .integration("my-route")
                .endpoint("jms:queue:foo")
                .jsonOutput(true)
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);

        Message storedMessage = context.getMessageStore().getMessage("my-route.message");
        Assertions.assertNotNull(storedMessage);
        Assertions.assertTrue(storedMessage.getPayload(String.class).contains("Last message"));
        Assertions.assertTrue(storedMessage.getPayload(String.class).startsWith("{"));
    }

    @Test
    public void shouldTimeoutWhenNoMessage() {

        String output = """
        Starting to receive messages from existing Camel: my-route (pid: 12345)
        Waiting for messages ...
        """;

        lenient().when(process.isAlive()).thenReturn(true);
        lenient().when(process.exitValue()).thenReturn(0);

        lenient().when(pao.getOutput()).thenReturn(output);
        when(camelCli.receive(any(String[].class))).thenReturn(pao);

        context.getReferenceResolver().bind("camelCli", camelCli);

        CamelCmdReceiveAction action = new CamelCmdReceiveAction.Builder()
                .integration("my-route")
                .endpoint("jms:queue:foo")
                .maxAttempts(3)
                .delayBetweenAttempts(1000L)
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        Assertions.assertThrows(ActionTimeoutException.class, () -> action.execute(context));
    }

    @Test
    public void shouldTimeoutWhenNoMessageJsonOutput() {

        String output = """
        Starting to receive messages from existing Camel: my-route (pid: 12345)
        Waiting for messages ...
        """;

        lenient().when(process.isAlive()).thenReturn(true);
        lenient().when(process.exitValue()).thenReturn(0);

        lenient().when(pao.getOutput()).thenReturn(output);
        when(camelCli.receive(any(String[].class))).thenReturn(pao);

        context.getReferenceResolver().bind("camelCli", camelCli);

        CamelCmdReceiveAction action = new CamelCmdReceiveAction.Builder()
                .integration("my-route")
                .endpoint("jms:queue:foo")
                .maxAttempts(3)
                .delayBetweenAttempts(1000L)
                .jsonOutput(true)
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        Assertions.assertThrows(ActionTimeoutException.class, () -> action.execute(context));
    }

    @Test
    public void shouldRestartReceiveCommandOnTransientCliFailure() {
        String failureOutput = """
        Starting to receive messages from existing Camel: my-route (pid: 12345)
        Waiting for messages ...
        java.lang.NullPointerException: Cannot read the array length because "grepPatterns" is null
        at org.apache.camel.dsl.jbang.core.commands.action.CamelReceiveAction.isValidGrep(CamelReceiveAction.java:846)
        """;

        String output = """
        Starting to receive messages from existing Camel: my-route (pid: 12345)
        Received Message: (1)
          Endpoint  jms://queue:foo
          Message   (DefaultMessage)
          Body      (String) (bytes: 5)
          Hello
        """;

        Process failedProcess = mock(Process.class);
        lenient().when(failedProcess.isAlive()).thenReturn(false);
        lenient().when(failedProcess.exitValue()).thenReturn(1);

        ProcessAndOutput failed = mock(ProcessAndOutput.class);
        lenient().when(failed.getProcess()).thenReturn(failedProcess);
        lenient().when(failed.getOutput()).thenReturn(failureOutput);

        lenient().when(process.isAlive()).thenReturn(true);
        lenient().when(process.exitValue()).thenReturn(0);
        lenient().when(pao.getOutput()).thenReturn(output);

        when(camelCli.receive(any(String[].class))).thenReturn(failed, pao);

        context.getReferenceResolver().bind("camelCli", camelCli);

        CamelCmdReceiveAction action = new CamelCmdReceiveAction.Builder()
                .integration("my-route")
                .endpoint("jms:queue:foo")
                .maxAttempts(3)
                .delayBetweenAttempts(0L)
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);

        verify(camelCli, times(2)).receive(any(String[].class));
    }

    @Test
    public void shouldFailWhenReceiveCommandFailsRepeatedly() {
        String failureOutput = """
        Starting to receive messages from existing Camel: my-route (pid: 12345)
        Waiting for messages ...
        java.lang.NullPointerException
        """;

        Process failedProcess = mock(Process.class);
        lenient().when(failedProcess.isAlive()).thenReturn(false);
        lenient().when(failedProcess.exitValue()).thenReturn(1);

        ProcessAndOutput failed = mock(ProcessAndOutput.class);
        lenient().when(failed.getProcess()).thenReturn(failedProcess);
        lenient().when(failed.getOutput()).thenReturn(failureOutput);

        when(camelCli.receive(any(String[].class))).thenReturn(failed);

        context.getReferenceResolver().bind("camelCli", camelCli);

        CamelCmdReceiveAction action = new CamelCmdReceiveAction.Builder()
                .integration("my-route")
                .endpoint("jms:queue:foo")
                .maxAttempts(3)
                .delayBetweenAttempts(0L)
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        Assertions.assertThrows(CitrusRuntimeException.class, () -> action.execute(context));

        verify(camelCli, times(2)).receive(any(String[].class));
    }

    @Test
    public void shouldReceiveMessagesSinceActionStartOnRestartWithTailZero() {
        List<String[]> commands = new ArrayList<>();
        ProcessAndOutput failed = failedReceive();

        lenient().when(process.isAlive()).thenReturn(true);
        lenient().when(pao.getOutput()).thenReturn(RECEIVED_OUTPUT);

        when(camelCli.receive(any(String[].class))).thenAnswer(invocation -> {
            commands.add((String[]) invocation.getRawArguments()[0]);
            return commands.size() == 1 ? failed : pao;
        });

        context.getReferenceResolver().bind("camelCli", camelCli);

        CamelCmdReceiveAction action = new CamelCmdReceiveAction.Builder()
                .integration("my-route")
                .endpoint("jms:queue:foo")
                .grep("Hello")
                .maxAttempts(3)
                .delayBetweenAttempts(0L)
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);

        Assertions.assertEquals(2, commands.size());
        Assertions.assertArrayEquals(new String[] {
                "my-route", "--endpoint", "jms:queue:foo", "--grep", "Hello", "--tail", "0", "--logging-color=false"
        }, commands.get(0));

        List<String> restartCommand = List.of(commands.get(1));
        Assertions.assertEquals(List.of("my-route", "--endpoint", "jms:queue:foo", "--grep", "Hello", "--tail", "-1", "--since"),
                restartCommand.subList(0, 8));
        Assertions.assertTrue(Long.parseLong(restartCommand.get(8)) >= 10L);
        Assertions.assertEquals("--logging-color=false", restartCommand.get(9));
    }

    @Test
    public void shouldKeepArgumentsOnRestartWithTail() {
        List<String[]> commands = new ArrayList<>();
        ProcessAndOutput failed = failedReceive();

        lenient().when(process.isAlive()).thenReturn(true);
        lenient().when(pao.getOutput()).thenReturn(RECEIVED_OUTPUT);

        when(camelCli.receive(any(String[].class))).thenAnswer(invocation -> {
            commands.add((String[]) invocation.getRawArguments()[0]);
            return commands.size() == 1 ? failed : pao;
        });

        context.getReferenceResolver().bind("camelCli", camelCli);

        CamelCmdReceiveAction action = new CamelCmdReceiveAction.Builder()
                .integration("my-route")
                .endpoint("jms:queue:foo")
                .tail(1)
                .maxAttempts(3)
                .delayBetweenAttempts(0L)
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);

        Assertions.assertEquals(2, commands.size());
        Assertions.assertArrayEquals(commands.get(0), commands.get(1));
    }

    @Test
    public void shouldRestartReceiveCommandWhenIntegrationNotFound() {
        List<String[]> commands = new ArrayList<>();
        Process notFoundProcess = mock(Process.class);
        lenient().when(notFoundProcess.isAlive()).thenReturn(true);

        ProcessAndOutput notFound = mock(ProcessAndOutput.class);
        lenient().when(notFound.getProcess()).thenReturn(notFoundProcess);
        lenient().when(notFound.getOutput()).thenReturn("Waiting for messages ...\n");

        lenient().when(process.isAlive()).thenReturn(true);
        lenient().when(pao.getOutput()).thenReturn(RECEIVED_OUTPUT);

        when(camelCli.receive(any(String[].class))).thenAnswer(invocation -> {
            commands.add((String[]) invocation.getRawArguments()[0]);
            return commands.size() == 1 ? notFound : pao;
        });

        context.getReferenceResolver().bind("camelCli", camelCli);

        CamelCmdReceiveAction action = new CamelCmdReceiveAction.Builder()
                .integration("my-route")
                .endpoint("jms:queue:foo")
                .maxAttempts(3)
                .delayBetweenAttempts(0L)
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);

        Assertions.assertEquals(2, commands.size());
        Assertions.assertArrayEquals(commands.get(0), commands.get(1));
        verify(notFoundProcess).destroy();
    }

    @Test
    public void shouldTimeoutWhenIntegrationNotFoundAfterRestart() {
        lenient().when(process.isAlive()).thenReturn(true);
        lenient().when(pao.getOutput()).thenReturn("Waiting for messages ...\n");

        when(camelCli.receive(any(String[].class))).thenReturn(pao);

        context.getReferenceResolver().bind("camelCli", camelCli);

        CamelCmdReceiveAction action = new CamelCmdReceiveAction.Builder()
                .integration("my-route")
                .endpoint("jms:queue:foo")
                .maxAttempts(3)
                .delayBetweenAttempts(0L)
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        Assertions.assertThrows(ActionTimeoutException.class, () -> action.execute(context));

        verify(camelCli, times(2)).receive(any(String[].class));
    }

    private static final String RECEIVED_OUTPUT = """
        Starting to receive messages from existing Camel: my-route (pid: 12345)
        Received Message: (1)
          Endpoint  jms://queue:foo
          Message   (DefaultMessage)
          Body      (String) (bytes: 5)
          Hello
        """;

    private static ProcessAndOutput failedReceive() {
        Process failedProcess = mock(Process.class);
        lenient().when(failedProcess.isAlive()).thenReturn(false);
        lenient().when(failedProcess.exitValue()).thenReturn(1);

        ProcessAndOutput failed = mock(ProcessAndOutput.class);
        lenient().when(failed.getProcess()).thenReturn(failedProcess);
        lenient().when(failed.getOutput()).thenReturn("java.lang.NullPointerException");
        return failed;
    }
}
