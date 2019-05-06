/*
 * Copyright 2006-2019 the original author or authors.
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

import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

/**
 * @author Christoph Deppisch
 */
public class ValidationMatcherUtilsTest extends AbstractTestNGUnitTest {

    @Autowired
    private ValidationMatcher validationMatcher;
    
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

        verify(validationMatcher, times(3)).validate("field", "value", Arrays.asList("value"), context);
        verify(validationMatcher).validate("field", "prefix:value", Arrays.asList("prefix:value"), context);
    }

    @Test
    public void testSubstituteIgnoreStatements() {

        //GIVEN
        final String controlMessage = "Something to @ignore@ for sure!";
        final String receivedMessage = "Something to consider for sure!";

        //WHEN
        final String substitutedControlMessage =
                ValidationMatcherUtils.substituteIgnoreStatements(controlMessage, receivedMessage);

        //THEN
        assertEquals(substitutedControlMessage, receivedMessage);
    }

    @Test
    public void testSubstituteIgnoreStatementsWithLengthLimit() {

        //GIVEN
        final String controlMessage = "(924,@ignore(23)@,txx,40)";
        final String receivedMessage = "(924,2018-10-01 16:53:38.561,txx,40)";

        //WHEN
        final String substitutedControlMessage =
                ValidationMatcherUtils.substituteIgnoreStatements(controlMessage, receivedMessage);

        //THEN
        assertEquals(substitutedControlMessage, receivedMessage);
    }

    @Test
    public void testSubstituteIgnoreStatementsWithLengthLimitFails() {

        //GIVEN
        final String controlMessage = "(924,@ignore(20)@,txx,40)";
        final String receivedMessage = "(924,2018-10-01 16:53:38.561,txx,40)";

        //WHEN
        final String substitutedControlMessage =
                ValidationMatcherUtils.substituteIgnoreStatements(controlMessage, receivedMessage);

        //THEN
        assertNotEquals(substitutedControlMessage, receivedMessage);
    }

}
