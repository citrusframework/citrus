/*
 * Copyright 2006-2015 the original author or authors.
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

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class DefaultControlExpressionParserTest {

    @Test(dataProvider = "validControlExpressions")
    public void shouldExtractControlParametersSuccessfully(String controlExpression, List<String> expectedParameters) {
        ControlExpressionParser expressionParser = new DefaultControlExpressionParser();
        List<String> extractedParameters = expressionParser.extractControlValues(controlExpression, null);

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
                {"'a,s','b',", Arrays.asList("a,s","b")},
                {"'a)s','b',", Arrays.asList("a)s","b")},
                {"'a's','b',", Arrays.asList("a's","b")},
                {"''", Arrays.asList("")},
                {"'',", Arrays.asList("")},
                {"", Arrays.<String>asList()},
                {null, Arrays.<String>asList()},
        };
    }

    @Test(dataProvider = "invalidControlExpressions", expectedExceptions = CitrusRuntimeException.class)
    public void shouldNotExtractControlParametersSuccessfully(String controlExpression) {
        ControlExpressionParser expressionParser = new DefaultControlExpressionParser();
        expressionParser.extractControlValues(controlExpression, null);
    }

    @DataProvider
    public Object[][] invalidControlExpressions() {
        return new Object[][]{
                {"'"},
                {"',"},
                {"'a"},
                {"'a,"},
                {"'a's,"},
                {"'a',s'"},
                {"'a','b"},
                {"'a','b,"},
        };
    }
}
