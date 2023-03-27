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

package org.citrusframework.citrus.demo;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

/**
 * @author Christoph Deppisch
 */
public class HelloJmsDemo implements Runnable {
    private static ClassPathXmlApplicationContext ctx;

    private static Object contextLock = new Object();

    public void start() {
        synchronized (contextLock) {
            if (ctx == null) {
                TaskExecutor taskExecutor = new SimpleAsyncTaskExecutor("HelloJmsDemo");
                taskExecutor.execute(this);
            }
        }
    }

    public void stop() {
        synchronized (contextLock) {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    public void run() {
        HelloJmsDemo.initApplicationContext();
    }

    private static void initApplicationContext() {
        ctx = new ClassPathXmlApplicationContext("hello-jms-demo-context.xml", HelloJmsDemo.class);
    }

    public static void main(String[] args) throws InterruptedException {
        Thread t = new Thread(new HelloJmsDemo());
        t.start();
        t.join();
    }
}
