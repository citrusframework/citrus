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
    
    /**
     * Runs the demo
     */
    public void start() {
        t = new Thread(new Runnable() {
            public void run() {
                ctx = new ClassPathXmlApplicationContext(new String[] {getDemoApplicationConfigLocation()}, 
                        getDemoClass(), new ClassPathXmlApplicationContext("citrus-samples-common.xml", CitrusSamplesDemo.class));
            }
        });
        
        t.start();
        
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for demo to start", e);
        }
    }

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
