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

import org.springframework.core.io.Resource;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.context.ValidationContextBuilder;

/**
 * Validation context builder supposed to construct a basic script validation context with
 * validation script.
 * 
 * @author Christoph Deppisch
 */
public class ScriptValidationContextBuilder implements ValidationContextBuilder<ScriptValidationContext> {

    /** Type indicating which type of script validator to use (e.g. groovy) */
    private String scriptType = "";
    
    /** Validation script for message validation */ 
    private String validationScript;
    
    /** Validation script resource */
    private Resource validationScriptResource;
    
    /**
     * Build a validation context with validation script.
     */
    public ScriptValidationContext buildValidationContext(TestContext context) {
        if (validationScript != null) {
            return new ScriptValidationContext(validationScript, scriptType);
        } else if (validationScriptResource != null) {
            return new ScriptValidationContext(validationScriptResource, scriptType);
        }
        
        // return empty script validation context
        return new ScriptValidationContext(scriptType);
    }

    /**
     * Checks the support for this validation context type.
     */
    public boolean supportsValidationContextType(Class<? extends ValidationContext> validationContextType) {
        return validationContextType.equals(ScriptValidationContext.class);
    }
    
    /**
     * Set the validation-script.
     * @param validationScript the validationScript to set
     */
    public void setValidationScript(String validationScript){
        this.validationScript = validationScript;
    }

    /**
     * Set the validation-script as resource
     * @param validationScriptResource the validationScriptResource to set
     */
    public void setValidationScriptResource(Resource validationScriptResource) {
        this.validationScriptResource = validationScriptResource;
    }

    /**
     * Sets the type of script used in this context.
     * @param scriptType the scriptType to set
     */
    public void setScriptType(String scriptType) {
        this.scriptType = scriptType;
    }

    /**
     * Gets the script type for this context.
     * @return the scriptType
     */
    public String getScriptType() {
        return scriptType;
    }
}
