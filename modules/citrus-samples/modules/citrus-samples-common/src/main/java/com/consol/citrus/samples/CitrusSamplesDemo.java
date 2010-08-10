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

package com.consol.citrus.samples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Christoph Deppisch
 * @since 2010
 */
public class CitrusSamplesDemo {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(CitrusSamplesDemo.class);
    
    private Thread t;
    
    private ClassPathXmlApplicationContext ctx;
    
    private static boolean running = false;
    
    private static Object startupMonitor = new Object();
    
    /**
     * Runs the demo by creating a Spring application context.
     */
    public void start() {
        synchronized (startupMonitor) {
            if(!running) {
                running = true;
            } else {
               return;
            }
            
            t = new Thread(new Runnable() {
                public void run() {
                    ctx = new ClassPathXmlApplicationContext(new String[] {getDemoApplicationConfigLocation()}, 
                            getDemoClass(), new ClassPathXmlApplicationContext("citrus-samples-common.xml", CitrusSamplesDemo.class));
                }
            });
            
            t.start();
        }
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for demo to start", e);
        }
    }

    /**
     * Stops the demo application context.
     */
    public void stop() {
        if(ctx != null) {
            ctx.close();
        }
        
        t = null;
    }

    /**
     * Override this method to provide a basis for 
     * the given config location paths
     * 
     * @return class
     */
    protected Class<? extends CitrusSamplesDemo> getDemoClass() {
        return CitrusSamplesDemo.class;
    }
    
    /**
     * Override this method to add 
     * specific application context file location
     * 
     * @return location path as String
     */
    protected String getDemoApplicationConfigLocation() {
        return null; 
    }
}
