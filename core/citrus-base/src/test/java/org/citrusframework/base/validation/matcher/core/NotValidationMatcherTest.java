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

package org.citrusframework.base.validation.matcher.core;

import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class NotValidationMatcherTest extends UnitTestSupport {

    private final NotValidationMatcher matcher = new NotValidationMatcher();

    @Test
    public void shouldPassWhenInnerMatcherFails() {
        // inner: startsWith('foo') — value is "bar", so inner fails → not() passes
        matcher.validate("field", "bar", List.of("startsWith('foo')"), context);
    }

    @Test
    public void shouldFailWhenInnerMatcherPasses() {
        try {
            // inner: startsWith('hel') — value is "hello", so inner passes → not() fails
            matcher.validate("field", "hello", List.of("startsWith('hel')"), context);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("not()"));
            assertTrue(e.getMessage().contains("startsWith('hel')"));
            assertTrue(e.getMessage().contains("hello"));
        }
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void shouldRejectZeroSubExpressions() {
        matcher.validate("field", "value", List.of(), context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void shouldRejectMoreThanOneSubExpression() {
        matcher.validate("field", "value", List.of("startsWith('a')", "endsWith('b')"), context);
    }

    @Test
    public void shouldHandleNestedCombinator() {
        // not(allOf(startsWith('foo'), endsWith('bar'))) on value "fooXYZ"
        // inner allOf: startsWith('foo') passes, endsWith('bar') fails → allOf fails → not passes
        matcher.validate("field", "fooXYZ", List.of("allOf(startsWith('foo'), endsWith('bar'))"), context);
    }
}
