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

package com.consol.citrus.validation.script;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.script.ScriptTypes;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.context.ValidationContext;

import java.io.IOException;

/**
 * Basic script validation context providing the validation code either from file resource or 
 * from direct script string.
 * 
 * @author Christoph Deppisch
 */
public class ScriptValidationContext implements ValidationContext {
    /** Validation script as file resource path */
    private String validationScriptResourcePath;
    
    /** Validation script code */
    private String validationScript = "";
    
    /** Type indicating which type of script we use (e.g. groovy, scala etc.) */
    private String scriptType = ScriptTypes.GROOVY;

    /** The message type this context was built for */
    private final String messageType;
    
    /**
     * Default constructor.
     */
    public ScriptValidationContext(String messageType) {
        this.messageType = messageType;
    }
    
    /**
     * Constructor using type field.
     * @param scriptType
     * @param messageType
     */
    public ScriptValidationContext(String scriptType, String messageType) {
        this(messageType);
        this.scriptType = scriptType;
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
                return context.replaceDynamicContentInString(FileUtils.readToString(FileUtils.getFileResource(validationScriptResourcePath, context)));
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
     * Sets the validationScriptResource.
     * @param validationScriptResource the validationScriptResource to set
     */
    public void setValidationScriptResourcePath(String validationScriptResource) {
        this.validationScriptResourcePath = validationScriptResource;
    }

    /**
     * Gets the validationScript.
     * @return the validationScript
     */
    public String getValidationScript() {
        return validationScript;
    }

    /**
     * Sets the validationScript.
     * @param validationScript the validationScript to set
     */
    public void setValidationScript(String validationScript) {
        this.validationScript = validationScript;
    }

    /**
     * Sets the scriptType.
     * @param scriptType the scriptType to set
     */
    public void setScriptType(String scriptType) {
        this.scriptType = scriptType;
    }

    /**
     * Gets the message type.
     * @return
     */
    public String getMessageType() {
        return messageType;
    }
}
