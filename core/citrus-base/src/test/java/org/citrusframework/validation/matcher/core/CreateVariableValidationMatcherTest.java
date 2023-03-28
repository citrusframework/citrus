/*
 * Copyright 2006-2014 the original author or authors.
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

import java.util.Arrays;

import org.citrusframework.UnitTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CreateVariableValidationMatcherTest extends UnitTestSupport {

    private CreateVariableValidationMatcher matcher = new CreateVariableValidationMatcher();

    @Test
    public void testValidateSuccess() {
        matcher.validate("field", "This is a test", Arrays.<String>asList(), context);

        Assert.assertEquals(context.getVariable("field"), "This is a test");

        matcher.validate("field", "This is a 2nd test", Arrays.<String>asList(), context);

        Assert.assertEquals(context.getVariable("field"), "This is a 2nd test");

        context.setVariable("foo", "bar");

        matcher.validate("field", "Another test", Arrays.asList("foo"), context);

        Assert.assertEquals(context.getVariable("field"), "This is a 2nd test");
        Assert.assertEquals(context.getVariable("foo"), "Another test");

        matcher.validate("field", "This is a 3rd test", null, context);
        Assert.assertEquals(context.getVariable("field"), "This is a 3rd test");
    }
}
