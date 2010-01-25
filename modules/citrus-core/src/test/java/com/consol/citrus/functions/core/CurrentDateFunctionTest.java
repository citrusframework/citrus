/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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
