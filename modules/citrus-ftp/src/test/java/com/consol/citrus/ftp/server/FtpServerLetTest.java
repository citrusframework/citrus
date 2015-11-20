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

package com.consol.citrus.ftp.server;

import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.ftp.message.FtpMessage;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.ftpserver.ftplet.*;
import org.apache.tools.ant.taskdefs.Java;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class FtpServerLetTest {

    private EndpointAdapter endpointAdapter = Mockito.mock(EndpointAdapter.class);
    private FtpSession ftpSession = Mockito.mock(FtpSession.class);
    private FtpRequest ftpRequest = Mockito.mock(FtpRequest.class);

    private FtpServerFtpLet ftpLet = new FtpServerFtpLet(endpointAdapter);

    @Test
    public void testCommand() throws FtpException, IOException {

        reset(endpointAdapter, ftpSession, ftpRequest);

        when(ftpRequest.getCommand()).thenReturn(FTPCmd.MKD.getCommand());
        when(ftpRequest.getArgument()).thenReturn("testDir");

        doAnswer(new Answer<FtpMessage>() {
            @Override
            public FtpMessage answer(InvocationOnMock invocation) throws Throwable {
                FtpMessage ftpMessage = (FtpMessage) invocation.getArguments()[0];

                Assert.assertEquals(ftpMessage.getPayload(String.class), FTPCmd.MKD.getCommand());

                Assert.assertEquals(ftpMessage.getCommand(), FTPCmd.MKD);
                Assert.assertEquals(ftpMessage.getArguments(), "testDir");
                Assert.assertNull(ftpMessage.getReplyCode());
                Assert.assertNull(ftpMessage.getReplyString());

                return new FtpMessage(FTPCmd.MKD, "testDir").replyCode(200).replyString("OK");
            }
        }).when(endpointAdapter).handleMessage(any(FtpMessage.class));

        FtpletResult result = ftpLet.beforeCommand(ftpSession, ftpRequest);

        Assert.assertEquals(result, FtpletResult.DEFAULT);

    }

}
