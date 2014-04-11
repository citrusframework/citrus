/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.aop;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.report.MessageTracingTestListener;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.Message;

import java.io.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Aspect can store received messages to the file system in order to track 
 * the message flow.
 * 
 * @author Christoph Deppisch
 * @deprecated since Citrus 1.2, in favor of using {@link MessageTracingTestListener}
 */
@Aspect
@Deprecated
public class StoreMessageInterceptorAspect {
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(StoreMessageInterceptorAspect.class);
    
    /** Target directory */
    private Resource debugDirectory = new FileSystemResource("logs/debug/messages/");
    
    /** Count messages */
    private static final AtomicInteger FILE_COUNTER = new AtomicInteger(1);
    
    @Pointcut("execution(org.springframework.integration.Message com.consol.citrus.message.MessageReceiver.receive*(..))")
    public void inReceivingMessage() {}

    @AfterReturning(pointcut="com.consol.citrus.aop.StoreMessageInterceptorAspect.inReceivingMessage()",
            returning="message")
    public void doInterceptMessage(Object message) {
        //in case of receive timeout message will be null, check that
        if (message != null) {
            storeMessage((Message<?>)message);
        }
    }
    
    /**
     * Save message to the file system.
     * @param receivedMessage
     * @throws CitrusRuntimeException
     */
    private void storeMessage(Message<?> receivedMessage) {
        Writer headerOutput = null;
        Writer bodyOutput = null;
        
        try {
            if (!debugDirectory.exists()) {
                boolean success = debugDirectory.getFile().mkdirs();
                
                if (!success) {
                    log.warn("Unable to create folder structure for message interceptor");
                }
            }
            
            int counter = StoreMessageInterceptorAspect.FILE_COUNTER.getAndIncrement();
            
            Resource fileBody = debugDirectory.createRelative("message" + counter + ".body");
            Resource fileHeader = debugDirectory.createRelative("message" + counter + ".header");
            
            //write body message
            bodyOutput = new BufferedWriter(new FileWriter(fileBody.getFile()));
            bodyOutput.write(receivedMessage.getPayload().toString());
            bodyOutput.flush();

            //write header message
            headerOutput = new BufferedWriter(new FileWriter(fileHeader.getFile()));
            Map<String, Object> header = receivedMessage.getHeaders();

            for (Entry<String, Object> entry : header.entrySet()) {
                headerOutput.write(entry.getKey() + "=" + entry.getValue() + "\n");
            }
            
            headerOutput.flush();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Error while trying to save incoming message to filesystem", e);
        } finally {
            if (headerOutput != null) {
                try {
                    headerOutput.close();
                } catch (IOException e) {
                    log.error("Error while closing header writer", e);
                }
            }
            
            if (bodyOutput != null) {
                try {
                    bodyOutput.close();
                } catch (IOException e) {
                    log.error("Error while closing body writer", e);
                }
            }
        }
    }
    
    /**
     * Resets the file counter.
     */
    public static final void resetFileCounter() {
        FILE_COUNTER.set(1);
    }

    /**
     * Sets the target directory on the file system.
     * @param debugDirectory the debugDirectory to set
     */
    public void setDebugDirectory(Resource debugDirectory) {
        this.debugDirectory = debugDirectory;
    }
}
