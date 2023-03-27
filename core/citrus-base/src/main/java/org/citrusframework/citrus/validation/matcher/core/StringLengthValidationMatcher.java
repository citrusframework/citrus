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

package org.citrusframework.citrus.validation.matcher.core;

import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.exceptions.ValidationException;
import org.citrusframework.citrus.validation.matcher.ValidationMatcher;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * ValidationMatcher checks string length of given field.
 * 
 * @author Christoph Deppisch
 */
public class StringLengthValidationMatcher implements ValidationMatcher {

    public void validate(String fieldName, String value, List<String> controlParameters, TestContext context) throws ValidationException {
        try {
            int control = Integer.valueOf(StringUtils.trimWhitespace(controlParameters.get(0)));

            if (!(value.length() == control)) {
                throw new ValidationException(this.getClass().getSimpleName()
                        + " failed for field '" + fieldName
                        + "'. Received value '" + value
                        + "' should match string length '" + control + "'.");
            }
        } catch (NumberFormatException e) {
            throw new ValidationException(this.getClass().getSimpleName()
                    + " failed for field '" + fieldName
                    + "'. Invalid matcher argument '" + controlParameters.get(0) + "'. Must be a number");
        }
    }
}
