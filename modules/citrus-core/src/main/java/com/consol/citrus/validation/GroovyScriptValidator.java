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

package com.consol.citrus.validation;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.IOException;

import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.core.Message;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;

/**
 * @author Christoph Deppisch
 */
public class GroovyScriptValidator implements ScriptValidator {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(GroovyScriptValidator.class);
    
    /** Static code snippet for groovy xml slurper script */
    private static Resource xmlSlurperTemplateResource = null;
    
    /** Head and tail for xml slurper script */
    private static String xmlSlurperHead = null;
    private static String xmlSlurperTail = null;
    
    /** Placeholder identifier for script body in template */
    private static final String BODY_PLACEHOLDER = "@SCRIPTBODY@";
    
    static {
        xmlSlurperTemplateResource = new ClassPathResource("com/consol/citrus/validation/xml-slurper-template.groovy");
        
        String xmlSlurperTemplate = null;
        try {
            xmlSlurperTemplate = FileUtils.readToString(xmlSlurperTemplateResource.getInputStream());
        } catch (IOException e) {
            throw new CitrusRuntimeException("Error loading Groovy xml slurper template from file resource", e);
        }
        
        if (!xmlSlurperTemplate.contains(BODY_PLACEHOLDER)) {
            throw new CitrusRuntimeException("Invalid script template - please define '" + BODY_PLACEHOLDER + "' placeholder");
        }
        
        xmlSlurperHead = xmlSlurperTemplate.substring(0, xmlSlurperTemplate.indexOf(BODY_PLACEHOLDER));
        xmlSlurperTail = xmlSlurperTemplate.substring((xmlSlurperTemplate.indexOf(BODY_PLACEHOLDER) + BODY_PLACEHOLDER.length()));
    }
    
    public void validate(Message<?> receivedMessage, 
            TestContext context, String validationScript) {
        
        log.info("Start groovy message validation");
        
        try {
            GroovyClassLoader loader = new GroovyClassLoader(GroovyScriptValidator.class.getClassLoader());
            Class<?> groovyClass = loader.parseClass(xmlSlurperHead + validationScript + xmlSlurperTail);
            
            if (groovyClass == null) {
                throw new CitrusRuntimeException("Failed to load groovy validation script resource");
            }
            
            GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
            ((ValidationScriptExecutor) groovyObject).validate(receivedMessage, context);
            
            log.info("Groovy message validation finished successfully: All values OK");
        } catch (CompilationFailedException e) {
            throw new CitrusRuntimeException(e);
        } catch (InstantiationException e) {
            throw new CitrusRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new CitrusRuntimeException(e);
        }
    }
    
    /**
     * Executes a validation-script
     */
     public static interface ValidationScriptExecutor {
         public void validate(Message<?> receivedMessage, TestContext context);
     }
}
