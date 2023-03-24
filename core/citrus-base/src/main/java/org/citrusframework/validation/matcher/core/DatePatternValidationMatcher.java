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

package org.citrusframework.validation.matcher.core;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.matcher.ValidationMatcher;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.PatternSyntaxException;

/**
 * ValidationMatcher checking for valid date format.
 * 
 * @author Christian Wied
 */
public class DatePatternValidationMatcher implements ValidationMatcher {

    public void validate(String fieldName, String value, List<String> controlParameters, TestContext context) throws ValidationException {
		String control = controlParameters.get(0);
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
