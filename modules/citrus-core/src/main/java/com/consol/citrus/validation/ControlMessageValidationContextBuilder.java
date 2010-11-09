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

package com.consol.citrus.validation;

import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.validation.context.ValidationContextBuilder;

/**
 * Validation context builder taking care of control message information
 * which is added to the validation context.
 * 
 * @author Christoph Deppisch
 */
public class ControlMessageValidationContextBuilder implements ValidationContextBuilder<ControlMessageValidationContext> {

    /**
     * Build the validation context.
     */
    public ControlMessageValidationContext buildValidationContext(TestAction action, TestContext context) {
        ControlMessageValidationContext validationContext = new ControlMessageValidationContext();
        
        addControlMessageToValidationContext(action, context, validationContext);
        
        return validationContext;
    }
    
    /**
     * Adds control message to the validation context if present. Subclasses may use this method to add
     * the control message to their implementation of {@link ControlMessageValidationContext}
     * 
     * @param action the current test action
     * @param context the current test context
     * @param validationContext the validation context object
     */
    public void addControlMessageToValidationContext(TestAction action, TestContext context, 
            ControlMessageValidationContext validationContext) {
        
        if (action instanceof ControlMessageValidationAware) {
            ControlMessageValidationAware controlMessageValidationAware = (ControlMessageValidationAware)action;
            
            validationContext.setControlMessage(controlMessageValidationAware.getControlMessage());
        }
    }
}
