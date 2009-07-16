package com.consol.citrus.aop;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.consol.citrus.exceptions.TestSuiteException;
import com.consol.citrus.message.Message;

@Aspect
public class StoreMessageInterceptorAspect {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(StoreMessageInterceptorAspect.class);
    
    private Resource debugDirectory = new FileSystemResource("logs/debug/messages/");
    
    @Pointcut("execution(com.consol.citrus.message.Message com.consol.citrus.service.Service.receiveMessage())")
    public void inReceivingMessage() {}

    @AfterReturning(pointcut="com.consol.citrus.aop.StoreMessageInterceptorAspect.inReceivingMessage()",
            returning="message")
    public void doInterceptMessage(Object message) {
        //in case of receive timeout message will be null, check that
        if(message != null) {
            storeMessage((Message)message);
        }
    }
    
    private void storeMessage(Message receivedMessage) {
        Writer output = null;
        
        try {
            if(debugDirectory.exists() == false) {
                debugDirectory.getFile().mkdirs();
            }
            
            Resource file_body = debugDirectory.createRelative(receivedMessage.toString() + ".body");
            Resource file_header = debugDirectory.createRelative(receivedMessage.toString() + ".header");
            
            //write body message
            output = new BufferedWriter(new FileWriter(file_body.getFile()));
            output.write(receivedMessage.getMessagePayload());
            output.flush();
            output.close();

            //write header message
            output = new BufferedWriter(new FileWriter(file_header.getFile()));
            Map header = receivedMessage.getHeader();

            Iterator it = header.entrySet().iterator();
            while (it.hasNext()) {
                Entry entry = (Entry)it.next();
                output.write(entry.getKey().toString() + "=" + entry.getValue() + "\n");
            }
            
        } catch (IOException e) {
            throw new TestSuiteException("Error while trying to save incoming message to filesystem", e);
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
}
