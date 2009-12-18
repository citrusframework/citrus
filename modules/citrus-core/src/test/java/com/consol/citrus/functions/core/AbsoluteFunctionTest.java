/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.functions.core;

import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.core.AbsoluteFunction;
import com.consol.citrus.testng.AbstractBaseTest;

public class AbsoluteFunctionTest extends AbstractBaseTest {
    AbsoluteFunction function = new AbsoluteFunction();
    
    @Test
    public void testFunction() {
        Assert.assertEquals(function.execute(Collections.singletonList("-0.0")), "0.0");
        Assert.assertEquals(function.execute(Collections.singletonList("-0")), "0");
        Assert.assertEquals(function.execute(Collections.singletonList("2.0")), "2.0");
        Assert.assertEquals(function.execute(Collections.singletonList("2")), "2");
        Assert.assertEquals(function.execute(Collections.singletonList("2.5")), "2.5");
        Assert.assertEquals(function.execute(Collections.singletonList("-2.0")), "2.0");
        Assert.assertEquals(function.execute(Collections.singletonList("-2")), "2");
        Assert.assertEquals(function.execute(Collections.singletonList("-2.5")), "2.5");
    }
    
    @Test(expectedExceptions = {NumberFormatException.class})
    public void testWrongParameterUsage() {
        function.execute(Collections.singletonList("no digit"));
    }
    
    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.<String>emptyList());
    }
}
