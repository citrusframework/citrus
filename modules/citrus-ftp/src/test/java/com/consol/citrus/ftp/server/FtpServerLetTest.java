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
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class FtpServerLetTest {

    private EndpointAdapter endpointAdapter = EasyMock.createMock(EndpointAdapter.class);
    private FtpSession ftpSession = EasyMock.createMock(FtpSession.class);
    private FtpRequest ftpRequest = EasyMock.createMock(FtpRequest.class);

    private FtpServerFtpLet ftpLet = new FtpServerFtpLet(endpointAdapter);

    @Test
    public void testCommand() throws FtpException, IOException {

        reset(endpointAdapter, ftpSession, ftpRequest);

        expect(ftpRequest.getCommand()).andReturn(FTPCmd.MKD.getCommand()).once();
        expect(ftpRequest.getArgument()).andReturn("testDir").once();

        expect(endpointAdapter.handleMessage(anyObject(FtpMessage.class))).andAnswer(new IAnswer<FtpMessage>() {
            @Override
            public FtpMessage answer() throws Throwable {
                FtpMessage ftpMessage = (FtpMessage) getCurrentArguments()[0];

                Assert.assertEquals(ftpMessage.getPayload(String.class), FTPCmd.MKD.getCommand());

                Assert.assertEquals(ftpMessage.getCommand(), FTPCmd.MKD);
                Assert.assertEquals(ftpMessage.getArguments(), "testDir");
                Assert.assertNull(ftpMessage.getReplyCode());
                Assert.assertNull(ftpMessage.getReplyString());

                return new FtpMessage(FTPCmd.MKD, "testDir").replyCode(200).replyString("OK");
            }
        });

        replay(endpointAdapter, ftpSession, ftpRequest);

        FtpletResult result = ftpLet.beforeCommand(ftpSession, ftpRequest);

        Assert.assertEquals(result, FtpletResult.DEFAULT);

        verify(endpointAdapter, ftpSession, ftpRequest);
    }

}
