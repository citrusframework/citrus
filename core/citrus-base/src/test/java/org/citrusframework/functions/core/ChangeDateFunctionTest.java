/*
 * Copyright the original author or authors.
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

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.FunctionParameterHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;

public class ChangeDateFunctionTest extends UnitTestSupport {

    private static final String BASE_DATE = "15.01.2026";
    private static final String BASE_DATE_TIME = "2026-01-15 10:20:30";

    private final ChangeDateFunction fixture = new ChangeDateFunction();

    @Test
    public void testDefaultDateFormat() {
        Calendar calendar = defaultBaseCalendar();
        calendar.add(Calendar.YEAR, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE + "', '+1y'"), context),
                String.format("%1$td.%1$tm.%1$tY", calendar));

        calendar = defaultBaseCalendar();
        calendar.add(Calendar.MONTH, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE + "', '+1M'"), context),
                String.format("%1$td.%1$tm.%1$tY", calendar));

        calendar = defaultBaseCalendar();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE + "', '+1d'"), context),
                String.format("%1$td.%1$tm.%1$tY", calendar));
    }

    @Test
    public void testFunction() {
        Calendar calendar = dateTimeBaseCalendar();
        calendar.add(Calendar.YEAR, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE_TIME + "', '+1y', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = dateTimeBaseCalendar();
        calendar.add(Calendar.MONTH, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE_TIME + "', '+1M', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = dateTimeBaseCalendar();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE_TIME + "', '+1d', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = dateTimeBaseCalendar();
        calendar.add(Calendar.HOUR, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE_TIME + "', '+1h', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = dateTimeBaseCalendar();
        calendar.add(Calendar.MINUTE, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE_TIME + "', '+1m', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = dateTimeBaseCalendar();
        calendar.add(Calendar.SECOND, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE_TIME + "', '+1s', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = dateTimeBaseCalendar();
        calendar.add(Calendar.YEAR, 10);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE_TIME + "', '+10y', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = dateTimeBaseCalendar();
        calendar.add(Calendar.YEAR, 1);
        calendar.add(Calendar.MONTH, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE_TIME + "', '+1y+1M', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE_TIME + "', '+1y+1M+1d', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar.add(Calendar.HOUR, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE_TIME + "', '+1y+1M+1d+1h', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar.add(Calendar.MINUTE, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE_TIME + "', '+1y+1M+1d+1h+1m', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar.add(Calendar.SECOND, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE_TIME + "', '+1y+1M+1d+1h+1m+1s', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = dateTimeBaseCalendar();
        calendar.add(Calendar.YEAR, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE_TIME + "', '-1y', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = dateTimeBaseCalendar();
        calendar.add(Calendar.MONTH, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE_TIME + "', '-1M', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = dateTimeBaseCalendar();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE_TIME + "', '-1d', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = dateTimeBaseCalendar();
        calendar.add(Calendar.HOUR, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE_TIME + "', '-1h', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = dateTimeBaseCalendar();
        calendar.add(Calendar.MINUTE, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE_TIME + "', '-1m', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = dateTimeBaseCalendar();
        calendar.add(Calendar.SECOND, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE_TIME + "', '-1s', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = dateTimeBaseCalendar();
        calendar.add(Calendar.YEAR, -1);
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE_TIME + "', '-1y+1M-1d', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = dateTimeBaseCalendar();
        calendar.add(Calendar.YEAR, 1);
        calendar.add(Calendar.MONTH, -1);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" + BASE_DATE_TIME + "', '+1y-1M-1d', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));
    }

    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testWrongDateFormatUsage() {
        fixture.execute(FunctionParameterHelper.getParameterList("'1970-01-01', '+1y'"), context);
    }

    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        fixture.execute(Collections.EMPTY_LIST, context);
    }

    private Calendar defaultBaseCalendar() {
        return new GregorianCalendar(2026, Calendar.JANUARY, 15, 0, 0, 0);
    }

    private Calendar dateTimeBaseCalendar() {
        return new GregorianCalendar(2026, Calendar.JANUARY, 15, 10, 20, 30);
    }
}
