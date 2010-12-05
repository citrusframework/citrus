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

import com.consol.citrus.context.TestContext;

/**
 * Builds a validation context from information given on test action.
 *  
 * @author Christoph Deppisch
 */
public interface ValidationContextBuilder<T extends ValidationContext> {
    /**
     * Builds a validation context.
     * @param context the current test context.
     * @return the validation context implementation.
     */
    public T buildValidationContext(TestContext context);
    
    /**
     * Checks if this builder is suitable for construction such a validation context type.
     * @return the validation context type.
     */
    public boolean supportsValidationContextType(Class<? extends ValidationContext> validationContextType);
}
