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

package org.citrusframework.validation.script;

import java.io.IOException;
import java.nio.charset.Charset;

import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.script.ScriptTypes;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.validation.context.DefaultValidationContext;
import org.citrusframework.validation.context.ValidationContext;

/**
 * Basic script validation context providing the validation code either from file resource or
 * from direct script string.
 *
 * @author Christoph Deppisch
 */
public class ScriptValidationContext extends DefaultValidationContext {

    /** Validation script as file resource path */
    private final String validationScriptResourcePath;

    /** Charset applied to script resource */
    private final String validationScriptResourceCharset;

    /** Validation script code */
    private final String validationScript;

    /** Type indicating which type of script we use (e.g. groovy, scala etc.) */
    private final String scriptType;

    /**
     * Default constructor.
     */
    public ScriptValidationContext() {
        this(Builder.groovy());
    }

    /**
     * Constructor using type field.
     * @param scriptType
     */
    public ScriptValidationContext(String scriptType) {
        this(new Builder()
                .scriptType(scriptType));
    }

    public ScriptValidationContext(Builder builder) {
        this.validationScript = builder.validationScript;
        this.validationScriptResourcePath = builder.validationScriptResourcePath;
        this.validationScriptResourceCharset = builder.validationScriptResourceCharset;
        this.scriptType = builder.scriptType;
    }

    /**
     * Constructs the actual validation script either from data or external resource.
     * @param context the current TestContext.
     * @return the validationScript
     * @throws CitrusRuntimeException
     */
    public String getValidationScript(TestContext context) {
        try {
            if (validationScriptResourcePath != null) {
                return context.replaceDynamicContentInString(FileUtils.readToString(FileUtils.getFileResource(validationScriptResourcePath, context),
                        Charset.forName(context.replaceDynamicContentInString(validationScriptResourceCharset))));
            } else if (validationScript != null) {
                return context.replaceDynamicContentInString(validationScript);
            } else {
                return "";
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to load validation script resource", e);
        }
    }

    /**
     * Fluent builder
     */
    public static final class Builder implements ValidationContext.Builder<ScriptValidationContext, Builder> {

        private String validationScriptResourcePath;
        private String validationScriptResourceCharset = CitrusSettings.CITRUS_FILE_ENCODING;
        private String validationScript = "";
        private String scriptType = ScriptTypes.GROOVY;

        public static Builder groovy() {
            return new Builder();
        }

        /**
         * Adds script validation.
         *
         * @param validationScript
         * @return
         */
        public Builder script(final String validationScript) {
            this.validationScript = validationScript;
            return this;
        }

        /**
         * Reads validation script file resource and sets content as validation script.
         *
         * @param scriptResource
         * @return
         */
        public Builder script(final Resource scriptResource) {
            return script(scriptResource, FileUtils.getDefaultCharset());
        }

        /**
         * Reads validation script file resource and sets content as validation script.
         *
         * @param scriptResource
         * @param charset
         * @return
         */
        public Builder script(final Resource scriptResource, final Charset charset) {
            try {
                script(FileUtils.readToString(scriptResource, charset));
            } catch (final IOException e) {
                throw new CitrusRuntimeException("Failed to read script resource file", e);
            }

            return this;
        }

        /**
         * Adds script validation file resource.
         *
         * @param fileResourcePath
         * @return
         */
        public Builder scriptResource(final String fileResourcePath) {
            this.validationScriptResourcePath = fileResourcePath;
            return this;
        }

        /**
         * Adds charset of script validation file resource.
         *
         * @param charsetName
         * @return
         */
        public Builder scriptResourceCharset(final String charsetName) {
            this.validationScriptResourceCharset = charsetName;
            return this;
        }

        /**
         * Adds custom validation script type.
         *
         * @param type
         * @return
         */
        public Builder scriptType(final String type) {
            this.scriptType = type;
            return this;
        }

        @Override
        public ScriptValidationContext build() {
            return new ScriptValidationContext(this);
        }
    }

    /**
     * Gets the type of script used in this validation context.
     * @return the scriptType
     */
    public String getScriptType() {
        return scriptType;
    }

    /**
     * Gets the validationScriptResource.
     * @return the validationScriptResource
     */
    public String getValidationScriptResourcePath() {
        return validationScriptResourcePath;
    }

    /**
     * Gets the validationScript.
     * @return the validationScript
     */
    public String getValidationScript() {
        return validationScript;
    }

    /**
     * Gets the validationScriptResourceCharset.
     * @return
     */
    public String getValidationScriptResourceCharset() {
        return validationScriptResourceCharset;
    }

}
