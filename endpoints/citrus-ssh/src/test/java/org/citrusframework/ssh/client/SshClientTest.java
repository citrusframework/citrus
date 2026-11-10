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

package org.citrusframework.ssh.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.ssh.model.SshMarshaller;
import org.citrusframework.ssh.model.SshRequest;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.api.xml.StringResult;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @since 12.09.12
 */
public class SshClientTest extends AbstractUnitTest {

    private static final String COMMAND = "ls";
    private static final String STDIN = "Hello world";

    private JSch jsch;
    private SshClient client;
    private ByteArrayOutputStream outStream;
    private Session session;
    private ChannelExec channel;
    private static final int CONNECTTION_TIMEOUT = 50;

    @BeforeEach
    public void setup() throws JSchException {
        jsch = Mockito.mock(JSch.class);

        SshEndpointConfiguration endpointConfiguration = new SshEndpointConfiguration();
        client = new SshClient(endpointConfiguration);
        client.setJsch(jsch);

        endpointConfiguration.setHost("planck");
        endpointConfiguration.setUser("roland");
        endpointConfiguration.setPort(1968);
        endpointConfiguration.setConnectionTimeout(CONNECTTION_TIMEOUT);
        endpointConfiguration.setCommandTimeout(2 * 60 * 1000);

        session = Mockito.mock(Session.class);
        when(jsch.getSession("roland","planck",1968)).thenReturn(session);

        channel = Mockito.mock(ChannelExec.class);

        ReflectionTestUtils.setField(client, "jsch", jsch);

        outStream = new ByteArrayOutputStream();
    }

    @Test
    public void noUser() {
        client.getEndpointConfiguration().setUser(null);
        CitrusRuntimeException thrown = Assertions.assertThrows(CitrusRuntimeException.class, () -> {
            send();
        });
        Assertions.assertTrue(thrown.getMessage().matches(".*user.*"), "Unexpected exception message: " + thrown.getMessage());
    }

    @Test
    public void strictHostCheckingWithoutKnownHosts() {
        strictHostChecking(true, null);
        CitrusRuntimeException thrown = Assertions.assertThrows(CitrusRuntimeException.class, () -> {
            send();
        });
        Assertions.assertTrue(thrown.getMessage().matches(".*knownHosts.*"), "Unexpected exception message: " + thrown.getMessage());
    }

    @Test
    public void strictHostCheckingWithFaultyKnownHosts() {
        strictHostChecking(true, "classpath:/org/citrusframework/ssh/blaHosts");
        CitrusRuntimeException thrown = Assertions.assertThrows(CitrusRuntimeException.class, () -> {
            send();
        });
        Assertions.assertTrue(thrown.getMessage().matches(".*blaHosts.*"), "Unexpected exception message: " + thrown.getMessage());
    }

    @Test
    public void strictHostCheckingWithFaultyKnownHosts2() {
        strictHostChecking(true, "/file/that/does/not/exist");
        CitrusRuntimeException thrown = Assertions.assertThrows(CitrusRuntimeException.class, () -> {
            send();
        });
        Assertions.assertTrue(thrown.getMessage().matches(".*/does/not/exist.*"), "Unexpected exception message: " + thrown.getMessage());
    }

    @Test
    public void strictHostCheckingWithKnownHosts() throws JSchException, IOException {
        strictHostChecking(true, "classpath:org/citrusframework/ssh/knownHosts");
        jsch.setKnownHosts(isA(InputStream.class));
        standardChannelPrepAndSend();
    }

    private void standardChannelPrepAndSend() throws JSchException, IOException {
        session.connect();
        prepareChannel(COMMAND, 0);
        disconnect();
        send();
    }

    @Test
    public void withUnknownPrivateKey() throws JSchException {
        strictHostChecking(false,null);
        client.getEndpointConfiguration().setPrivateKeyPath("/file/that/does/not/exist");
        doThrow(new JSchException("No such file")).when(jsch).addIdentity("/file/that/does/not/exist", (String) null);
        CitrusRuntimeException thrown = Assertions.assertThrows(CitrusRuntimeException.class, () -> {
            send();
        });
        Assertions.assertTrue(thrown.getMessage().matches(".*/that/does/not/exist.*"), "Unexpected exception message: " + thrown.getMessage());
    }

