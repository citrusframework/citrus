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

package com.consol.citrus.validation.callback;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.Message;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * Validation callback automatically extracts message payload and headers so we work with
 * Java code for validation.
 *  
 * @author Christoph Deppisch
 */
public abstract class AbstractValidationCallback<T> implements ValidationCallback, ApplicationContextAware {

    /** Spring application context injected before validation callback is called */
    protected ApplicationContext applicationContext;
    
    @Override
    public void validate(Message message, TestContext context) {
        validate((T) message.getPayload(), message.getHeaders(), context);
    }
    
    /**
     * Subclasses do override this method for validation purpose.
     * @param payload the message payload object.
     * @param headers the message headers
     * @param context the current test context
     */
    public abstract void validate(T payload, Map<String, Object> headers, TestContext context);
    
    /**
     * Sets the applicationContext.
     * @param applicationContext the applicationContext to set
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
