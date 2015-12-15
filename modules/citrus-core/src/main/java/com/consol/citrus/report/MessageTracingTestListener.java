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

package com.consol.citrus.report;

import com.consol.citrus.TestCase;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.RawMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
public class MessageTracingTestListener extends AbstractTestListener implements InitializingBean, MessageListener {
    
    /** File ending for all message trace files */
    private static final String TRACE_FILE_ENDING = ".msgs";

    /** File ending for all message trace files */
    private static final Date TEST_EXECUTION_DATE = new Date();

    /** Output directory */
    private Resource outputDirectory = new FileSystemResource("logs/trace/messages/");
    
    /** List of messages to trace */
    private final List<String> messages = new ArrayList<>();
    
    /** Locking object for synchronization */
    private final Object lockObject = new Object();

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(MessageTracingTestListener.class);
            
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

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(getTraceFile(test.getName())))) {
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
                messages.add("INBOUND_MESSAGE:" + newLine() + newLine() + message);
            }
        }
    }

    @Override
    public void onOutboundMessage(Message message, TestContext context) {
        if (message instanceof RawMessage) {
            synchronized (lockObject) {
                messages.add("OUTBOUND_MESSAGE:" + newLine() + newLine() + message);
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
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        if (!outputDirectory.exists()) {
            boolean success = outputDirectory.getFile().mkdirs();
            
            if (!success) {
                throw new CitrusRuntimeException("Unable to create output directory structure for message tracing");
            }
        }
    }

    /**
     * Sets the outputDirectory.
     * @param outputDirectory the outputDirectory to set
     */
    public void setOutputDirectory(Resource outputDirectory) {
        this.outputDirectory = outputDirectory;
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
        String testExecutionStartTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(TEST_EXECUTION_DATE);
        String filename = String.format("%s_%s%s", testName, testExecutionStartTime, TRACE_FILE_ENDING);
        try {
            Resource traceResource = outputDirectory.createRelative(filename);
            if(traceResource.exists()) {
                LOG.warn(String.format("Trace file '%s' already exists. Normally a new file is created on each test execution ", traceResource.getFilename()));
            }
            return traceResource.getFile();
        }
        catch (IOException e) {
            throw new CitrusRuntimeException("Error creating trace file", e);
        }
    }
}
