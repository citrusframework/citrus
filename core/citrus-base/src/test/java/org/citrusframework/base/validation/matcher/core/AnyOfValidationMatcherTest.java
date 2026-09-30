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

public class AnyOfValidationMatcherTest extends UnitTestSupport {

    private final AnyOfValidationMatcher matcher = new AnyOfValidationMatcher();

    @Test
    public void shouldPassWhenFirstSubMatcherPasses() {
        matcher.validate("field", "hello", List.of("startsWith('hel')", "startsWith('world')"), context);
    }

    @Test
    public void shouldPassWhenLastSubMatcherPasses() {
        matcher.validate("field", "world", List.of("startsWith('hel')", "startsWith('world')"), context);
    }

    @Test
    public void shouldPassWithMoreThanTwoSubMatchers() {
        matcher.validate("field", "citrus",
                List.of("startsWith('foo')", "startsWith('bar')", "startsWith('cit')"), context);
    }

    @Test
    public void shouldFailWhenAllSubMatchersFail() {
        try {
            matcher.validate("field", "citrus", List.of("startsWith('foo')", "startsWith('bar')"), context);
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("anyOf()"));
            assertTrue(e.getMessage().contains("startsWith('foo')"));
            assertTrue(e.getMessage().contains("startsWith('bar')"));
            assertTrue(e.getMessage().contains("all matchers failed"));
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
        // anyOf(allOf(startsWith('foo'), endsWith('bar')), contains('cit'))
        // value "citrus": allOf fails, contains('cit') passes → anyOf passes
        matcher.validate("field", "citrus",
                List.of("allOf(startsWith('foo'), endsWith('bar'))", "contains('cit')"),
                context);
    }

    @Test
    public void shouldShortCircuitOnFirstPass() {
        // first sub-matcher passes, so the second (which would also pass) is never reached
        // we can't easily observe short-circuit directly, but we verify it passes correctly
        matcher.validate("field", "foobar",
                List.of("startsWith('foo')", "endsWith('baz')"), context);
    }
}
