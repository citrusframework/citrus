/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.functions.core;

import java.util.Arrays;
import java.util.Collections;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.DefaultFunctionLibrary;
import org.citrusframework.functions.Function;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class EnvironmentPropertyFunctionTest extends AbstractTestNGUnitTest {

    private final Environment environment = Mockito.mock(Environment.class);
    private final EnvironmentPropertyFunction function = new EnvironmentPropertyFunction();

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

    @Test
    public void shouldLookupFunction() {
        Assert.assertTrue(Function.lookup().containsKey("env"));
        Assert.assertEquals(Function.lookup().get("env").getClass(), EnvironmentPropertyFunction.class);

        Assert.assertEquals(new DefaultFunctionLibrary().getFunction("env").getClass(), EnvironmentPropertyFunction.class);
    }
}
