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

package com.consol.citrus.validation.matcher;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;

/**
 * General validation matcher interface.
 * 
 * @author Christian Wied
 */
public interface ValidationMatcher {

    /**
     * Method called on validation.
     * 
     * @param fieldName the fieldName for logging purpose.
     * @param value the value to be validated.
     * @param control the control value.
     * @param context
     * @throws ValidationException when validation fails
     */
    void validate(String fieldName, String value, String control, TestContext context) throws ValidationException;
}
