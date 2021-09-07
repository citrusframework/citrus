/*
 * Copyright 2006-2011 the original author or authors.
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

package com.consol.citrus.validation.matcher;

import java.util.Collections;

import com.consol.citrus.UnitTestSupport;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.exceptions.NoSuchValidationMatcherLibraryException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class ValidationMatcherRegistryTest extends UnitTestSupport {

    private ValidationMatcherLibrary validationMatcherLibrary = new ValidationMatcherLibrary();

    @Mock
    private ValidationMatcher matcher;

    @BeforeClass
    public void setupMocks() {
        MockitoAnnotations.initMocks(this);

        validationMatcherLibrary.setName("fooValidationMatcherLibrary");
        validationMatcherLibrary.setPrefix("foo:");
        validationMatcherLibrary.setMembers(Collections.singletonMap("customMatcher", matcher));
    }

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = super.createTestContextFactory();
        factory.getValidationMatcherRegistry().addValidationMatcherLibrary(validationMatcherLibrary);
        return factory;
    }

    @Test
    public void testGetValidationMatcherLibrary() {
        Assert.assertEquals(context.getValidationMatcherRegistry().getLibraryForPrefix("foo:"), validationMatcherLibrary);
    }

    @Test
    public void testUnknownValidationMatcherLibrary() {
        try {
            context.getValidationMatcherRegistry().getLibraryForPrefix("unknown:");
        } catch (NoSuchValidationMatcherLibraryException e) {
            Assert.assertTrue(e.getMessage().contains("unknown:"));
        }
    }

}
