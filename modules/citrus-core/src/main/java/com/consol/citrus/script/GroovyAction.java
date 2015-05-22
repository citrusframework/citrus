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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.script.TemplateBasedScriptBuilder;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Action executes groovy scripts either specified inline or from external file resource.
 * 
 * @author Christoph Deppisch
 * @since 2006
 */
public class GroovyAction extends AbstractTestAction {

    /** Inline groovy script */
    private String script;

    /** External script file resource path */
    private String scriptResourcePath;
    
    /** Static code snippet for basic groovy action implementation */
    private String scriptTemplatePath = "classpath:com/consol/citrus/script/script-template.groovy";
    
    /** Manage automatic groovy template usage */
    private boolean useScriptTemplate = true;
    
    /** Executes a script using the TestContext */
    public interface ScriptExecutor {
        void execute(TestContext context);
    }

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(GroovyAction.class);

    /**
     * Default constructor.
     */
    public GroovyAction() {
        setName("groovy");
    }

    @Override
    public void doExecute(TestContext context) {
        try {
            GroovyClassLoader loader = AccessController.doPrivileged(new PrivilegedAction<GroovyClassLoader>() {
                public GroovyClassLoader run() {
                    ClassLoader parent = getClass().getClassLoader();
                    return new GroovyClassLoader(parent);
                }
            });

            assertScriptProvided();

            String rawCode = StringUtils.hasText(script) ? script.trim() : FileUtils.readToString(FileUtils.getFileResource(scriptResourcePath, context));
            String code = context.replaceDynamicContentInString(rawCode.trim());

            // load groovy code
            Class<?> groovyClass = loader.parseClass(code);
            // Instantiate an object from groovy code
            GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();

            // only apply default script template in case we have feature enabled and code is not a class, too
            if (useScriptTemplate && groovyObject.getClass().getSimpleName().startsWith("script")) {
                // build new script with surrounding template
                code = TemplateBasedScriptBuilder.fromTemplateResource(FileUtils.getFileResource(scriptTemplatePath, context))
                                                 .withCode(code)
                                                 .build();

                groovyClass = loader.parseClass(code);
                groovyObject = (GroovyObject) groovyClass.newInstance();
            }

            if (log.isDebugEnabled()) {
                log.debug("Executing Groovy script:\n" + code);
            }

            // execute the Groovy script
            if (groovyObject instanceof ScriptExecutor) {
                ((ScriptExecutor) groovyObject).execute(context);
            } else {
                groovyObject.invokeMethod("run", new Object[] {});
            }

            log.info("Groovy script execution successfully");
        } catch (CitrusRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new CitrusRuntimeException(e);
        }
    }

    private void assertScriptProvided() {
        if (!StringUtils.hasText(script) && scriptResourcePath == null) {
            throw new CitrusRuntimeException("Neither inline script nor " +
                "external script resource is defined. Unable to execute groovy script.");
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
    public String getScriptResourcePath() {
        return scriptResourcePath;
    }

    /**
     * Set file resource.
     * @param fileResource the fileResource to set
     */
    public void setScriptResourcePath(String fileResource) {
        this.scriptResourcePath = fileResource;
    }

    /**
     * Set the script template resource.
     * @param scriptTemplate the scriptTemplate to set
     */
    public void setScriptTemplatePath(String scriptTemplate) {
        this.scriptTemplatePath = scriptTemplate;
    }

    /**
     * Prevent script template usage if false.
     * @param useScriptTemplate the useScriptTemplate to set
     */
    public void setUseScriptTemplate(boolean useScriptTemplate) {
        this.useScriptTemplate = useScriptTemplate;
    }

    /**
     * Gets the useScriptTemplate.
     * @return the useScriptTemplate
     */
    public boolean isUseScriptTemplate() {
        return useScriptTemplate;
    }

    /**
     * Gets the scriptTemplatePath.
     * @return the scriptTemplatePath
     */
    public String getScriptTemplatePath() {
        return scriptTemplatePath;
    }
}
