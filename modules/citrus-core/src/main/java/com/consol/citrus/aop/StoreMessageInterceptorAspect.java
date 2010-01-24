/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.aop;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.core.Message;

import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Aspect can store received messages to the file system in order to track 
 * the message flow.
 * 
 * @author Christoph Deppisch
 */
@Aspect
public class StoreMessageInterceptorAspect {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(StoreMessageInterceptorAspect.class);
    
    /** Target directory */
    private Resource debugDirectory = new FileSystemResource("logs/debug/messages/");
    
    /** Count messages */
    private static final AtomicInteger count = new AtomicInteger(1);
    
    @Pointcut("execution(org.springframework.integration.core.Message com.consol.citrus.message.MessageReceiver.receive*(..))")
    public void inReceivingMessage() {}

    @AfterReturning(pointcut="com.consol.citrus.aop.StoreMessageInterceptorAspect.inReceivingMessage()",
            returning="message")
    public void doInterceptMessage(Object message) {
        //in case of receive timeout message will be null, check that
        if(message != null) {
            storeMessage((Message<?>)message);
        }
    }
    
    /**
     * Save message to the file system.
     * @param receivedMessage
     * @throws CitrusRuntimeException
     */
    private void storeMessage(Message<?> receivedMessage) {
        Writer output = null;
        
        try {
            if(!debugDirectory.exists()) {
                debugDirectory.getFile().mkdirs();
            }
            
            int counter = StoreMessageInterceptorAspect.count.getAndIncrement();
            
            Resource file_body = debugDirectory.createRelative("message" + counter + ".body");
            Resource file_header = debugDirectory.createRelative("message" + counter + ".header");
            
            //write body message
            output = new BufferedWriter(new FileWriter(file_body.getFile()));
            output.write(receivedMessage.getPayload().toString());
            output.flush();
            output.close();

            //write header message
            output = new BufferedWriter(new FileWriter(file_header.getFile()));
            Map<String, Object> header = receivedMessage.getHeaders();

            for (Entry<String, Object> entry : header.entrySet()) {
                output.write(entry.getKey() + "=" + entry.getValue() + "\n");
            }
            
        } catch (IOException e) {
            throw new CitrusRuntimeException("Error while trying to save incoming message to filesystem", e);
        } finally {
            if(output != null) {
                try {
                    output.flush();
                    output.close();
                } catch (IOException e) {
                    log.error("Error while closing writer", e);
                }
            }
        }
    }
    
    /**
     * Resets the file counter.
     */
    public static final void resetFileCounter() {
        count.set(1);
    }

    /**
     * Sets the target directory on the file system.
     * @param debugDirectory the debugDirectory to set
     */
    public void setDebugDirectory(Resource debugDirectory) {
        this.debugDirectory = debugDirectory;
    }
}
