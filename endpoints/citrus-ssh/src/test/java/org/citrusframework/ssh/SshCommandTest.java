/*
 * Copyright 2006-2012 the original author or authors.
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

package org.citrusframework.ssh;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.ssh.client.SshEndpointConfiguration;
import org.citrusframework.ssh.model.SshMarshaller;
import org.citrusframework.ssh.model.SshRequest;
import org.citrusframework.ssh.model.SshResponse;
import org.citrusframework.xml.StringResult;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Roland Huss
 * @since 05.09.12
 */
public class SshCommandTest {

    private ByteArrayOutputStream stdout, stderr;
    private SshCommand cmd;
    private EndpointAdapter adapter;

    private static final String COMMAND = "shutdown";
    private SshMarshaller marshaller;
    private ExitCallback exitCallback;

    @BeforeMethod
    public void setup() {
        adapter = Mockito.mock(EndpointAdapter.class);

        cmd = new SshCommand(COMMAND, adapter, new SshEndpointConfiguration());

        stdout = new ByteArrayOutputStream();
        stderr = new ByteArrayOutputStream();
        cmd.setErrorStream(stderr);
        cmd.setOutputStream(stdout);

        exitCallback = Mockito.mock(ExitCallback.class);
        cmd.setExitCallback(exitCallback);

        marshaller = new SshMarshaller();
    }

    @Test
    public void base() {
        String input = "Hello world";
        String output = "Think positive!";
        String error = "Error, Error";
        int exitCode = 12;

        assertEquals(cmd.getCommand(),COMMAND);

        prepare(input, output, error, exitCode);
        cmd.run();

        assertEquals(stdout.toByteArray(),output.getBytes());
        assertEquals(stderr.toByteArray(),error.getBytes());
    }

    @Test
    public void start() throws IOException {
        Environment env = Mockito.mock(Environment.class);
        ChannelSession session = Mockito.mock(ChannelSession.class);
        Map<String,String> map = new HashMap<>();
        map.put(Environment.ENV_USER,"roland");
        when(env.getEnv()).thenReturn(map);
        prepare("input","output",null,0);
        cmd.start(session, env);
    }

    @Test
    public void ioException() throws IOException {
        InputStream i = Mockito.mock(InputStream.class);
        doThrow(new IOException("No")).when(i).readAllBytes();
        i.close();

        exitCallback.onExit(1,"No");
        cmd.setInputStream(i);

        cmd.run();
    }

    private void prepare(String pInput, String pOutput, String pError, int pExitCode) {
        StringResult request = new StringResult();
        marshaller.marshal(new SshRequest(COMMAND, pInput), request);

        SshResponse resp = new SshResponse(pOutput, pError, pExitCode);
        StringResult response = new StringResult();
        marshaller.marshal(resp, response);

        Message respMsg = new DefaultMessage(response.toString());
        when(adapter.handleMessage(eqMessage(request.toString()))).thenReturn(respMsg);
        exitCallback.onExit(pExitCode);
        cmd.setInputStream(new ByteArrayInputStream(pInput.getBytes()));
    }

    public Message eqMessage(final String expected) {
        argThat(argument -> {
            Message msg = (Message) argument;
            String payload = (String) msg.getPayload();
            return expected.equals(payload);
        });
        return null;
    }
}
