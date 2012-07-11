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

package com.consol.citrus.admin.service;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Singleton bean holding the application context for this Citrus project. Singleton in applciation context so all participating
 * beans can autowire this class and application context is only loaded once.
 * 
 * @author Christoph Deppisch
 */
public class AppContextHolder {
    
    /** Citrus application context */
    private ClassPathXmlApplicationContext applicationContext;
    
    /**
     * Gets the current application context. If not loaded before we initialize the 
     * context on the fly.
     * 
     * @return
     */
    public ClassPathXmlApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            applicationContext = loadApplicationContext();
        }
        
        return applicationContext;
    }

    /**
     * Loads the basic Citrus application context with all necessary parent context files.
     */
    private ClassPathXmlApplicationContext loadApplicationContext() {
        return new ClassPathXmlApplicationContext(new String[] { "classpath:com/consol/citrus/spring/root-application-ctx.xml", 
                "classpath:citrus-context.xml", 
                "classpath:com/consol/citrus/functions/citrus-function-ctx.xml",
                "classpath:com/consol/citrus/validation/citrus-validationmatcher-ctx.xml"});
    }

    /**
     * Stops and destroy this application context.
     */
    public void destroyApplicationContext() {
        if (applicationContext != null) {
            applicationContext.stop();
            applicationContext.destroy();
            applicationContext = null;
        }
    }
    
}
