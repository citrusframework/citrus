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

package com.consol.citrus.util;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.IOException;

import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Class to provide utilities for Groovy.
 * 
 * @author Philipp Komninos
 * @since 30.08.2010
 */
public class GroovyUtils {
	
    /** Static code snippet for basic groovy markup builder script */
    private static Resource markupBuilderTemplateResource = null;
    
    /** Head and tail for markup builder script */
    private static String markupBuilderHead = null;
    private static String markupBuilderTail = null;
    
    /** Placeholder identifier for script body in template */
    private static final String BODY_PLACEHOLDER = "@SCRIPTBODY@";
    
    static {
        markupBuilderTemplateResource = new ClassPathResource("com/consol/citrus/script/markup-builder-template.groovy");
        
        String markupBuilderTemplate = null;
        try {
            markupBuilderTemplate = FileUtils.readToString(markupBuilderTemplateResource.getInputStream());
        } catch (IOException e) {
            throw new CitrusRuntimeException("Error loading Groovy markup builder template from file resource", e);
        }
        
        if (!markupBuilderTemplate.contains(BODY_PLACEHOLDER)) {
            throw new CitrusRuntimeException("Invalid script template - please define '" + BODY_PLACEHOLDER + "' placeholder");
        }
        
        markupBuilderHead = markupBuilderTemplate.substring(0, markupBuilderTemplate.indexOf(BODY_PLACEHOLDER));
        markupBuilderTail = markupBuilderTemplate.substring((markupBuilderTemplate.indexOf(BODY_PLACEHOLDER) + BODY_PLACEHOLDER.length()));
    }
    
    /**
     * Prevent instantiation.
     */
    private GroovyUtils() {
    }
    
	/**
     * Builds an automatic Groovy MarkupBuilder script with given script body.
     * 
     * @param scriptData
     * @return
     */
	public static String buildMarkupBuilderScript(String scriptData) {
		try {
			ClassLoader parent = GroovyUtils.class.getClassLoader(); 
			GroovyClassLoader loader = new GroovyClassLoader(parent);
			
			Class<?> groovyClass = loader.parseClass(markupBuilderHead + scriptData + markupBuilderTail);
			if(groovyClass == null) {
                throw new CitrusRuntimeException("Could not load groovy script!");    
            }
			
			GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
			return (String) groovyObject.invokeMethod("run", new Object[] {});
		} catch (CompilationFailedException e) {
			throw new CitrusRuntimeException(e);
		} catch (InstantiationException e) {
			throw new CitrusRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new CitrusRuntimeException(e);
		}
	}
}
