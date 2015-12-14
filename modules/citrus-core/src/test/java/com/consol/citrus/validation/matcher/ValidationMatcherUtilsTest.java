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

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.testng.Assert.fail;

/**
 * @author Christoph Deppisch
 */
public class ValidationMatcherUtilsTest extends AbstractTestNGUnitTest {

    @Autowired
    private ValidationMatcher validationMatcher;
    
    @Test
    public void testResolveDefaultValidationMatcher() {
        ValidationMatcherUtils.resolveValidationMatcher("field", "value", "@equalsIgnoreCase('value')@", context);
        ValidationMatcherUtils.resolveValidationMatcher("field", "value", "@${equalsIgnoreCase('value')}@", context);
        ValidationMatcherUtils.resolveValidationMatcher("field", "value", "@${equalsIgnoreCase(value)}@", context);
        // TODO CD should this be supported? Perhaps using "\'" to escape quote?
        // ValidationMatcherUtils.resolveValidationMatcher("field", "John's", "@equalsIgnoreCase('John's')@", context);
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

    @Test(dataProvider = "validControlExpressions")
    public void shouldExtractControlParametersSuccessfully(String controlExpression, List<String> expectedParameters) {
        ControlExpressionParser expressionParser = ValidationMatcherUtils.getDefaultControlExpressionParser();
        List<String> extractedParameters = ValidationMatcherUtils.extractControlValues(expressionParser, controlExpression, null);

        Assert.assertEquals(extractedParameters.size(), expectedParameters.size());

        for (int i = 0; i < expectedParameters.size(); i++) {
            Assert.assertTrue(extractedParameters.size() > i);
            Assert.assertEquals(extractedParameters.get(i), expectedParameters.get(i));
        }
    }

    @DataProvider
    public Object[][] validControlExpressions() {
        return new Object[][]{
                // {control-expression, expected-parameter-1, expected-parameter-2, ..}
                {"'a'", Arrays.asList("a")},
                {"'a',", Arrays.asList("a")},
                {"'a','b'", Arrays.asList("a","b")},
                {"'a','b',", Arrays.asList("a","b")},
                {"''", Arrays.asList("")},
                {"'',", Arrays.asList("")},
                {"", Arrays.<String>asList()},
                {null, Arrays.<String>asList()},
        };
    }

    @Test(dataProvider = "invalidControlExpressions", expectedExceptions = CitrusRuntimeException.class)
    public void shouldNotExtractControlParametersSuccessfully(String controlExpression) {
        ControlExpressionParser expressionParser = ValidationMatcherUtils.getDefaultControlExpressionParser();
        ValidationMatcherUtils.extractControlValues(expressionParser, controlExpression, null);
    }

    @DataProvider
    public Object[][] invalidControlExpressions() {
        return new Object[][]{
                {"'"},
                {"',"},
                {"'a"},
                {"'a,"},
                {"'a','b"},
                {"'a','b,"},
        };
    }

}
