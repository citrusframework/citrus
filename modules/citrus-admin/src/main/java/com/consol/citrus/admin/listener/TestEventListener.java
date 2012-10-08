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

import com.consol.citrus.TestAction;
import com.consol.citrus.TestCase;
import com.consol.citrus.report.*;

/**
 * Special test event listener implementation implements several test listener interfaces 
 * and takes care on all test related events. Delegates all incoming test
 * events to logging listeners (such as web socket servlet for pushing events to clients).
 * 
 * @author Christoph Deppisch
 * @since 1.3
 */
public class TestEventListener implements TestListener, TestSuiteListener, TestActionListener, ServletContextAware {

    /** Servlet context attribute name */
    public static final String ATTRIBUTE = "com.consol.citrus.test.listener";
    
    /** Logging listeners */
    private List<LoggingListener> listeners = new ArrayList<LoggingListener>();
    
    /**
     * {@inheritDoc}
     */
    public void onTestStart(TestCase test) {
        for (LoggingListener listener : listeners) {
            listener.onLoggingMessage("Test " + test.getName() + " started");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void onTestSuccess(TestCase test) {
        for (LoggingListener listener : listeners) {
            listener.onLoggingMessage("Test " + test.getName() + " success");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onTestFailure(TestCase test, Throwable cause) {
        for (LoggingListener listener : listeners) {
            listener.onLoggingMessage("Test " + test.getName() + " failed - " + cause.getMessage());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void onTestFinish(TestCase test) {
        for (LoggingListener listener : listeners) {
            listener.onLoggingMessage("Test " + test.getName() + " finished");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onTestSkipped(TestCase test) {
        for (LoggingListener listener : listeners) {
            listener.onLoggingMessage("Test " + test.getName() + " skipped");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void onTestActionStart(TestAction testAction) {
        for (LoggingListener listener : listeners) {
            listener.onLoggingMessage(testAction.getName() + ":started");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void onTestActionSuccess(TestAction testAction) {
        for (LoggingListener listener : listeners) {
            listener.onLoggingMessage(testAction.getName() + ":success");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void onTestActionFailure(TestAction testAction, Throwable cause) {
        for (LoggingListener listener : listeners) {
            listener.onLoggingMessage(testAction.getName() + ":failed - " + cause.getMessage());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void onTestActionFinish(TestAction testAction) {
        for (LoggingListener listener : listeners) {
            listener.onLoggingMessage(testAction.getName() + ":finished");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onTestActionSkipped(TestAction testAction) {
        for (LoggingListener listener : listeners) {
            listener.onLoggingMessage(testAction.getName() + ":skipped");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onStart() {
        for (LoggingListener listener : listeners) {
            listener.onLoggingMessage("Test run startup");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onStartSuccess() {
        for (LoggingListener listener : listeners) {
            listener.onLoggingMessage("Test run startup done");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onStartFailure(Throwable cause) {
        for (LoggingListener listener : listeners) {
            listener.onLoggingMessage("Test run startup failed - " + cause.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onFinish() {
        for (LoggingListener listener : listeners) {
            listener.onLoggingMessage("Test run finished");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onFinishSuccess() {
        for (LoggingListener listener : listeners) {
            listener.onLoggingMessage("Test run finished successfully");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onFinishFailure(Throwable cause) {
        for (LoggingListener listener : listeners) {
            listener.onLoggingMessage("Test run failed - " + cause.getMessage());
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
