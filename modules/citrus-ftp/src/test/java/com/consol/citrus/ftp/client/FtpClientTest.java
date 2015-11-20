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
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class FtpClientTest extends AbstractTestNGUnitTest {

    private FTPClient apacheFtpClient = Mockito.mock(FTPClient.class);

    @Test
    public void testLoginLogout() throws Exception {
        FtpClient ftpClient = new FtpClient();
        ftpClient.setFtpClient(apacheFtpClient);

        reset(apacheFtpClient);

        when(apacheFtpClient.isConnected())
                .thenReturn(false)
                .thenReturn(true);

        when(apacheFtpClient.getReplyString()).thenReturn("OK");
        when(apacheFtpClient.getReplyCode()).thenReturn(200);
        when(apacheFtpClient.logout()).thenReturn(true);

        ftpClient.afterPropertiesSet();
        ftpClient.connectAndLogin();
        ftpClient.destroy();

        verify(apacheFtpClient).configure(any(FTPClientConfig.class));
        verify(apacheFtpClient).addProtocolCommandListener(any(ProtocolCommandListener.class));
        verify(apacheFtpClient).connect("localhost", 22222);
        verify(apacheFtpClient).disconnect();

    }

    @Test
    public void testCommand() throws Exception {
        FtpClient ftpClient = new FtpClient();
        ftpClient.setFtpClient(apacheFtpClient);

        reset(apacheFtpClient);

        when(apacheFtpClient.isConnected()).thenReturn(false);
        when(apacheFtpClient.getReplyString()).thenReturn("OK");
        when(apacheFtpClient.getReplyCode()).thenReturn(200);

        when(apacheFtpClient.sendCommand(FTPCmd.PWD, null)).thenReturn(200);

        ftpClient.send(new FtpMessage(FTPCmd.PWD, null), context);

        Message reply = ftpClient.receive(context);

        Assert.assertTrue(reply instanceof FtpMessage);

        FtpMessage ftpReply = (FtpMessage) reply;

        Assert.assertEquals(ftpReply.getCommand(), FTPCmd.PWD);
        Assert.assertNull(ftpReply.getArguments());
        Assert.assertEquals(ftpReply.getReplyCode(), new Integer(200));
        Assert.assertEquals(ftpReply.getReplyString(), "OK");

        verify(apacheFtpClient).connect("localhost", 22222);
    }

    @Test
    public void testCommandWithArguments() throws Exception {
        FtpEndpointConfiguration endpointConfiguration = new FtpEndpointConfiguration();
        FtpClient ftpClient = new FtpClient(endpointConfiguration);
        ftpClient.setFtpClient(apacheFtpClient);

        endpointConfiguration.setUser("admin");
        endpointConfiguration.setPassword("consol");

        reset(apacheFtpClient);

        when(apacheFtpClient.isConnected())
                .thenReturn(false)
                .thenReturn(true);

        when(apacheFtpClient.login("admin", "consol")).thenReturn(true);
        when(apacheFtpClient.getReplyString()).thenReturn("OK");
        when(apacheFtpClient.getReplyCode()).thenReturn(200);
        when(apacheFtpClient.sendCommand(FTPCmd.PWD, null)).thenReturn(200);
        when(apacheFtpClient.sendCommand(FTPCmd.MKD, "testDir")).thenReturn(201);

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

        verify(apacheFtpClient).connect("localhost", 22222);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testCommandWithUserLoginFailed() throws Exception {
        FtpEndpointConfiguration endpointConfiguration = new FtpEndpointConfiguration();
        FtpClient ftpClient = new FtpClient(endpointConfiguration);
        ftpClient.setFtpClient(apacheFtpClient);

        endpointConfiguration.setUser("admin");
        endpointConfiguration.setPassword("consol");

        reset(apacheFtpClient);

        when(apacheFtpClient.isConnected()).thenReturn(false);
        when(apacheFtpClient.getReplyString()).thenReturn("OK");
        when(apacheFtpClient.getReplyCode()).thenReturn(200);

        when(apacheFtpClient.login("admin", "consol")).thenReturn(false);

        ftpClient.send(new FtpMessage(FTPCmd.PWD, null), context);

        verify(apacheFtpClient).connect("localhost", 22222);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testCommandNegativeReply() throws Exception {
        FtpClient ftpClient = new FtpClient();
        ftpClient.setFtpClient(apacheFtpClient);

        reset(apacheFtpClient);

        when(apacheFtpClient.isConnected()).thenReturn(false);
        when(apacheFtpClient.getReplyString()).thenReturn("OK");
        when(apacheFtpClient.getReplyCode()).thenReturn(200);

        when(apacheFtpClient.sendCommand(FTPCmd.PWD, null)).thenReturn(500);

        ftpClient.send(new FtpMessage(FTPCmd.PWD, null), context);

        verify(apacheFtpClient).connect("localhost", 22222);
    }
}
