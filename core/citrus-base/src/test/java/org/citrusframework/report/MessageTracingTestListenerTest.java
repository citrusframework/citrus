/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.report;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.message.RawMessage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class MessageTracingTestListenerTest extends UnitTestSupport {

    private final MessageTracingTestListener testling = new MessageTracingTestListener();

    @BeforeClass
    public void setupTestling() {
        testling.setOutputDirectory("target/citrus-logs/trace/messages");
    }

    @Test
    public void shouldReturnTheSameTraceFile() throws Exception {
        String testname = "SomeDummyTest";
        Assert.assertEquals(testling.getTraceFile(testname).getAbsolutePath(), testling.getTraceFile(testname).getAbsolutePath());
    }

    @Test
    public void shouldContainMessages() throws Exception {
        String testname = "SomeDummyTest";
        String inboundPayload = "Inbound Message";
        String outboundPayload = "Outbound Message";

        TestCase testCaseMock = setupTestCaseMock(testname);
        RawMessage inboundMessageMock = setupRawMessageMock(inboundPayload);
        RawMessage outboundMessageMock = setupRawMessageMock(outboundPayload);

        testling.onTestStart(testCaseMock);
        testling.onInboundMessage(inboundMessageMock, context);
        testling.onOutboundMessage(outboundMessageMock, context);
        testling.onTestFinish(testCaseMock);

        assertFileExistsWithContent(testname, inboundPayload);
        assertFileExistsWithContent(testname, outboundPayload);
    }

    private TestCase setupTestCaseMock(String testname) {
        TestCase mock = mock(TestCase.class);
        when(mock.getName()).thenReturn(testname);
        return mock;
    }

    private RawMessage setupRawMessageMock(String payload) {
        RawMessage mock = mock(RawMessage.class);
        when(mock.print(context)).thenReturn(payload);
        return mock;
    }

    private void assertFileExistsWithContent(String testname, String content) {
        File traceFile = testling.getTraceFile(testname);
        Assert.assertTrue(traceFile.isFile());
        try (Scanner scanner = new Scanner(traceFile)) {
            String fileContent = scanner.useDelimiter("\\Z").next();
            Assert.assertTrue(fileContent.contains(content));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
