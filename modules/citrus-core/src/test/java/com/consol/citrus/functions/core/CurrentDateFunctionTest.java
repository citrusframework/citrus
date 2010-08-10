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

import java.util.Calendar;
import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.functions.FunctionParameterHelper;
import com.consol.citrus.functions.core.CurrentDateFunction;
import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
 */
public class CurrentDateFunctionTest extends AbstractBaseTest {
    CurrentDateFunction function = new CurrentDateFunction();
    
    @Test
    public void testFunction() {
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd'")), String.format("%1$tY-%1$tm-%1$td", Calendar.getInstance()));
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss'")), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()));
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd'T'hh:mm:ss'")), String.format("%1$tY-%1$tm-%1$tdT%1$tI:%1$tM:%1$tS", Calendar.getInstance()));

        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y'")), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));
        
        c = Calendar.getInstance();
        c.add(Calendar.MONTH, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1M'")), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));
        
        c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1d'")), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));
        
        c = Calendar.getInstance();
        c.add(Calendar.HOUR, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1h'")), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));
        
        c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1m'")), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));
        
        c = Calendar.getInstance();
        c.add(Calendar.SECOND, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1s'")), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.YEAR, 10);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+10y'")), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));
        
        c = Calendar.getInstance();
        c.add(Calendar.YEAR, 1);
        c.add(Calendar.MONTH, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y+1M'")), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));
        
        c.add(Calendar.DAY_OF_YEAR, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y+1M+1d'")), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));
        
        c.add(Calendar.HOUR, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y+1M+1d+1h'")), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));
        
        c.add(Calendar.MINUTE, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y+1M+1d+1h+1m'")), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));
        
        c.add(Calendar.SECOND, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y+1M+1d+1h+1m+1s'")), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.YEAR, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1y'")), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));
        
        c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1M'")), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));
        
        c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1d'")), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));
        
        c = Calendar.getInstance();
        c.add(Calendar.HOUR, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1h'")), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));
        
        c = Calendar.getInstance();
        c.add(Calendar.MINUTE, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1m'")), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));
        
        c = Calendar.getInstance();
        c.add(Calendar.SECOND, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1s'")), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.YEAR, -1);
        c.add(Calendar.MONTH, 1);
        c.add(Calendar.DAY_OF_YEAR, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1y+1M-1d'")), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));
        
        c = Calendar.getInstance();
        c.add(Calendar.YEAR, 1);
        c.add(Calendar.MONTH, -1);
        c.add(Calendar.DAY_OF_YEAR, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y-1M-1d'")), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));
    }
    
    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testWrongParameterUsage() {
        function.execute(Collections.singletonList("no date format string"));
    }
    
	@Test
	@SuppressWarnings("unchecked")
    public void testNoParameters() {
        Assert.assertEquals(function.execute(Collections.EMPTY_LIST), String.format("%1$td.%1$tm.%1$tY", Calendar.getInstance()));
    }
}
