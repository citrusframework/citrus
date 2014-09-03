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

package com.consol.citrus.admin.executor;

import com.consol.citrus.admin.websocket.WebSocketTestListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContextManager;

import com.consol.citrus.report.*;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;

/**
 * Singleton bean holding the application context for this Citrus project. Singleton in application context so all participating
 * beans can autowire this class and application context is only loaded once.
 * 
 * @author Christoph Deppisch
 * @since 1.3
 */
public class ApplicationContextHolder implements DisposableBean {
    
    /** Citrus application context */
    private ApplicationContext applicationContext;
    
    @Autowired
    private WebSocketTestListener webSocketTestEventListener;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(ApplicationContextHolder.class);
    
    /**
     * Returns the status of the application context. If the application context hasn't been loaded already or has been
     * loaded but afterwards destroyed then false is returned. Otherwise true is returned.
     *
     * @return true if already loaded, otherwise false
     */
    public boolean isApplicationContextLoaded() {
        return applicationContext != null;
    }

    /**
     * Loads the basic Citrus application context with all necessary parent context files.
     */
    public ApplicationContext loadApplicationContext() {
        if (applicationContext == null) {
            TestContextManager testContextManager = new TestContextManager(AbstractTestNGCitrusTest.class) {
                @Override
                public void prepareTestInstance(Object testInstance) throws Exception {
                    applicationContext = getTestContext().getApplicationContext();

                    // add special admin webapp test listeners
                    applicationContext.getBean(TestListeners.class).addTestListener(webSocketTestEventListener);
                    applicationContext.getBean(TestActionListeners.class).addTestActionListener(webSocketTestEventListener);
                    applicationContext.getBean(MessageListeners.class).addMessageListener(webSocketTestEventListener);
                }
            };

            try {
                testContextManager.prepareTestInstance(null);
            } catch (Exception e) {
                log.error("Failed to load application context", e);
            }
        }
        
        return applicationContext;
    }

    /**
     * Stops and destroy this application context.
     */
    public void destroyApplicationContext() {
        TestContextManager testContextManager = new TestContextManager(AbstractTestNGCitrusTest.class) {
            @Override
            public void prepareTestInstance(Object testInstance) throws Exception {
                getTestContext().markApplicationContextDirty(DirtiesContext.HierarchyMode.CURRENT_LEVEL);
            }
        };
        
        try {
            testContextManager.prepareTestInstance(null);
        } catch (Exception e) {
            log.error("Failed to stop application context", e);
        }
        
        applicationContext = null;
    }

    /**
     * Gets the application context.
     * @return
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void destroy() throws Exception {
        if (applicationContext != null) {
            destroyApplicationContext();
        }
    }
}
