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

package com.consol.citrus.functions.core;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class EnvironmentPropertyFunctionTest extends AbstractTestNGUnitTest {

    private Environment environment = Mockito.mock(Environment.class);
    private EnvironmentPropertyFunction function = new EnvironmentPropertyFunction();

    @BeforeMethod
    public void setup() {
        function.setEnvironment(environment);
    }

    @Test
    public void testFunction() {
        when(environment.getProperty("foo.property")).thenReturn("Citrus rocks!");
        Assert.assertEquals(function.execute(Collections.singletonList("foo.property"), context), "Citrus rocks!");
    }

    @Test
    public void testFunctionDefaultValue() {
        Assert.assertEquals(function.execute(Arrays.asList("bar.property", "This is a default"), context), "This is a default");
    }

    @Test(expectedExceptions = {CitrusRuntimeException.class}, expectedExceptionsMessageRegExp = "Failed to resolve property 'bar.property' in environment")
    public void testPropertyNotFound() {
        function.execute(Collections.singletonList("bar.property"), context);
    }

    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.emptyList(), context);
    }
}