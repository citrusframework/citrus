/*
 * Copyright 2006-2018 the original author or authors.
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

import com.consol.citrus.context.TestContext;
import com.consol.citrus.validation.context.HeaderValidationContext;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public interface HeaderValidator {

    /**
     * Filter supported headers by name and value type
     * @param headerName
     * @param type
     * @return
     */
    boolean supports(String headerName, Class<?> type);

    /**
     * Validate header values with received value and control value.
     * @param name
     * @param received
     * @param control
     * @param context
     * @param validationContext
     */
    void validateHeader(String name, Object received, Object control, TestContext context, HeaderValidationContext validationContext);
}
