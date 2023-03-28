/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.validation;

import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.is;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class HamcrestHeaderValidatorTest extends AbstractTestNGUnitTest {

    private HamcrestHeaderValidator validator = new HamcrestHeaderValidator();
    private HeaderValidationContext validationContext = new HeaderValidationContext();

    @Test(dataProvider = "successData")
    public void testValidateHeader(Object receivedValue, Object controlValue) {
        validator.validateHeader("foo", receivedValue, controlValue, context, validationContext);
    }

    @DataProvider
    public Object[][] successData() {
        return new Object[][] {
            new Object[] { "foo", is("foo") }
        };
    }

    @Test(dataProvider = "errorData", expectedExceptions = ValidationException.class)
    public void testValidateHeaderError(Object receivedValue, Object controlValue) {
        validator.validateHeader("foo", receivedValue, controlValue, context, validationContext);
    }

    @DataProvider
    public Object[][] errorData() {
        return new Object[][] {
            new Object[] { "foo", is("wrong") }
        };
    }
}
