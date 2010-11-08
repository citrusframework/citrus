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
import java.text.ParseException;

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
    
    /** The current test context */
    private TestContext context;

    /**
     * Constructor just using test context as field.
     * @param context
     */
    public ScriptValidationContext(TestContext context) {
        this.context = context;
    }
    
    /**
     * Constructor using validation script resource.
     * @param validationScriptResource
     * @param context
     */
    public ScriptValidationContext(Resource validationScriptResource, TestContext context) {
        super();
        this.validationScriptResource = validationScriptResource;
        this.context = context;
    }
    
    /**
     * Constructor using validation script.
     * @param validationScript
     * @param context
     */
    public ScriptValidationContext(String validationScript, TestContext context) {
        super();
        this.validationScript = validationScript;
        this.context = context;
    }

    /**
     * @return the validationScript
     * @throws CitrusRuntimeException
     */
    public String getValidationScript() throws CitrusRuntimeException {
        try {
            if (validationScriptResource != null) {
                return context.replaceDynamicContentInString(FileUtils.readToString(validationScriptResource));
            } else if (validationScript != null) {
                return context.replaceDynamicContentInString(validationScript);
            } else {
                return null; //should never happen
            }
        } catch (ParseException e) {
            throw new CitrusRuntimeException("Failed to replace variables in validation script", e);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to load validation script resource", e);
        }
    }
}
