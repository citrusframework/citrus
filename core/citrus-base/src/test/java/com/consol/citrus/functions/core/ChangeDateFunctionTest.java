/*
 * Copyright 2006-2013 the original author or authors.
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

import com.consol.citrus.UnitTestSupport;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.FunctionParameterHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class ChangeDateFunctionTest extends UnitTestSupport {
    ChangeDateFunction function = new ChangeDateFunction();

    @Test
    public void testDefaultDateFormat() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$td.%1$tm.%1$tY", Calendar.getInstance()) + "', '+1y'"), context),
                String.format("%1$td.%1$tm.%1$tY", c));

        c = Calendar.getInstance();
        c.add(Calendar.MONTH, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$td.%1$tm.%1$tY", Calendar.getInstance()) + "', '+1M'"), context),
                String.format("%1$td.%1$tm.%1$tY", c));

        c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$td.%1$tm.%1$tY", Calendar.getInstance()) + "', '+1d'"), context),
                String.format("%1$td.%1$tm.%1$tY", c));
    }

    @Test
    public void testFunction() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1y', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.MONTH, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1M', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1d', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.HOUR, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1h', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1m', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.SECOND, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1s', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.YEAR, 10);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+10y', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.YEAR, 1);
        c.add(Calendar.MONTH, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1y+1M', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c.add(Calendar.DAY_OF_YEAR, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1y+1M+1d', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c.add(Calendar.HOUR, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1y+1M+1d+1h', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c.add(Calendar.MINUTE, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1y+1M+1d+1h+1m', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c.add(Calendar.SECOND, 1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1y+1M+1d+1h+1m+1s', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.YEAR, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '-1y', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '-1M', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '-1d', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.HOUR, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '-1h', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.MINUTE, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '-1m', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.SECOND, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '-1s', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.YEAR, -1);
        c.add(Calendar.MONTH, 1);
        c.add(Calendar.DAY_OF_YEAR, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '-1y+1M-1d', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = Calendar.getInstance();
        c.add(Calendar.YEAR, 1);
        c.add(Calendar.MONTH, -1);
        c.add(Calendar.DAY_OF_YEAR, -1);
        Assert.assertEquals(function.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1y-1M-1d', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));
    }

    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testWrongDateFormatUsage() {
        function.execute(FunctionParameterHelper.getParameterList("'1970-01-01', '+1y'"), context);
    }

	@Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.EMPTY_LIST, context);
    }
}
