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

package org.citrusframework.citrus.functions.core;

import java.util.Arrays;
import java.util.Collections;

import org.citrusframework.citrus.UnitTestSupport;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.citrusframework.citrus.exceptions.InvalidFunctionUsageException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class SystemPropertyFunctionTest extends UnitTestSupport {

    private final SystemPropertyFunction function = new SystemPropertyFunction();

    @Test
    public void testFunction() {
        System.setProperty("foo.property", "Citrus rocks!");
        Assert.assertEquals(function.execute(Collections.singletonList("foo.property"), context), "Citrus rocks!");
    }

    @Test
    public void testFunctionDefaultValue() {
        Assert.assertEquals(function.execute(Arrays.asList("bar.property", "This is a default"), context), "This is a default");
    }

    @Test(expectedExceptions = {CitrusRuntimeException.class}, expectedExceptionsMessageRegExp = "Failed to resolve system property 'bar.property'")
    public void testPropertyNotFound() {
        function.execute(Collections.singletonList("bar.property"), context);
    }

    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.emptyList(), context);
    }
}
