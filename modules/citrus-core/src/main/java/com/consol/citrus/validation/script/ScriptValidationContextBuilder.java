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

import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.validation.context.ValidationContextBuilder;

/**
 * Validation context builder supposed to construct a basic script validation context with
 * validation script.
 * 
 * @author Christoph Deppisch
 */
public class ScriptValidationContextBuilder implements ValidationContextBuilder<ScriptValidationContext> {

    /**
     * Build a validation context with validation script.
     */
    public ScriptValidationContext buildValidationContext(TestAction action, TestContext context) {
        if (action instanceof ScriptValidationAware) {
            ScriptValidationAware scriptValidationAware = (ScriptValidationAware)action;
            
            if (scriptValidationAware.getValidationScript() != null) {
                return new ScriptValidationContext(scriptValidationAware.getValidationScript(), context);
            } else if (scriptValidationAware.getValidationScriptResource() != null) {
                return new ScriptValidationContext(scriptValidationAware.getValidationScriptResource(), context);
            }
        }
        
        // return empty script validation context
        return new ScriptValidationContext(context);
    }

}
