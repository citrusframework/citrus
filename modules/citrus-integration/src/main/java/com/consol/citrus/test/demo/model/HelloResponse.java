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

package com.consol.citrus.test.demo.model;

/**
 * @author Christoph Deppisch
 */
public class HelloResponse {
    private String messageId;
    private String correlationId;
    private String user;
    private String text;
    
    private String exception;
    
    /**
     * @return the messageId
     */
    public String getMessageId() {
        return messageId;
    }
    
    /**
     * @param messageId the messageId to set
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    /**
     * @return the correlationId
     */
    public String getCorrelationId() {
        return correlationId;
    }
    
    /**
     * @param correlationId the correlationId to set
     */
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
    
    /**
     * @return the text
     */
    public String getText() {
        return text;
    }
    
    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }
    
    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }
    
    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }
    
    /**
     * @param exception the exception to set
     */
    public void setException(String exception) {
        this.exception = exception;
    }
    
    /**
     * @return the exception
     */
    public String getException() {
        return exception;
    }
    
}
