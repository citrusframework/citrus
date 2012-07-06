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

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.consol.citrus.TestCase;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Test listener collects all messages sent and received by Citrus during test execution. Listener
 * writes a trace file with all message content per test case to a output directory.
 * 
 * Note: This class is not thread safe! Parallel test execution might lead to unexpected behaviour.
 * 
 * @author Christoph Deppisch
 * @since 1.2
 */
public class MessageTracingTestListener extends AbstractTestListener implements InitializingBean {
    
    /** New line in log file */
    private static final String NEWLINE = "\n";

    /** Separator string placed between messages in log */
    private static final String SEPARATOR = "======================================================================";

    /** File ending for all message trace files */
    private static final String TRACE_FILE_ENDING = ".msgs";

    /** Output directory */
    private Resource outputDirectory = new FileSystemResource("logs/trace/messages/");
    
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(MessageTracingTestListener.class);
    
    /** List of messages to trace */
    private List<String> messages = new ArrayList<String>();
    
    /** Locking object for synchronization */
    private Object lockObject = new Object();
            
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
        BufferedWriter writer = null;
        
        try {
            Resource outputFile = outputDirectory.createRelative(test.getName() + TRACE_FILE_ENDING);
            
            writer = new BufferedWriter(new FileWriter(outputFile.getFile()));
            
            writer.write(SEPARATOR + NEWLINE + NEWLINE);
            
            synchronized (lockObject) {
                for (String message : messages) {
                    writer.write(message);
                    writer.write(NEWLINE + SEPARATOR + NEWLINE + NEWLINE);
                }
            }
            
            writer.flush();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to write message trace to filesystem", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    log.error("Error while closing message trace file writer", e);
                }
            }
        }
    }
    
    /**
     * Adds a message to the current message stack for this test execution.
     * @param message the message content.
     */
    public void traceMessage(String message) {
        synchronized (lockObject) {
            messages.add(message);
        }
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
    
}
