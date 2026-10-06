/*
 * Copyright the original author or authors.
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

package org.citrusframework.validation.assertj;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.HeaderValidator;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.ValidationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Header validator for control values given as AssertJ {@link org.assertj.core.api.Condition} or {@link AssertJCheck}.
 */
public class AssertJHeaderValidator implements HeaderValidator {

    private static final Logger logger = LoggerFactory.getLogger(AssertJHeaderValidator.class);

    @Override
    public void validateHeader(String headerName, Object receivedValue, Object controlValue, TestContext context, HeaderValidationContext validationContext) {
        try {
            AssertJCheck.from(controlValue).check(receivedValue, context);
        } catch (AssertionError e) {
            validationContext.updateStatus(ValidationStatus.FAILED);
            throw new ValidationException("Header validation failed for header '" + headerName + "': " + e.getMessage(), e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Header validation: {}='{}': OK", headerName, controlValue);
        }
        validationContext.updateStatus(ValidationStatus.PASSED);
    }

    @Override
    public boolean supports(String headerName, Class<?> type) {
        return AssertJCheck.isSupported(type);
    }
}
