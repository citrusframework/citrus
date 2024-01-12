/*
 * Copyright 2006-2024 the original author or authors.
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

package org.citrusframework.validation.matcher;

import java.util.Collections;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.context.TestContextFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Christoph Deppisch
 */
public class ValidationMatcherUtilsTest extends UnitTestSupport {

    private ValidationMatcherLibrary validationMatcherLibrary = new ValidationMatcherLibrary();

    @Mock
    private ValidationMatcher validationMatcher;

    @BeforeClass
    public void setupMocks() {
        MockitoAnnotations.openMocks(this);

        validationMatcherLibrary.setName("fooValidationMatcherLibrary");
        validationMatcherLibrary.setPrefix("foo:");
        validationMatcherLibrary.setMembers(Collections.singletonMap("customMatcher", validationMatcher));
    }

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = super.createTestContextFactory();
        factory.getValidationMatcherRegistry().addValidationMatcherLibrary(validationMatcherLibrary);
        return factory;
    }

    @Test
    public void testResolveDefaultValidationMatcher() {
        ValidationMatcherUtils.resolveValidationMatcher("field", "value", "@ignore@", context);
        ValidationMatcherUtils.resolveValidationMatcher("field", "value", "@ignore()@", context);
        ValidationMatcherUtils.resolveValidationMatcher("field", "value", "@ignore('bad syntax')@", context);
        ValidationMatcherUtils.resolveValidationMatcher("field", "value", "@equalsIgnoreCase('value')@", context);
        ValidationMatcherUtils.resolveValidationMatcher("field", "value", "@${equalsIgnoreCase('value')}@", context);
        ValidationMatcherUtils.resolveValidationMatcher("field", "value", "@${equalsIgnoreCase(value)}@", context);
        ValidationMatcherUtils.resolveValidationMatcher("field", "John's", "@equalsIgnoreCase('John's')@", context);
        ValidationMatcherUtils.resolveValidationMatcher("field", "John's&Barabara's", "@equalsIgnoreCase('John's&Barabara's')@", context);
        ValidationMatcherUtils.resolveValidationMatcher("field", "", "@equalsIgnoreCase('')@", context);
        ValidationMatcherUtils.resolveValidationMatcher("field", "prefix:value", "@equalsIgnoreCase('prefix:value')@", context);
    }

    @Test
    public void testResolveCustomValidationMatcher() {
        reset(validationMatcher);

        ValidationMatcherUtils.resolveValidationMatcher("field", "value", "@foo:customMatcher('value')@", context);
        ValidationMatcherUtils.resolveValidationMatcher("field", "value", "@foo:customMatcher(value)@", context);
        ValidationMatcherUtils.resolveValidationMatcher("field", "value", "@${foo:customMatcher('value')}@", context);
        ValidationMatcherUtils.resolveValidationMatcher("field", "prefix:value", "@foo:customMatcher('prefix:value')@", context);

        verify(validationMatcher, times(3)).validate("field", "value", Collections.singletonList("value"), context);
        verify(validationMatcher).validate("field", "prefix:value", Collections.singletonList("prefix:value"), context);
    }
}
