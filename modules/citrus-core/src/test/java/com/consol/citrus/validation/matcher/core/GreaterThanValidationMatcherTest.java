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

package com.consol.citrus.validation.matcher.core;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;

public class GreaterThanValidationMatcherTest extends AbstractTestNGUnitTest {
    
	private LowerThanValidationMatcher matcher = new LowerThanValidationMatcher();
    
    @Test
    public void testValidateSuccess() {
        matcher.validate("field", "2", "3", context);
        matcher.validate("field", "-1", "1", context);
        matcher.validate("field", "-0.000000001", "0", context);
        matcher.validate("field", "0", "0.000000001", context);
    }
    
    @Test
    public void testValidateError() {
    	assertException("field", "NaN", "2");
    	assertException("field", "2", "NaN");
    	assertException("field", "2.0", "2.0");
    	assertException("field", "2.1", "2.0");
    }

    private void assertException(String fieldName, String value, String control) {
    	try {
    		matcher.validate(fieldName, value, control, context);
    		Assert.fail("Expected exception not thrown!");
    	} catch (ValidationException e) {
			Assert.assertTrue(e.getMessage().contains(fieldName));
			Assert.assertTrue(e.getMessage().contains(value));
			Assert.assertTrue(e.getMessage().contains(control));
		}
    }
}
