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

package org.citrusframework.validation;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 3.0.0
 */
public class HamcrestHeaderValidator implements HeaderValidator {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(HamcrestHeaderValidator.class);

    @Override
    public void validateHeader(String headerName, Object receivedValue, Object controlValue, TestContext context, HeaderValidationContext validationContext) {
        if (controlValue instanceof Matcher) {
            if (!((Matcher<?>) controlValue).matches(receivedValue)) {
                throw new ValidationException(ValidationUtils.buildValueMismatchErrorMessage(
                        "Header validation failed: Values not matching for header '" + headerName + "'", controlValue, receivedValue));
            }
        } else {
            IsEqual<Object> equalMatcher = new IsEqual<>(controlValue);
            if (!equalMatcher.matches(receivedValue)) {
               throw new ValidationException(ValidationUtils.buildValueMismatchErrorMessage(
                        "Header validation failed: Values not equal for header '" + headerName + "'", controlValue, receivedValue));
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Header validation: " + headerName + "='" + controlValue + "': OK.");
        }
    }

    @Override
    public boolean supports(String headerName, Class<?> type) {
        return type != null && Matcher.class.isAssignableFrom(type);
    }
}
