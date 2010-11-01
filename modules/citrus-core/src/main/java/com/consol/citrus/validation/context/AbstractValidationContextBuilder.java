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

package com.consol.citrus.validation.context;

import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.validation.ControlMessageValidationAware;

/**
 * Abstract validation context builder taking care of control message information
 * which is added to the validation context.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractValidationContextBuilder<T extends AbstractValidationContext> implements ValidationContextBuilder<T> {

    /**
     * Build the validation context.
     */
    public T buildValidationContext(TestAction action, TestContext context) {
        T validationContext = prepareValidationContext(action, context); 
        
        if (action instanceof ControlMessageValidationAware) {
            ControlMessageValidationAware controlMessageValidationAware = (ControlMessageValidationAware)action;
            
            validationContext.setControlMessage(controlMessageValidationAware.getControlMessage());
        } else {
            throw new CitrusRuntimeException("Test action type '" + action.getClass().getName() +
                    "' is not able to handle script validation - need validation script information");
        }
        
        return validationContext;
    }
    
    /**
     * Prepare the validation context. Hook for subclasses to add specific information 
     * to the validation context.
     * 
     * @param action the test action.
     * @param context the current test context.
     * @return
     */
    public abstract T prepareValidationContext(TestAction action, TestContext context);
}
