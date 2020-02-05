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

package com.consol.citrus.script;

import com.consol.citrus.TestCase;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import java.io.*;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Class parsing a groovy script to create a test case instance.
 * @author Christoph Deppisch
 * @since 2009
 */
public final class GroovyTestCaseParser implements ApplicationContextAware {
   
    /** Application context */
    private ApplicationContext applicationContext;
    
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(GroovyTestCaseParser.class);

    /** Builds a test case using the application context and test context */
    public interface TestCaseBuilder {
        TestCase build(ApplicationContext applicationContext);
    }
    
    /**
     * Parse the groovy script.
     * @param groovyScript
     * @throws CitrusRuntimeException
     * @return
     */
    public TestCase parse(Resource groovyScript) {
        BufferedReader templateReader = null;
        BufferedReader bodyReader = null;
        
        try {
            GroovyClassLoader loader = AccessController.doPrivileged(new PrivilegedAction<GroovyClassLoader>() {
                public GroovyClassLoader run() {
                    ClassLoader parent = getClass().getClassLoader();
                    return new GroovyClassLoader(parent);
                }
            });

            StringBuilder script = new StringBuilder();
            bodyReader = new BufferedReader(new FileReader(groovyScript.getFile()));
            templateReader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("test_template.groovy")));
            String line;
            while ((line = templateReader.readLine()) != null) {
                if (!line.trim().equalsIgnoreCase("\"+++++ BODY +++++\"")) {
                    script.append(line);
                    script.append("\n");
                } else {
                    String bodyLine;                
                    while ((bodyLine = bodyReader.readLine()) != null) {
                        script.append(bodyLine);
                        script.append("\n");
                    }
                }
            }
            
            Class<?> groovyClass = loader.parseClass(script.toString());
    
            GroovyObject groovyObject;
            groovyObject = (GroovyObject) groovyClass.newInstance();
            
            if (groovyObject instanceof TestCaseBuilder) {
                return ((TestCaseBuilder)groovyObject).build(applicationContext);
            } else {
                throw new CitrusRuntimeException("Unable to parse groovy script. Script must implement TestCaseBuilder.");
            }
        } catch (InstantiationException e) {
            throw new CitrusRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new CitrusRuntimeException(e);
        } catch (CompilationFailedException e) {
            throw new CitrusRuntimeException(e);
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        } finally {
            try {
                if (templateReader != null) {
                    templateReader.close();
                }
            } catch (IOException e) {
                log.error("Failed to close stream for groovy template resource", e);
            }
            
            try {
                if (bodyReader != null) {
                    bodyReader.close();
                }
            } catch (IOException e) {
                log.error("Failed to close stream for groovy script resource", e);
            }
        }
    }

    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }
}
