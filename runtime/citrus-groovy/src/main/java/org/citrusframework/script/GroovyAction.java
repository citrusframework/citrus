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

package org.citrusframework.script;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.script.TemplateBasedScriptBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action executes groovy scripts either specified inline or from external file resource.
 *
 * @author Christoph Deppisch
 * @since 2006
 */
public class GroovyAction extends AbstractTestAction {

    /** Inline groovy script */
    private final String script;

    /** External script file resource path */
    private final String scriptResourcePath;

    /** Script template code */
    private final String scriptTemplate;

    /** Static code snippet for basic groovy action implementation */
    private final String scriptTemplatePath;

    /** Manage automatic groovy template usage */
    private final boolean useScriptTemplate;

    /** Executes a script using the TestContext */
    public interface ScriptExecutor {
        void execute(TestContext context);
    }

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(GroovyAction.class);

    /**
     * Default constructor.
     */
    public GroovyAction(Builder builder) {
        super("groovy", builder);

        this.script = builder.script;
        this.scriptResourcePath = builder.scriptResourcePath;
        this.scriptTemplate = builder.scriptTemplate;
        this.scriptTemplatePath = builder.scriptTemplatePath;
        this.useScriptTemplate = builder.useScriptTemplate;
    }

    @Override
    public void doExecute(TestContext context) {
        try {
            GroovyClassLoader loader = AccessController.doPrivileged(new PrivilegedAction<>() {
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
            GroovyObject groovyObject = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance();

            // only apply default script template in case we have feature enabled and code is not a class, too
            if (useScriptTemplate && groovyObject.getClass().getSimpleName().startsWith("Script")) {
                if (StringUtils.hasText(scriptTemplate)) {
                    // build new script with surrounding template
                    code = TemplateBasedScriptBuilder.fromTemplateScript(context.replaceDynamicContentInString(scriptTemplate))
                            .withCode(code)
                            .build();
                } else {
                    // build new script with surrounding template
                    code = TemplateBasedScriptBuilder.fromTemplateResource(FileUtils.getFileResource(scriptTemplatePath, context))
                            .withCode(code)
                            .build();
                }

                groovyClass = loader.parseClass(code);
                groovyObject = (GroovyObject) groovyClass.getDeclaredConstructor().newInstance();
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Executing Groovy script:\n" + code);
            }

            // execute the Groovy script
            if (groovyObject instanceof ScriptExecutor) {
                ((ScriptExecutor) groovyObject).execute(context);
            } else {
                groovyObject.invokeMethod("run", new Object[] {});
            }

            logger.info("Groovy script execution successful");
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

    /**
     * Gets the script template.
     * @return
     */
    public String getScriptTemplate() {
        return scriptTemplate;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<GroovyAction, Builder> {

        private String script;
        private String scriptResourcePath;
        private String scriptTemplate;
        private String scriptTemplatePath = "classpath:org/citrusframework/script/script-template.groovy";
        private boolean useScriptTemplate = true;

        public static Builder groovy() {
            return new Builder();
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         * @param script
         * @return
         */
        public static Builder groovy(String script) {
            Builder builder = new Builder();
            builder.script(script);
            return builder;
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         * @param scriptResource
         * @return
         */
        public static Builder groovy(Resource scriptResource) {
            Builder builder = new Builder();
            builder.script(scriptResource);
            return builder;
        }

        /**
         * Sets the Groovy script to execute.
         * @param script
         * @return
         */
        public Builder script(String script) {
            this.script = script;
            return this;
        }

        /**
         * Sets the Groovy script to execute.
         * @param scriptResource
         * @return
         */
        public Builder script(Resource scriptResource) {
            return script(scriptResource, FileUtils.getDefaultCharset());
        }

        /**
         * Sets the Groovy script to execute.
         * @param scriptResource
         * @param charset
         * @return
         */
        public Builder script(Resource scriptResource, Charset charset) {
            try {
                this.script = FileUtils.readToString(scriptResource, charset);
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read script resource file", e);
            }
            return this;
        }

        /**
         * Sets the Groovy script to execute.
         * @param scriptResourcePath
         * @return
         */
        public Builder scriptResourcePath(String scriptResourcePath) {
            this.scriptResourcePath = scriptResourcePath;
            return this;
        }

        /**
         * Use a script template from file path.
         * @param scriptTemplatePath the scriptTemplate to set
         */
        public Builder template(String scriptTemplatePath) {
            this.scriptTemplatePath = scriptTemplatePath;
            return this;
        }

        /**
         * Use a script template resource.
         * @param scriptTemplate the scriptTemplate to set
         */
        public Builder template(Resource scriptTemplate) {
            return template(scriptTemplate, FileUtils.getDefaultCharset());
        }

        /**
         * Use a script template resource.
         * @param scriptTemplate the scriptTemplate to set
         * @param charset
         */
        public Builder template(Resource scriptTemplate, Charset charset) {
            try {
                this.scriptTemplate = FileUtils.readToString(scriptTemplate, charset);
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read script template file", e);
            }
            return this;
        }

        /**
         * Prevent script template usage.
         */
        public Builder skipTemplate() {
            return useScriptTemplate(false);
        }

        public Builder useScriptTemplate(boolean useScriptTemplate) {
            this.useScriptTemplate = useScriptTemplate;
            return this;
        }

        @Override
        public GroovyAction build() {
            return new GroovyAction(this);
        }
    }
}
