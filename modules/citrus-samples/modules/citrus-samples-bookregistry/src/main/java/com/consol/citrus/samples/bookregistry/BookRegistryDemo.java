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

package com.consol.citrus.samples.bookregistry;

import com.consol.citrus.samples.CitrusSamplesDemo;

/**
 * @author Christoph Deppisch
 */
public class BookRegistryDemo extends CitrusSamplesDemo {
    
    @Override
    protected String getDemoApplicationConfigLocation() {
        return "bookRegistryDemo.xml";
    }
    
    @Override
    protected Class<? extends CitrusSamplesDemo> getDemoClass() {
        return BookRegistryDemo.class;
    }
    
    /**
     * Main CLI method for running sample demo.
     * @param args, cli arguments
     */
    public static void main(String[] args) {
        BookRegistryDemo demo = new BookRegistryDemo();
        demo.start();
    }
}
