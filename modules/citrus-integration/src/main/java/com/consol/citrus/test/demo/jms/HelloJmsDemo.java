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

package com.consol.citrus.test.demo.jms;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

/**
 * @author Christoph Deppisch
 */
public class HelloJmsDemo {
    private static ClassPathXmlApplicationContext ctx;
    
    private static Object contextLock = new Object();
    
    public void start() {
        synchronized (contextLock) {
            if(ctx == null) {
                TaskExecutor taskExecutor = new SimpleAsyncTaskExecutor("HelloJmsDemo");
                
                taskExecutor.execute(new Runnable() {
                    public void run() {
                        ctx = new ClassPathXmlApplicationContext("jms-demo-context.xml", HelloJmsDemo.class);
                    }
                });
            }
        }
    }
    
    public void stop() {
        synchronized (contextLock) {
            if(ctx != null) {
                ctx.close();
            }
        }
    }
}
