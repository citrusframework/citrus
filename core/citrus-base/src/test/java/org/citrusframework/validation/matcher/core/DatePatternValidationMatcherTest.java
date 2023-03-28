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

import java.util.Arrays;
import java.util.List;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.ValidationException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DatePatternValidationMatcherTest extends UnitTestSupport {

	private DatePatternValidationMatcher matcher = new DatePatternValidationMatcher();

    @Test
    public void testValidateSuccess() {
    	matcher.validate("field", "2011-10-10", Arrays.asList("yyyy-MM-dd"), context);
        matcher.validate("field", "10.10.2011", Arrays.asList("dd.MM.yyyy"), context);
        matcher.validate("field", "2011-01-01T01:02:03", Arrays.asList("yyyy-MM-dd'T'HH:mm:ss"), context);
    }

    @Test
    public void testValidateError() {
    	assertException("field", "201110-10", Arrays.asList("yy-MM-dd"));
    }

    private void assertException(String fieldName, String value, List<String> control) {
    	try {
    		matcher.validate(fieldName, value, control, context);
    		Assert.fail("Expected exception not thrown!");
    	} catch (ValidationException e) {
			Assert.assertTrue(e.getMessage().contains(fieldName));
			Assert.assertTrue(e.getMessage().contains(value));
			Assert.assertTrue(e.getMessage().contains(control.get(0)));
		}
    }
}
