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

package org.citrusframework.camel.cli;

import java.util.List;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.ProcessAndOutput;
import org.citrusframework.jbang.ProcessLauncher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CamelCliTest {

    @Mock
    private ProcessLauncher launcher;

    @Mock
    private ProcessAndOutput pao;

    @Mock
    private Process process;

    @Test
    public void shouldSendMessage() {
        mockSendResult(0, "Reply received - Hello");

        Assertions.assertSame(pao, new CamelCli(launcher).send("my-route", "--body", "Hello"));
    }

    @Test
    public void shouldFailTransientOnSendTimeout() {
        mockSendResult(1, "Send timeout");

        CamelCliTransientException error = Assertions.assertThrows(CamelCliTransientException.class,
                () -> new CamelCli(launcher).send("my-route", "--body", "Hello"));
        Assertions.assertTrue(error.getMessage().contains("Send timeout"));
    }

    @Test
    public void shouldFailTransientWhenNoRunningIntegrationFound() {
        mockSendResult(1, "Name or pid my-route matches 0 running Camel integrations. Specify a name or PID that matches exactly one.");

        CamelCliTransientException error = Assertions.assertThrows(CamelCliTransientException.class,
                () -> new CamelCli(launcher).send("my-route", "--body", "Hello"));
        Assertions.assertTrue(error.getMessage().contains("no running Camel integration found"));
    }

    @Test
    public void shouldFailOnErrorExitCode() {
        mockSendResult(2, "Unknown option: '--foo'");

        CitrusRuntimeException error = Assertions.assertThrows(CitrusRuntimeException.class,
                () -> new CamelCli(launcher).send("my-route", "--foo"));
        Assertions.assertFalse(error instanceof CamelCliTransientException);
    }

    private void mockSendResult(int exitCode, String output) {
        when(launcher.run(eq("cmd"), anyList())).thenAnswer(invocation -> {
            List<String> args = invocation.getArgument(1);
            Assertions.assertEquals("send", args.get(0));
            return pao;
        });
        when(pao.getProcess()).thenReturn(process);
        when(process.exitValue()).thenReturn(exitCode);
        when(pao.getOutput()).thenReturn(output);
    }
}
