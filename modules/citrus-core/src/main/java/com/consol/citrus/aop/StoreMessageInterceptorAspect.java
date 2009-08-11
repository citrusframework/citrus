package com.consol.citrus.aop;

import java.io.*;
import java.util.Iterator;
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

@Aspect
public class StoreMessageInterceptorAspect {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(StoreMessageInterceptorAspect.class);
    
    private Resource debugDirectory = new FileSystemResource("logs/debug/messages/");
    
    private static final AtomicInteger count = new AtomicInteger(1);
    
    @Pointcut("execution(org.springframework.integration.core.Message com.consol.citrus.message.MessageReceiver.receive*(..))")
    public void inReceivingMessage() {}

    @AfterReturning(pointcut="com.consol.citrus.aop.StoreMessageInterceptorAspect.inReceivingMessage()",
            returning="message")
    public void doInterceptMessage(Object message) {
        //in case of receive timeout message will be null, check that
        if(message != null) {
            storeMessage((Message)message);
        }
    }
    
    /**
     * @param receivedMessage
     * @throws CitrusRuntimeException
     */
    private void storeMessage(Message receivedMessage) {
        Writer output = null;
        
        try {
            if(debugDirectory.exists() == false) {
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
            Map header = receivedMessage.getHeaders();

            Iterator it = header.entrySet().iterator();
            while (it.hasNext()) {
                Entry entry = (Entry)it.next();
                output.write(entry.getKey().toString() + "=" + entry.getValue() + "\n");
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
    
    public static final void resetFileCounter() {
        count.set(1);
    }

    /**
     * @param debugDirectory the debugDirectory to set
     */
    public void setDebugDirectory(Resource debugDirectory) {
        this.debugDirectory = debugDirectory;
    }
}
