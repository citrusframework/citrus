/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.restdocs.util;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class RestDocTestNameFormatterTest {

    @Test
    public void testFormat() throws Exception {
        Assert.assertEquals(RestDocTestNameFormatter.format(getClass(), getClass().getSimpleName() + ".testName"), "testName");
        Assert.assertEquals(RestDocTestNameFormatter.format(getClass(), getClass().getSimpleName() + ".testNameIT"), "testNameIt");
        Assert.assertEquals(RestDocTestNameFormatter.format(getClass(), "someOtherName.testName"), "someOtherName.testName");
        Assert.assertEquals(RestDocTestNameFormatter.format(getClass(), "someOtherName.testNameIT"), "someOtherName.testNameIt");
        Assert.assertEquals(RestDocTestNameFormatter.format(getClass(), "testName"), "testName");
        Assert.assertEquals(RestDocTestNameFormatter.format(getClass(), "testNameIT"), "testNameIt");
    }
}
