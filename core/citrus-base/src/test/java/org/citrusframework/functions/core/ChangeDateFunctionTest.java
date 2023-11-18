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

package org.citrusframework.functions.core;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.FunctionParameterHelper;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.Collections;

import static org.mockito.Mockito.doReturn;

/**
 * @author Christoph Deppisch
 */
public class ChangeDateFunctionTest extends UnitTestSupport {

    @Mock
    private ChangeDateFunction.CalendarProvider calendarProviderMock;

    private AutoCloseable mockitoContext;

    private ChangeDateFunction fixture;

    @BeforeMethod
    void beforeMethodSetup() {
        mockitoContext = MockitoAnnotations.openMocks(this);

        fixture = new ChangeDateFunction();
        ReflectionTestUtils.setField(fixture, "calendarProvider", calendarProviderMock, ChangeDateFunction.CalendarProvider.class);
    }

    @Test
    public void testDefaultDateFormat() {
        Calendar c = getAndInsertMockCalendar();
        c.add(Calendar.YEAR, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$td.%1$tm.%1$tY", Calendar.getInstance()) + "', '+1y'"), context),
                String.format("%1$td.%1$tm.%1$tY", c));

        c = getAndInsertMockCalendar();
        c.add(Calendar.MONTH, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$td.%1$tm.%1$tY", Calendar.getInstance()) + "', '+1M'"), context),
                String.format("%1$td.%1$tm.%1$tY", c));

        c = getAndInsertMockCalendar();
        c.add(Calendar.DAY_OF_YEAR, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$td.%1$tm.%1$tY", Calendar.getInstance()) + "', '+1d'"), context),
                String.format("%1$td.%1$tm.%1$tY", c));
    }

    @Test
    public void testFunction() {
        Calendar c = getAndInsertMockCalendar();
        c.add(Calendar.YEAR, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1y', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = getAndInsertMockCalendar();
        c.add(Calendar.MONTH, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1M', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = getAndInsertMockCalendar();
        c.add(Calendar.DAY_OF_YEAR, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1d', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = getAndInsertMockCalendar();
        c.add(Calendar.HOUR, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1h', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = getAndInsertMockCalendar();
        c.add(Calendar.MINUTE, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1m', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = getAndInsertMockCalendar();
        c.add(Calendar.SECOND, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1s', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = getAndInsertMockCalendar();
        c.add(Calendar.YEAR, 10);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+10y', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = getAndInsertMockCalendar();
        c.add(Calendar.YEAR, 1);
        c.add(Calendar.MONTH, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1y+1M', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c.add(Calendar.DAY_OF_YEAR, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1y+1M+1d', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c.add(Calendar.HOUR, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1y+1M+1d+1h', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c.add(Calendar.MINUTE, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1y+1M+1d+1h+1m', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c.add(Calendar.SECOND, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1y+1M+1d+1h+1m+1s', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = getAndInsertMockCalendar();
        c.add(Calendar.YEAR, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '-1y', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = getAndInsertMockCalendar();
        c.add(Calendar.MONTH, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '-1M', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = getAndInsertMockCalendar();
        c.add(Calendar.DAY_OF_YEAR, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '-1d', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = getAndInsertMockCalendar();
        c.add(Calendar.HOUR, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '-1h', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = getAndInsertMockCalendar();
        c.add(Calendar.MINUTE, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '-1m', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = getAndInsertMockCalendar();
        c.add(Calendar.SECOND, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '-1s', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = getAndInsertMockCalendar();
        c.add(Calendar.YEAR, -1);
        c.add(Calendar.MONTH, 1);
        c.add(Calendar.DAY_OF_YEAR, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '-1y+1M-1d', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));

        c = getAndInsertMockCalendar();
        c.add(Calendar.YEAR, 1);
        c.add(Calendar.MONTH, -1);
        c.add(Calendar.DAY_OF_YEAR, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'" +
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Calendar.getInstance()) + "', '+1y-1M-1d', 'yyyy-MM-dd HH:mm:ss'"), context),
                String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", c));
    }

    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testWrongDateFormatUsage() {
        fixture.execute(FunctionParameterHelper.getParameterList("'1970-01-01', '+1y'"), context);
    }

	@Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        fixture.execute(Collections.EMPTY_LIST, context);
    }

    private Calendar getAndInsertMockCalendar() {
        Calendar c = Calendar.getInstance();
        doReturn(c.clone()).when(calendarProviderMock).getInstance();
        return c;
    }

    @AfterMethod
    public void afterMethodTeardown() throws Exception {
        mockitoContext.close();
    }
}
