/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.ftp.client;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.ftp.message.FtpMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.*;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class FtpClientTest extends AbstractTestNGUnitTest {

    private FTPClient apacheFtpClient = EasyMock.createMock(FTPClient.class);

    @Test
    public void testLoginLogout() throws Exception {
        FtpClient ftpClient = new FtpClient();
        ftpClient.setFtpClient(apacheFtpClient);

        reset(apacheFtpClient);

        apacheFtpClient.configure(anyObject(FTPClientConfig.class));
        expectLastCall().once();

        apacheFtpClient.addProtocolCommandListener(anyObject(ProtocolCommandListener.class));
        expectLastCall().once();

        expect(apacheFtpClient.isConnected())
                .andReturn(false).once()
                .andReturn(true).once();

        apacheFtpClient.connect("localhost", 22222);
        expectLastCall().once();

        expect(apacheFtpClient.getReplyString()).andReturn("OK").once();
        expect(apacheFtpClient.getReplyCode()).andReturn(200).once();

        apacheFtpClient.disconnect();
        expectLastCall().once();

        expect(apacheFtpClient.logout()).andReturn(true).once();

        replay(apacheFtpClient);

        ftpClient.afterPropertiesSet();
        ftpClient.connectAndLogin();
        ftpClient.destroy();

        verify(apacheFtpClient);
    }

    @Test
    public void testCommand() throws Exception {
        FtpClient ftpClient = new FtpClient();
        ftpClient.setFtpClient(apacheFtpClient);

        reset(apacheFtpClient);

        expect(apacheFtpClient.isConnected()).andReturn(false).once();

        apacheFtpClient.connect("localhost", 22222);
        expectLastCall().once();

        expect(apacheFtpClient.getReplyString()).andReturn("OK").times(2);
        expect(apacheFtpClient.getReplyCode()).andReturn(200).once();

        expect(apacheFtpClient.sendCommand(FTPCmd.PWD, null)).andReturn(200).once();

        replay(apacheFtpClient);

        ftpClient.send(new FtpMessage(FTPCmd.PWD, null), context);

        Message reply = ftpClient.receive(context);

        Assert.assertTrue(reply instanceof FtpMessage);

        FtpMessage ftpReply = (FtpMessage) reply;

        Assert.assertEquals(ftpReply.getCommand(), FTPCmd.PWD);
        Assert.assertNull(ftpReply.getArguments());
        Assert.assertEquals(ftpReply.getReplyCode(), new Integer(200));
        Assert.assertEquals(ftpReply.getReplyString(), "OK");

        verify(apacheFtpClient);
    }

    @Test
    public void testCommandWithArguments() throws Exception {
        FtpEndpointConfiguration endpointConfiguration = new FtpEndpointConfiguration();
        FtpClient ftpClient = new FtpClient(endpointConfiguration);
        ftpClient.setFtpClient(apacheFtpClient);

        endpointConfiguration.setUser("admin");
        endpointConfiguration.setPassword("consol");

        reset(apacheFtpClient);

        expect(apacheFtpClient.isConnected())
                .andReturn(false).once()
                .andReturn(true).once();

        apacheFtpClient.connect("localhost", 22222);
        expectLastCall().once();

        expect(apacheFtpClient.login("admin", "consol")).andReturn(true).once();

        expect(apacheFtpClient.getReplyString()).andReturn("OK").times(3);
        expect(apacheFtpClient.getReplyCode()).andReturn(200).once();

        expect(apacheFtpClient.sendCommand(FTPCmd.PWD, null)).andReturn(200).once();
        expect(apacheFtpClient.sendCommand(FTPCmd.MKD, "testDir")).andReturn(201).once();

        replay(apacheFtpClient);

        ftpClient.send(new FtpMessage(FTPCmd.PWD, null), context);

        Message reply = ftpClient.receive(context);

        Assert.assertTrue(reply instanceof FtpMessage);

        FtpMessage ftpReply = (FtpMessage) reply;

        Assert.assertEquals(ftpReply.getCommand(), FTPCmd.PWD);
        Assert.assertNull(ftpReply.getArguments());
        Assert.assertEquals(ftpReply.getReplyCode(), new Integer(200));
        Assert.assertEquals(ftpReply.getReplyString(), "OK");

        ftpClient.send(new FtpMessage(FTPCmd.MKD, "testDir"), context);

        reply = ftpClient.receive(context);

        Assert.assertTrue(reply instanceof FtpMessage);

        ftpReply = (FtpMessage) reply;

        Assert.assertEquals(ftpReply.getCommand(), FTPCmd.MKD);
        Assert.assertEquals(ftpReply.getArguments(), "testDir");
        Assert.assertEquals(ftpReply.getReplyCode(), new Integer(201));
        Assert.assertEquals(ftpReply.getReplyString(), "OK");

        verify(apacheFtpClient);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testCommandWithUserLoginFailed() throws Exception {
        FtpEndpointConfiguration endpointConfiguration = new FtpEndpointConfiguration();
        FtpClient ftpClient = new FtpClient(endpointConfiguration);
        ftpClient.setFtpClient(apacheFtpClient);

        endpointConfiguration.setUser("admin");
        endpointConfiguration.setPassword("consol");

        reset(apacheFtpClient);

        expect(apacheFtpClient.isConnected()).andReturn(false).once();

        apacheFtpClient.connect("localhost", 22222);
        expectLastCall().once();

        expect(apacheFtpClient.getReplyString()).andReturn("OK").once();
        expect(apacheFtpClient.getReplyCode()).andReturn(200).once();

        expect(apacheFtpClient.login("admin", "consol")).andReturn(false).once();

        replay(apacheFtpClient);

        ftpClient.send(new FtpMessage(FTPCmd.PWD, null), context);

        verify(apacheFtpClient);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testCommandNegativeReply() throws Exception {
        FtpClient ftpClient = new FtpClient();
        ftpClient.setFtpClient(apacheFtpClient);

        reset(apacheFtpClient);

        expect(apacheFtpClient.isConnected()).andReturn(false).once();

        apacheFtpClient.connect("localhost", 22222);
        expectLastCall().once();

        expect(apacheFtpClient.getReplyString()).andReturn("OK").times(2);
        expect(apacheFtpClient.getReplyCode()).andReturn(200).once();

        expect(apacheFtpClient.sendCommand(FTPCmd.PWD, null)).andReturn(500).once();

        replay(apacheFtpClient);

        ftpClient.send(new FtpMessage(FTPCmd.PWD, null), context);

        verify(apacheFtpClient);
    }
}
