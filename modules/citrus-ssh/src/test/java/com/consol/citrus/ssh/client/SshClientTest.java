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

package com.consol.citrus.ssh.client;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.ssh.model.SshMarshaller;
import com.consol.citrus.ssh.model.SshRequest;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.jcraft.jsch.*;
import org.easymock.IArgumentMatcher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.xml.transform.StringResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.*;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;

/**
 * @author Roland Huss
 * @since 12.09.12
 */
public class SshClientTest extends AbstractTestNGUnitTest {

    private static final String COMMAND = "ls";
    private static final String STDIN = "Hello world";

    private JSch jsch;
    private SshClient client;
    private ByteArrayOutputStream outStream;
    private Session session;
    private ChannelExec channel;
    private static final int CONNECTTION_TIMEOUT = 50;

    @BeforeMethod
    public void setup() throws JSchException {
        jsch = createMock(JSch.class);

        SshEndpointConfiguration endpointConfiguration = new SshEndpointConfiguration();
        client = new SshClient(endpointConfiguration);
        client.setJsch(jsch);

        endpointConfiguration.setHost("planck");
        endpointConfiguration.setUser("roland");
        endpointConfiguration.setPort(1968);
        endpointConfiguration.setConnectionTimeout(CONNECTTION_TIMEOUT);
        endpointConfiguration.setCommandTimeout(2 * 60 * 1000);

        session = createMock(Session.class);
        expect(jsch.getSession("roland","planck",1968)).andReturn(session);

        channel = createMock(ChannelExec.class);

        ReflectionTestUtils.setField(client, "jsch", jsch);

        outStream = new ByteArrayOutputStream();
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,expectedExceptionsMessageRegExp = ".*user.*")
    public void noUser() {
        client.getEndpointConfiguration().setUser(null);
        send();
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,expectedExceptionsMessageRegExp = ".*knownHosts.*")
    public void strictHostCheckingWithoutKnownHosts() throws JSchException {
        strictHostChecking(true, null);
        replay(jsch,session);

        send();
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,expectedExceptionsMessageRegExp = ".*blaHosts.*")
    public void strictHostCheckingWithFaultyKnownHosts() throws JSchException {
        strictHostChecking(true, "classpath:/com/consol/citrus/ssh/blaHosts");
        replay(jsch,session);
        send();
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,expectedExceptionsMessageRegExp = ".*/does/not/exist.*")
    public void strictHostCheckingWithFaultyKnownHosts2() throws JSchException {
        strictHostChecking(true, "/file/that/does/not/exist");
        replay(jsch, session);
        send();
    }

    @Test
    public void strictHostCheckingWithKnownHosts() throws JSchException, IOException {
        strictHostChecking(true, "classpath:com/consol/citrus/ssh/knownHosts");
        jsch.setKnownHosts(isA(InputStream.class));
        standardChannelPrepAndSend();
    }

    private void standardChannelPrepAndSend() throws JSchException, IOException {
        session.connect();
        prepareChannel(COMMAND, 0);
        disconnect();
        replay(jsch, session, channel);
        send();
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = ".*/that/does/not/exist.*")
    public void withUnknownPrivateKey() throws JSchException {
        strictHostChecking(false,null);
        client.getEndpointConfiguration().setPrivateKeyPath("/file/that/does/not/exist");
        jsch.addIdentity("/file/that/does/not/exist", (String) null);
        expectLastCall().andThrow(new JSchException("No such file"));
        replay(jsch, session, channel);
        send();
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,expectedExceptionsMessageRegExp = ".*/notthere\\.key.*")
    public void withUnknownPrivateKey2() throws JSchException {
        strictHostChecking(false,null);
        client.getEndpointConfiguration().setPrivateKeyPath("classpath:com/consol/citrus/ssh/notthere.key");
        jsch.addIdentity("classpath:com/consol/citrus/ssh/notthere.key",(String) null);
        replay(jsch, session, channel);
        send();
    }

    @Test
    public void withPrivateKey() throws JSchException, IOException {
        strictHostChecking(false,null);
        client.getEndpointConfiguration().setPrivateKeyPath("classpath:com/consol/citrus/ssh/private.key");
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
        expect(session.isConnected()).andReturn(true);
        session.disconnect();
        expect(session.openChannel("exec")).andReturn(channel);
    }

    private void prepareChannel(String pCommand, int pExitStatus) throws JSchException, IOException {
        channel.setErrStream((OutputStream) anyObject());
        channel.setOutputStream((OutputStream) anyObject());
        channel.setInputStream((InputStream) anyObject());
        channel.setCommand(pCommand);
        channel.connect(CONNECTTION_TIMEOUT);
        expect(channel.getOutputStream()).andReturn(outStream);
        expect(channel.isClosed()).andReturn(false);
        expect(channel.isClosed()).andReturn(true).times(2);
        expect(channel.getExitStatus()).andReturn(pExitStatus);
        expect(channel.isConnected()).andReturn(true);
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
            session.setConfig("StrictHostKeyChecking","yes");
            client.getEndpointConfiguration().setKnownHosts(knownHosts);
        } else {
            session.setConfig("StrictHostKeyChecking","no");
        }

    }

    private UserInfo getUserInfo(final String arg) {
        reportMatcher(new IArgumentMatcher() {
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
