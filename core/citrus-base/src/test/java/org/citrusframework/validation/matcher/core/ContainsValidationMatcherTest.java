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

public class ContainsValidationMatcherTest extends UnitTestSupport {

    private ContainsValidationMatcher matcher = new ContainsValidationMatcher();

    @Test
    public void testValidateSuccess() {
        matcher.validate("field", "This is a test", Arrays.asList("is a"), context);
        matcher.validate("field", "This is a test", Arrays.asList("This"), context);
        matcher.validate("field", "This is a test", Arrays.asList("test"), context);
        matcher.validate("field", "This is a 0815test", Arrays.asList("0815"), context);
        matcher.validate("field", "This is a test", Arrays.asList(" "), context);
    }

    @Test
    public void testValidateError() {
    	assertException("field", "This is a test", Arrays.asList("0815"));
    	assertException("field", null, Arrays.asList("control"));
    }

    private void assertException(String fieldName, String value, List<String> control) {
    	try {
    		matcher.validate(fieldName, value, control, context);
    		Assert.fail("Expected exception not thrown!");
    	} catch (ValidationException e) {
			Assert.assertTrue(e.getMessage().contains(fieldName));
			Assert.assertTrue(e.getMessage().contains(control.get(0)));

            if (value != null) {
			    Assert.assertTrue(e.getMessage().contains(value));
            } else {
                Assert.assertTrue(e.getMessage().contains("null"));
            }
		}
    }
}
