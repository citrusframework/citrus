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

package org.citrusframework.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.citrusframework.CitrusSettings;
import org.citrusframework.TestCase;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.message.RawMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test listener collects all messages sent and received by Citrus during test execution. Listener
 * writes a trace file with all message content per test case to a output directory.
 *
 * Note: This class is not thread safe! Parallel test execution leads to behaviour that messages get mixed.
 * Proper correlation to test case is not possible here.
 *
 * @author Christoph Deppisch
 * @since 1.2
 */
public class MessageTracingTestListener extends AbstractTestListener implements MessageListener {

    /** File ending for all message trace files */
    private static final String TRACE_FILE_ENDING = ".msgs";

    /** File ending for all message trace files */
    private static final Date TEST_EXECUTION_DATE = new Date();

    /** Output directory */
    private String outputDirectory = CitrusSettings.getMessageTraceDirectory();

    /** List of messages to trace */
    private final List<String> messages = new ArrayList<>();

    /** Locking object for synchronization */
    private final Object lockObject = new Object();

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(MessageTracingTestListener.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTestStart(TestCase test) {
        synchronized (lockObject) {
            messages.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTestFinish(TestCase test) {
        if (messages.isEmpty()) {
            return; // do not write empty message trace file
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getTraceFile(test.getName())))) {
            writer.write(separator() + newLine() + newLine());

            synchronized (lockObject) {
                for (String message : messages) {
                    writer.write(message);
                    writer.write(newLine() + separator() + newLine() + newLine());
                }
            }

            writer.flush();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to write message trace to filesystem", e);
        }
    }

    @Override
    public void onInboundMessage(Message message, TestContext context) {
        if (message instanceof RawMessage) {
            synchronized (lockObject) {
                messages.add("INBOUND_MESSAGE:" + newLine() + newLine() + message.print(context));
            }
        }
    }

    @Override
    public void onOutboundMessage(Message message, TestContext context) {
        if (message instanceof RawMessage) {
            synchronized (lockObject) {
                messages.add("OUTBOUND_MESSAGE:" + newLine() + newLine() + message.print(context));
            }
        }
    }

    /**
     * Creates message separator line.
     * @return
     */
    private String separator() {
        return "======================================================================";
    }

    /**
     * Get new line character.
     * @return
     */
    private String newLine() {
        return System.getProperty("line.separator");
    }

    /**
     * Returns the trace file for message tracing. The file name should be unique per test execution run; the test name
     * and a execution id (the test execution start time) is embedded within the filename. Normally this should suffice
     * to ensure that the trace filename is unique per test/test-execution.
     *
     * @param testName the name of the test to create the trace file for
     * @return the trace file to use for message tracing
     */
    protected File getTraceFile(String testName) {
        File targetDirectory = new File(outputDirectory);
        if (!targetDirectory.exists() && !targetDirectory.mkdirs()) {
            throw new CitrusRuntimeException("Unable to create message tracing output directory: " + outputDirectory);
        }

        String testExecutionStartTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(TEST_EXECUTION_DATE);
        String filename = String.format("%s_%s%s", testName, testExecutionStartTime, TRACE_FILE_ENDING);

        File traceFile = new File(targetDirectory, filename);
        if (traceFile.exists()) {
            logger.warn(String.format("Trace file '%s' already exists. Normally a new file is created on each test execution ", traceFile.getName()));
        }
        return traceFile;
    }

    /**
     * Sets the outputDirectory.
     * @param outputDirectory the outputDirectory to set
     */
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}
