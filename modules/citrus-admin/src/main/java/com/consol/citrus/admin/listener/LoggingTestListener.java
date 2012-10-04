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

package com.consol.citrus.admin.listener;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.springframework.web.context.ServletContextAware;

import com.consol.citrus.TestCase;
import com.consol.citrus.report.AbstractTestListener;

/**
 * Special test listener implementation delegates to logging listeners.
 * 
 * @author Christoph Deppisch
 * @since 1.3
 */
public class LoggingTestListener extends AbstractTestListener implements ServletContextAware {

    public static final String ATTRIBUTE = "com.consol.citrus.logging.test";
    
    /** Logging listeners */
    private List<LoggingListener> listeners = new ArrayList<LoggingListener>();
    
    @Override
    public void onTestStart(TestCase test) {
        for (LoggingListener listener : listeners) {
            listener.onLoggingMessage("Started test " + test.getName());
        }
    }
    
    @Override
    public void onTestSuccess(TestCase test) {
        for (LoggingListener listener : listeners) {
            listener.onLoggingMessage("Success " + test.getName());
        }
    }
    
    @Override
    public void onTestFailure(TestCase test, Throwable cause) {
        for (LoggingListener listener : listeners) {
            listener.onLoggingMessage("Failed " + test.getName() + " - " + cause.getMessage());
        }
    }
    
    /**
     * Adds a new listener instance for logging events.
     * @param listener
     */
    public void addLoggingListener(LoggingListener listener) {
        this.listeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    public void setServletContext(ServletContext servletContext) {
        servletContext.setAttribute(ATTRIBUTE, this);
    }
}
