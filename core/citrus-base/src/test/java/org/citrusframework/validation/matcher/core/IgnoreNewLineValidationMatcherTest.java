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

package org.citrusframework.validation.matcher.core;

import java.util.Collections;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.ValidationException;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class IgnoreNewLineValidationMatcherTest extends UnitTestSupport {

    private IgnoreNewLineValidationMatcher matcher = new IgnoreNewLineValidationMatcher();

    @Test
    public void testValidateSuccess() {
        matcher.validate("field", "value", Collections.singletonList("value"), context);
        matcher.validate("field", "value1 \nvalue2 \nvalue3!", Collections.singletonList("value1 value2 value3!"), context);
        matcher.validate("field", "\nvalue1 \nvalue2 \nvalue3!\n", Collections.singletonList("value1 value2 value3!"), context);
        matcher.validate("field", "value1 \r\nvalue2 \r\nvalue3!\r\n", Collections.singletonList("value1 value2 value3!"), context);
        matcher.validate("field", "\r\nvalue1 \r\nvalue2 \r\nvalue3!", Collections.singletonList("value1 value2 value3!"), context);
        matcher.validate("field", "value1 \n\n\nvalue2 \n\nvalue3!", Collections.singletonList("value1 value2 value3!"), context);
        matcher.validate("field", "value1 \r\n\r\n\r\nvalue2 \r\n\r\nvalue3!", Collections.singletonList("value1 value2 value3!"), context);
        matcher.validate("field", "value1 \n\n\nvalue2 \n\nvalue3!", Collections.singletonList("value1 \nvalue2 \nvalue3!"), context);
        matcher.validate("field", "value1 \r\n\r\n\r\nvalue2 \r\n\r\nvalue3!", Collections.singletonList("value1 \r\nvalue2 \r\nvalue3!"), context);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testValidateError() {
        matcher.validate("field", "value1 \nvalue2 \nvalue3!", Collections.singletonList("value1! value2! value3!"), context);
    }
}
