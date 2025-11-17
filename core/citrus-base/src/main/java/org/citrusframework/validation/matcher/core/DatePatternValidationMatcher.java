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

package org.citrusframework.validation.matcher.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.PatternSyntaxException;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.matcher.StringValidationMatcher;

/**
 * ValidationMatcher checking for valid date format.
 *
 */
public class DatePatternValidationMatcher implements StringValidationMatcher {

    public void validate(String fieldName, String value, String control, TestContext context) throws ValidationException {
    	SimpleDateFormat dateFormat;
    	try {
    		dateFormat = new SimpleDateFormat(control);
    	} catch (PatternSyntaxException e) {
    		throw new ValidationException(this.getClass().getSimpleName()
                    + " failed for field '" + fieldName + "' " +
                    ". Found invalid date format", e);
		}
    	try {
			dateFormat.parse(value);
		} catch (ParseException e) {
            throw new ValidationException(this.getClass().getSimpleName()
                    + " failed for field '" + fieldName + "'" +
                    		". Received invalid date format for value '" + value
                    + "', expected date format is '" + control + "'", e);
		}
    }
}