    @Test
    public void withUnknownPrivateKey2() throws JSchException {
        strictHostChecking(false,null);
        client.getEndpointConfiguration().setPrivateKeyPath("classpath:org/citrusframework/ssh/notthere.key");
        jsch.addIdentity("classpath:org/citrusframework/ssh/notthere.key",(String) null);
        CitrusRuntimeException thrown = Assertions.assertThrows(CitrusRuntimeException.class, () -> {
            send();
        });
        Assertions.assertTrue(thrown.getMessage().matches(".*/notthere\\.key.*"), "Unexpected exception message: " + thrown.getMessage());
    }

    @Test
    public void withPrivateKey() throws JSchException, IOException {
        strictHostChecking(false,null);
        client.getEndpointConfiguration().setPrivateKeyPath("classpath:org/citrusframework/ssh/private.key");
        jsch.addIdentity(isA(String.class), (String) isNull());
        strictHostChecking(false, null);
        standardChannelPrepAndSend();
    }

    @Test
    public void withPassword() throws JSchException, IOException {
        client.getEndpointConfiguration().setPassword("consol");
        session.setUserInfo(getUserInfo("consol"));
        session.setPassword("consol");

        strictHostChecking(false, null);
        standardChannelPrepAndSend();
    }

    @Test
    public void straight() throws JSchException, IOException {

        strictHostChecking(false, null);
        standardChannelPrepAndSend();
    }

    private void send() {
        client.send(createMessage(COMMAND, STDIN), context);
    }

    private void disconnect() throws JSchException {
        channel.disconnect();
        when(session.isConnected()).thenReturn(true);
        session.disconnect();
        when(session.openChannel("exec")).thenReturn(channel);
    }

    private void prepareChannel(String pCommand, int pExitStatus) throws JSchException, IOException {
        channel.setErrStream((OutputStream) any());
        channel.setOutputStream((OutputStream) any());
        channel.setInputStream((InputStream) any());
        channel.setCommand(pCommand);
        channel.connect(CONNECTTION_TIMEOUT);
        when(channel.getOutputStream()).thenReturn(outStream);
        when(channel.isClosed()).thenReturn(false);
        when(channel.isClosed()).thenReturn(true);
        when(channel.getExitStatus()).thenReturn(pExitStatus);
        when(channel.isConnected()).thenReturn(true);
    }

    private Message createMessage(String pCommand, String pInput) {
        SshRequest request = new SshRequest(pCommand,pInput);

        StringResult payload = new StringResult();
        new SshMarshaller().marshal(request, payload);
        return new DefaultMessage(payload.toString());
    }

    private void strictHostChecking(boolean flag,String knownHosts) {
        if (flag) {
            client.getEndpointConfiguration().setStrictHostChecking(true);
            session.setConfig(KnownHostsServerKeyVerifier.STRICT_CHECKING_OPTION,"yes");
            client.getEndpointConfiguration().setKnownHosts(knownHosts);
        } else {
            session.setConfig(KnownHostsServerKeyVerifier.STRICT_CHECKING_OPTION,"no");
        }

    }

    @SuppressWarnings("unchecked")
    private UserInfo getUserInfo(final String arg) {
        argThat(new ArgumentMatcher() {
            public boolean matches(Object argument) {
                UserInfo info = (UserInfo) argument;
                assertFalse(info.promptPassphrase("bla"));
                assertFalse(info.promptYesNo("bla"));
                assertFalse(info.promptPassword("bla"));
                assertNull(info.getPassphrase());
                return info.getPassword().equals(arg);
            }

            public void appendTo(StringBuffer buffer) {
                buffer.append("user info matcher");
            }
        });
        return null;
    }

}
