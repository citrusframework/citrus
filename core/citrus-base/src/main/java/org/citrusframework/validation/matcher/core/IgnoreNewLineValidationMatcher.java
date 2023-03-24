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

package org.citrusframework.validation.matcher.core;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.matcher.ValidationMatcher;

import java.util.List;

/**
 * ValidationMatcher ignores all new line characters in value and control value.
 * 
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class IgnoreNewLineValidationMatcher implements ValidationMatcher {

    public void validate(String fieldName, String value, List<String> controlParameters, TestContext context) throws ValidationException {
        String control = controlParameters.get(0);

        String normalizedValue = value.replaceAll("\\r(\\n)?", "\n").replaceAll("\\n", "");
        String normalizedControl = control.replaceAll("\\r(\\n)?", "\n").replaceAll("\\n", "");

        if (!normalizedValue.equalsIgnoreCase(normalizedControl)) {
            throw new ValidationException(this.getClass().getSimpleName()
                    + " failed for field '" + fieldName
                    + "'. Received value is '" + normalizedValue
                    + "', control value is '" + normalizedControl + "'.");
        }
    }
}
