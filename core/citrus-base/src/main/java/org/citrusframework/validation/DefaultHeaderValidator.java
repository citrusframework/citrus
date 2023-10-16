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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.matcher.ValidationMatcherUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class DefaultHeaderValidator implements HeaderValidator {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(DefaultHeaderValidator.class);

    /** Set of default header validators located via resource path lookup */
    private static final Map<String, HeaderValidator> DEFAULT_VALIDATORS = HeaderValidator.lookup();

    @Override
    public void validateHeader(String headerName, Object receivedValue, Object controlValue, TestContext context, HeaderValidationContext validationContext) {
        Optional<HeaderValidator> validator = getHeaderValidator(headerName, controlValue, context);
        if (validator.isPresent()) {
            validator.get().validateHeader(headerName, receivedValue, controlValue, context, validationContext);
            return;
        }

        String expectedValue = Optional.ofNullable(controlValue)
                .map(value -> context.getTypeConverter().convertIfNecessary(value, String.class))
                .map(context::replaceDynamicContentInString)
                .orElse("");

        if (receivedValue != null) {
            String receivedValueString = context.getTypeConverter().convertIfNecessary(receivedValue, String.class);
            if (ValidationMatcherUtils.isValidationMatcherExpression(expectedValue)) {
                ValidationMatcherUtils.resolveValidationMatcher(headerName, receivedValueString,
                        expectedValue, context);
                return;
            }

            if (!receivedValueString.equals(expectedValue)) {
                throw new ValidationException("Values not equal for header element '"
                        + headerName + "', expected '"
                        + expectedValue + "' but was '"
                        + receivedValue + "'");
            }
        } else if (StringUtils.hasText(expectedValue)) {
                throw new ValidationException("Values not equal for header element '"
                        + headerName + "', expected '"
                        + expectedValue + "' but was '"
                        + null + "'");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Validating header element: " + headerName + "='" + expectedValue + "': OK.");
        }
    }

    @Override
    public boolean supports(String headerName, Class<?> type) {
        return type == null || type.isInstance(String.class) || type.isPrimitive();
    }

    /**
     * Combines header validators from multiple sources. Includes validators coming from reference resolver
     * and resource path lookup are added.
     *
     * Then pick validator that explicitly supports the given header name or control value and return as optional.
     * @param headerName
     * @param controlValue
     * @param context
     * @return
     */
    private static Optional<HeaderValidator> getHeaderValidator(String headerName, Object controlValue, TestContext context) {
        // add validators from resource path lookup
        Map<String, HeaderValidator> allValidators = new HashMap<>(DEFAULT_VALIDATORS);

        // add validators in reference resolver
        allValidators.putAll(context.getReferenceResolver().resolveAll(HeaderValidator.class));

        return allValidators.values().stream()
                .filter(validator -> !(validator instanceof DefaultHeaderValidator))
                .filter(validator -> validator.supports(headerName, Optional.ofNullable(controlValue).map(Object::getClass).orElse(null)))
                .findFirst();
    }
}
