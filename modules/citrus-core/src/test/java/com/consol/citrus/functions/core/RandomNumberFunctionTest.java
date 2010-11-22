/*
 * Copyright 2006-2010 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
 */
public class RandomNumberFunctionTest extends AbstractBaseTest {
    RandomNumberFunction function = new RandomNumberFunction();
    
    @Test
    public void testFunction() {
        List<String> params = new ArrayList<String>();
        params.add("3");
        
        Assert.assertTrue(Integer.valueOf(function.execute(params)) < 1000);
        
        params = new ArrayList<String>();
        params.add("3");
        params.add("true");
        
        Assert.assertTrue(function.execute(params).length() == 3);
    }
    
    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testWrongParameterUsage() {
        function.execute(Collections.singletonList("-1"));
    }
    
    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.<String>emptyList());
    }
    
    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testTooManyParameters() {
        List<String> params = new ArrayList<String>();
        params.add("3");
        params.add("true");
        params.add("too much");
        
        function.execute(params);
    }
}
