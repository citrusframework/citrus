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

package org.citrusframework.ftp.server;

import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.ftp.client.FtpEndpointConfiguration;
import org.citrusframework.ftp.message.FtpMessage;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.ftpserver.ftplet.*;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class FtpServerFtpLetTest {

    private EndpointAdapter endpointAdapter = Mockito.mock(EndpointAdapter.class);
    private FtpSession ftpSession = Mockito.mock(FtpSession.class);
    private FtpRequest ftpRequest = Mockito.mock(FtpRequest.class);

    private FtpServerFtpLet ftpLet = new FtpServerFtpLet(new FtpEndpointConfiguration(), endpointAdapter);

    @Test
    public void testCommand() {
        reset(endpointAdapter, ftpSession, ftpRequest);

        when(ftpRequest.getCommand()).thenReturn(FTPCmd.MKD.getCommand());
        when(ftpRequest.getArgument()).thenReturn("testDir");

        doAnswer((Answer<FtpMessage>) invocation -> {
            FtpMessage ftpMessage = (FtpMessage) invocation.getArguments()[0];

            Assert.assertEquals(ftpMessage.getPayload(String.class), "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><command xmlns=\"http://www.citrusframework.org/schema/ftp/message\"><signal>MKD</signal><arguments>testDir</arguments></command>");

            Assert.assertEquals(ftpMessage.getSignal(), FTPCmd.MKD.getCommand());
            Assert.assertEquals(ftpMessage.getArguments(), "testDir");
            Assert.assertNull(ftpMessage.getReplyCode());
            Assert.assertNull(ftpMessage.getReplyString());

            return FtpMessage.success(FTPReply.COMMAND_OK, "OK");
        }).when(endpointAdapter).handleMessage(any(FtpMessage.class));

        FtpletResult result = ftpLet.beforeCommand(ftpSession, ftpRequest);

        Assert.assertEquals(result, FtpletResult.SKIP);
    }

    @Test
    public void testAutoLogin() {
        reset(endpointAdapter, ftpSession, ftpRequest);

        when(ftpRequest.getCommand()).thenReturn(FTPCmd.USER.getCommand()).thenReturn(FTPCmd.PASS.getCommand());
        when(ftpRequest.getArgument()).thenReturn("foo").thenReturn("secret");

        FtpletResult result = ftpLet.beforeCommand(ftpSession, ftpRequest);
        Assert.assertEquals(result, FtpletResult.DEFAULT);
        
        result = ftpLet.beforeCommand(ftpSession, ftpRequest);
        Assert.assertEquals(result, FtpletResult.DEFAULT);
    }

}
