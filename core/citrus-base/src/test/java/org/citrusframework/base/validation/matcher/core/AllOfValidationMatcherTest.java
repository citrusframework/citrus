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

public class AllOfValidationMatcherTest extends UnitTestSupport {

    private final AllOfValidationMatcher matcher = new AllOfValidationMatcher();

    @Test
    public void shouldPassWhenAllSubMatchersPass() {
        matcher.validate("field", "hello world", List.of("startsWith('hello')", "endsWith('world')"), context);
    }

    @Test
    public void shouldFailWhenOneSubMatcherFails() {
        try {
            matcher.validate("field", "hello", List.of("startsWith('hello')", "endsWith('world')"), context);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("allOf()"));
            assertTrue(e.getMessage().contains("endsWith('world')"));
        }
    }

    @Test
    public void shouldReportAllFailures() {
        try {
            matcher.validate("field", "citrus", List.of("startsWith('foo')", "endsWith('bar')"), context);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("startsWith('foo')"));
            assertTrue(e.getMessage().contains("endsWith('bar')"));
        }
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void shouldRejectZeroSubExpressions() {
        matcher.validate("field", "value", List.of(), context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void shouldRejectOneSubExpression() {
        matcher.validate("field", "value", List.of("startsWith('a')"), context);
    }

    @Test
    public void shouldHandleNestedCombinator() {
        // allOf(anyOf(startsWith('foo'), startsWith('bar')), not(contains('error')))
        // value "fooOk": anyOf passes (startsWith foo), not(contains('error')) passes
        matcher.validate("field", "fooOk",
                List.of("anyOf(startsWith('foo'), startsWith('bar'))", "not(contains('error'))"),
                context);
    }

    @Test
    public void shouldFailNestedCombinatorWhenInnerFails() {
        try {
            // value "foo-error": anyOf passes (startsWith 'foo'), not(contains('error')) fails (value does contain 'error')
            matcher.validate("field", "foo-error",
                    List.of("anyOf(startsWith('foo'), startsWith('bar'))", "not(contains('error'))"),
                    context);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("allOf()"));
            assertTrue(e.getMessage().contains("not(contains('error'))"));
        }
    }
}
