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

import java.io.IOException;

import org.springframework.core.io.Resource;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.context.ValidationContext;

/**
 * Basic script validation context providing the validation code either from file resource or 
 * from direct script string.
 * 
 * @author Christoph Deppisch
 */
public class ScriptValidationContext implements ValidationContext {
    /** Validation script as file resource*/
    private Resource validationScriptResource;
    
    /** Validation script code */
    private String validationScript = "";
    
    /** Type indicating which type of script we use (e.g. groovy, scala etc.) */
    private String scriptType;

    /**
     * Constructor just using test context as field.
     * @param scriptType
     * @param context
     */
    public ScriptValidationContext(String scriptType) {
        this.scriptType = scriptType;
    }
    
    /**
     * Constructor using validation script resource.
     * @param validationScriptResource
     * @param scriptType
     * @param context
     */
    public ScriptValidationContext(Resource validationScriptResource, String scriptType) {
        super();
        this.validationScriptResource = validationScriptResource;
        this.scriptType = scriptType;
    }
    
    /**
     * Constructor using validation script.
     * @param validationScript
     * @param scriptType
     * @param context
     */
    public ScriptValidationContext(String validationScript, String scriptType) {
        super();
        this.validationScript = validationScript;
        this.scriptType = scriptType;
    }

    /**
     * Constructs the actual validation script either from data or external resource.
     * @param context the current TestContext.
     * @return the validationScript
     * @throws CitrusRuntimeException
     */
    public String getValidationScript(TestContext context) throws CitrusRuntimeException {
        try {
            if (validationScriptResource != null) {
                return context.replaceDynamicContentInString(FileUtils.readToString(validationScriptResource));
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
}
