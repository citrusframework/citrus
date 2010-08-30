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

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.IOException;
import java.text.ParseException;

import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;

/**
 * Action executes groovy scripts either specified inline or from external file resource.
 * 
 * @author Christoph Deppisch
 * @since 2006
 */
public class GroovyAction extends AbstractTestAction {

    /** Inline groovy script */
    private String script;

    /** External script file resource */
    private Resource fileResource;
    
    /** Placeholder identifier for script body in template */
    private final String BODY_PLACEHOLDER = "@SCRIPTBODY@";

    /** Static code snippet for basic groovy action implementation */
    private Resource scriptTemplateResource = null;
    
    /** Manage automatic groovy template usage */
    private boolean useScriptTemplate = true;
    
    /** Executes a script using the TestContext */
    public interface ScriptExecutor {
        public void execute(TestContext context);
    }

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(GroovyAction.class);

    /**
     * @see com.consol.citrus.TestAction#execute(TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        try {
            ClassLoader parent = getClass().getClassLoader();
            GroovyClassLoader loader = new GroovyClassLoader(parent);
            
            String code;
            
            // get the script either from inline data or external file resource
            if (StringUtils.hasText(script)) {
            	code = context.replaceDynamicContentInString(script.trim());
            } else if (fileResource != null) {
            	code = context.replaceDynamicContentInString(FileUtils.readToString(fileResource)).trim();
            } else {
                throw new CitrusRuntimeException("Neither inline script nor " +
                		"external script resource is defined. Unable to execute groovy script.");
            }
            
            // load groovy code
            Class<?> groovyClass = loader.parseClass(code);
            // Instantiate an object from groovy code
            GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
            
            // only apply default script template in case we have feature enabled and code is not a class, too
            if (useScriptTemplate && groovyObject.getClass().getSimpleName().startsWith("script")) {
                // surround code with default script template code
                if (scriptTemplateResource == null) {
                    scriptTemplateResource = new ClassPathResource("script-template.groovy", GroovyAction.class);
                }
                
                String scriptTemplate = FileUtils.readToString(scriptTemplateResource.getInputStream());
                if (!scriptTemplate.contains(BODY_PLACEHOLDER)) {
                    throw new CitrusRuntimeException("Invalid script template - please define '" + BODY_PLACEHOLDER + "' placeholder");
                }
                
                String scriptHeader = scriptTemplate.substring(0, scriptTemplate.indexOf(BODY_PLACEHOLDER));
                String scriptTail = scriptTemplate.substring((scriptTemplate.indexOf(BODY_PLACEHOLDER) + BODY_PLACEHOLDER.length()));
                
                // build new script with surrounding template
                code = scriptHeader + code + scriptTail;
                groovyClass = loader.parseClass(code);
                groovyObject = (GroovyObject) groovyClass.newInstance();
            }
            
            if (log.isDebugEnabled()) {
                log.debug("Executing Groovy script:\n" + code);
            }
            
            // execute the Groovy script
            if(groovyObject instanceof ScriptExecutor) {
                ((ScriptExecutor)groovyObject).execute(context);
            } else {
                groovyObject.invokeMethod("run", new Object[] {});
            }
            
            log.info("Groovy script execution successfully");
        } catch (InstantiationException e) {
            throw new CitrusRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new CitrusRuntimeException(e);
        } catch (CompilationFailedException e) {
            throw new CitrusRuntimeException(e);
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        } catch (ParseException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Set the groovy script code.
     * @param script the script to set
     */
    public void setScript(String script) {
        this.script = script;
    }

    /**
     * Get the groovy script.
     * @return the script
     */
    public String getScript() {
        return script;
    }
    
    /**
     * Get the file resource.
     * @return the fileResource
     */
    public Resource getFileResource() {
        return fileResource;
    }

    /**
     * Set file resource.
     * @param fileResource the fileResource to set
     */
    public void setFileResource(Resource fileResource) {
        this.fileResource = fileResource;
    }

    /**
     * Set the script template resource.
     * @param scriptTemplate the scriptTemplate to set
     */
    public void setScriptTemplateResource(Resource scriptTemplate) {
        this.scriptTemplateResource = scriptTemplate;
    }

    /**
     * Prevent script template usage if false.
     * @param useScriptTemplate the useScriptTemplate to set
     */
    public void setUseScriptTemplate(boolean useScriptTemplate) {
        this.useScriptTemplate = useScriptTemplate;
    }
}
