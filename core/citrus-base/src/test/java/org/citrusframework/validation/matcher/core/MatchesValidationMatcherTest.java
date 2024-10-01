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

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.ValidationException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Collections.singletonList;

public class MatchesValidationMatcherTest extends UnitTestSupport {

	private final MatchesValidationMatcher matcher = new MatchesValidationMatcher();

    @Test
    public void testValidateSuccess() {
    	matcher.validate("field", "This is a test", List.of(".*"), context);
        matcher.validate("field", "This is a test", List.of("Thi.*"), context);
        matcher.validate("field", "This is a test", List.of(".*test"), context);
        matcher.validate("field", "This is a number: 01234", List.of("This is a number: [0-9]+"), context);
        matcher.validate("field", "This is a number: 01234/999", List.of("This is a number: [0-9]+/[0-9]{3}"), context);
        matcher.validate("field", "https://localhost:12345/", List.of("https://localhost:[0-9]+/"), context);
        matcher.validate("field", "aaaab", singletonList("a*b"), context);
    }

    @Test
    public void testValidateError() {
    	assertException("a", List.of("[^a]"));
    	assertException("aaaab", List.of("aaab*"));
    }

    private void assertException(String value, List<String> control) {
    	try {
    		matcher.validate("field", value, control, context);
    		Assert.fail("Expected exception not thrown!");
    	} catch (ValidationException e) {
			Assert.assertTrue(e.getMessage().contains("field"));
			Assert.assertTrue(e.getMessage().contains(value));
			Assert.assertTrue(e.getMessage().contains(control.get(0)));
		}
    }
}
