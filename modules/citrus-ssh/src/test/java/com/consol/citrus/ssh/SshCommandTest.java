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

package com.consol.citrus.ssh;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import com.consol.citrus.message.MessageHandler;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.easymock.IArgumentMatcher;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;
import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Roland Huss
 * @since 05.09.12
 */
public class SshCommandTest {

    private ByteArrayOutputStream stdout, stderr;
    private SshCommand cmd;
    private MessageHandler handler;

    private static String COMMAND = "shutdown";
    private XmlMapper xmlMapper;
    private ExitCallback exitCallback;

    @BeforeMethod
    public void setup() {
        handler = createMock(MessageHandler.class);
        cmd = new SshCommand(COMMAND,handler);

        stdout = new ByteArrayOutputStream();
        stderr = new ByteArrayOutputStream();
        cmd.setErrorStream(stderr);
        cmd.setOutputStream(stdout);

        exitCallback = createMock(ExitCallback.class);
        cmd.setExitCallback(exitCallback);

        xmlMapper = new XmlMapper();
    }
    
    @Test
    public void base() throws IOException {
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
        Environment env = createMock(Environment.class);
        Map<String,String> map = new HashMap<String,String>();
        map.put(Environment.ENV_USER,"roland");
        expect(env.getEnv()).andReturn(map);
        replay(env);

        prepare("input","output",null,0);
        cmd.start(env);
        verify(env);
    }

    @Test
    public void ioException() throws IOException {
        InputStream i = createMock(InputStream.class);
        expect(i.read((byte[]) anyObject())).andThrow(new IOException("No"));
        i.close();

        exitCallback.onExit(1,"No");
        replay(i, exitCallback);
        cmd.setInputStream(i);

        cmd.run();
    }
    
    /**
     * Prepare actions.
     * @param pInput
     * @param pOutput
     * @param pError
     * @param pExitCode
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void prepare(String pInput, String pOutput, String pError, int pExitCode) {
        String request = xmlMapper.toXML(new SshRequest(COMMAND, pInput));
        SshResponse resp = new SshResponse(pOutput, pError, pExitCode);
        Message respMsg = MessageBuilder.withPayload(xmlMapper.toXML(resp)).build();
        expect(handler.handleMessage(eqMessage(request))).andReturn(respMsg);
        replay(handler);

        exitCallback.onExit(pExitCode);
        replay(exitCallback);

        cmd.setInputStream(new ByteArrayInputStream(pInput.getBytes()));
    }

    /**
     * Special report matcher for mocking reasons.
     * @param expected
     * @return
     */
    public Message<?> eqMessage(final String expected) {
        reportMatcher(new IArgumentMatcher() {
            public boolean matches(Object argument) {
                Message<?> msg = (Message<?>) argument;
                String payload = (String) msg.getPayload();
                return expected.equals(payload);
            }

            public void appendTo(StringBuffer buffer) {
                buffer.append("message matcher");
            }
        });
        return null;
    }
}
