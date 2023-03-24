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

package org.citrusframework.functions.core;

import java.util.Calendar;
import java.util.Collections;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.functions.FunctionParameterHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class CurrentDateFunctionTest extends UnitTestSupport {
    CurrentDateFunction function = new CurrentDateFunction();

    @Test
    public void testFunction() {
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd'"), context), String.format("%1$tY-%1$tm-%1$td", Calendar.getInstance()));
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()));
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd'T'hh:mm:ss'"), context), String.format("%1$tY-%1$tm-%1$tdT%1$tI:%1$tM:%1$tS", Calendar.getInstance()));

        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.MONTH, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1M'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1d'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.HOUR, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1h'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1m'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.SECOND, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1s'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.YEAR, 10);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+10y'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.YEAR, 1);
        c.add(Calendar.MONTH, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y+1M'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c.add(Calendar.DAY_OF_YEAR, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y+1M+1d'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c.add(Calendar.HOUR, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y+1M+1d+1h'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c.add(Calendar.MINUTE, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y+1M+1d+1h+1m'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c.add(Calendar.SECOND, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y+1M+1d+1h+1m+1s'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.YEAR, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1y'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1M'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1d'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.HOUR, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1h'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.MINUTE, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1m'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.SECOND, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1s'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.YEAR, -1);
        c.add(Calendar.MONTH, 1);
        c.add(Calendar.DAY_OF_YEAR, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1y+1M-1d'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.YEAR, 1);
        c.add(Calendar.MONTH, -1);
        c.add(Calendar.DAY_OF_YEAR, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y-1M-1d'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testWrongParameterUsage() {
        function.execute(Collections.singletonList("no date format string"), context);
    }

	@Test
	@SuppressWarnings("unchecked")
    public void testNoParameters() {
        Assert.assertEquals(function.execute(Collections.EMPTY_LIST, context), String.format("%1$td.%1$tm.%1$tY", Calendar.getInstance()));
    }
}
