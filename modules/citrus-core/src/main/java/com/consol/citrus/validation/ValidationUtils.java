/*
 * Copyright 2006-2012 the original author or authors.
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

/**
 * Utility class provides helper methods for validation work in Citrus.
 * 
 * @author Christoph Deppisch
 * @since 1.3
 */
public abstract class ValidationUtils {

    /**
     * Prevent instantiation.
     */
    private ValidationUtils() {
    }
    
    /**
     * Constructs proper error message with expected value and actual value.
     * @param baseMessage the base error message.
     * @param controlValue the expected value.
     * @param actualValue the actual value.
     * @return
     */
    public static String buildValueMismatchErrorMessage(String baseMessage, Object controlValue, Object actualValue) {
        return baseMessage + ", expected '" + controlValue + "' but was '" + actualValue + "'";
    }
}
