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

package org.citrusframework.base.validation.matcher;

import java.util.Collections;

import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.exceptions.NoSuchValidationMatcherLibraryException;
import org.citrusframework.validation.matcher.ValidationMatcher;
import org.citrusframework.validation.matcher.ValidationMatcherLibrary;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ValidationMatcherRegistryTest extends UnitTestSupport {

    private ValidationMatcherLibrary validationMatcherLibrary = new ValidationMatcherLibrary();

    @Mock
    private ValidationMatcher matcher;

    @Override
    protected TestContextFactory createTestContextFactory() {
        // Called by the superclass @BeforeEach, which runs before any @BeforeEach of this class
        validationMatcherLibrary.setName("fooValidationMatcherLibrary");
        validationMatcherLibrary.setPrefix("foo:");
        validationMatcherLibrary.setMembers(Collections.singletonMap("customMatcher", matcher));

        TestContextFactory factory = super.createTestContextFactory();
        factory.getValidationMatcherRegistry().addValidationMatcherLibrary(validationMatcherLibrary);
        return factory;
    }

    @Test
    public void testGetValidationMatcherLibrary() {
        Assertions.assertEquals(validationMatcherLibrary, context.getValidationMatcherRegistry().getLibraryForPrefix("foo:"));
    }

    @Test
    public void testUnknownValidationMatcherLibrary() {
        try {
            context.getValidationMatcherRegistry().getLibraryForPrefix("unknown:");
        } catch (NoSuchValidationMatcherLibraryException e) {
            Assertions.assertTrue(e.getMessage().contains("unknown:"));
        }
    }
}
