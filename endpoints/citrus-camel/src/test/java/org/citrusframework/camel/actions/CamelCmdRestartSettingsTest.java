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
import java.util.List;

import org.citrusframework.camel.UnitTestSupport;
import org.citrusframework.camel.cli.CamelCli;
import org.citrusframework.camel.cli.CamelCliTransientException;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.ProcessAndOutput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies the Camel CLI command restart settings on the send and receive actions.
 */
@Isolated("Mutates global camel.cli System properties")
@ExtendWith(MockitoExtension.class)
public class CamelCmdRestartSettingsTest extends UnitTestSupport {

    @Mock
    private CamelCli camelCli;

    @Mock
    private ProcessAndOutput pao;

    @Mock
    private Process process;

    @AfterEach
    public void cleanup() {
        System.clearProperty("citrus.camel.cli.command.restarts");
        System.clearProperty("citrus.camel.cli.receive.restart.grace.period");
    }

    @Test
    public void shouldNotRestartSendCommandWhenRestartsDisabled() {
        System.setProperty("citrus.camel.cli.command.restarts", "0");

        when(camelCli.send(any(String[].class)))
                .thenThrow(new CamelCliTransientException("Send timeout while sending message via Camel CLI"));

        context.getReferenceResolver().bind("camelCli", camelCli);

        CamelCmdSendAction action = new CamelCmdSendAction.Builder()
                .integration("my-route")
                .body("Hello")
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        Assertions.assertThrows(CamelCliTransientException.class, () -> action.execute(context));
        verify(camelCli, times(1)).send(any(String[].class));
    }

    @Test
    public void shouldRestartSendCommandConfiguredTimes() {
        System.setProperty("citrus.camel.cli.command.restarts", "2");

        when(camelCli.send(any(String[].class)))
                .thenThrow(new CamelCliTransientException("Send timeout while sending message via Camel CLI"))
                .thenThrow(new CamelCliTransientException("Send timeout while sending message via Camel CLI"))
                .thenReturn(pao);

        context.getReferenceResolver().bind("camelCli", camelCli);

        CamelCmdSendAction action = new CamelCmdSendAction.Builder()
                .integration("my-route")
                .body("Hello")
                .withReferenceResolver(context.getReferenceResolver())
                .build();

        action.execute(context);
        verify(camelCli, times(3)).send(any(String[].class));
    }

    @Test
    public void shouldNotRestartReceiveCommandWhenRestartsDisabled() {
        System.setProperty("citrus.camel.cli.command.restarts", "0");

        ProcessAndOutput failed = failedReceive();
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
        verify(camelCli, times(1)).receive(any(String[].class));
    }

    @Test
    public void shouldUseConfiguredGracePeriodOnReceiveRestart() {
        System.setProperty("citrus.camel.cli.receive.restart.grace.period", "0");

        List<String[]> commands = new ArrayList<>();
        ProcessAndOutput failed = failedReceive();

        lenient().when(pao.getProcess()).thenReturn(process);
        lenient().when(process.isAlive()).thenReturn(true);
        lenient().when(pao.getOutput()).thenReturn("""
            Starting to receive messages from existing Camel: my-route (pid: 12345)
            Received Message: (1)
              Body      (String) (bytes: 5)
              Hello
            """);

        when(camelCli.receive(any(String[].class))).thenAnswer(invocation -> {
            commands.add((String[]) invocation.getRawArguments()[0]);
            return commands.size() == 1 ? failed : pao;
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
        List<String> restartCommand = List.of(commands.get(1));
        int sinceIndex = restartCommand.indexOf("--since");
        Assertions.assertTrue(sinceIndex > 0);
        Assertions.assertTrue(Long.parseLong(restartCommand.get(sinceIndex + 1)) < 10L);
    }

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
