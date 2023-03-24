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

package org.citrusframework.validation.matcher;

import java.util.Collections;

import org.citrusframework.exceptions.NoSuchValidationMatcherException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class ValidationMatcherLibraryTest {

    private ValidationMatcherLibrary validationMatcherLibrary = new ValidationMatcherLibrary();

    @Mock
    private ValidationMatcher matcher;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);

        validationMatcherLibrary.setName("fooValidationMatcherLibrary");
        validationMatcherLibrary.setPrefix("foo:");
        validationMatcherLibrary.setMembers(Collections.singletonMap("customMatcher", matcher));
    }

    @Test
    public void testGetValidationMatcher() {
        Assert.assertNotNull(validationMatcherLibrary.getValidationMatcher("customMatcher"));
    }

    @Test
    public void testUnknownValidationMatcher() {
        try {
            validationMatcherLibrary.getValidationMatcher("unknownMatcher");
        } catch (NoSuchValidationMatcherException e) {
            Assert.assertTrue(e.getMessage().contains("unknownMatcher"));
            Assert.assertTrue(e.getMessage().contains(validationMatcherLibrary.getName()));
            Assert.assertTrue(e.getMessage().contains(validationMatcherLibrary.getPrefix()));
        }
    }

    @Test
    public void testKnowsValidationMatcher() {
        Assert.assertTrue(validationMatcherLibrary.knowsValidationMatcher("foo:customMatcher()"));
        Assert.assertFalse(validationMatcherLibrary.knowsValidationMatcher("foo:unknownMatcher()"));
    }
}
