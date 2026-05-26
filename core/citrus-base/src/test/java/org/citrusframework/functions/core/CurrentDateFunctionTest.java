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
import org.citrusframework.functions.FunctionParameterHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.Collections;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

public class CurrentDateFunctionTest extends UnitTestSupport {

    private final CurrentDateFunction fixture = new CurrentDateFunction();

    @Test
    public void testFunction() {
        Calendar calendar = Calendar.getInstance();
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd'"), context), String.format("%1$tY-%1$tm-%1$td", calendar));
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd'T'hh:mm:ss'"), context), String.format("%1$tY-%1$tm-%1$tdT%1$tI:%1$tM:%1$tS", calendar));

        String zonedResult = fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd'T'hh:mm:ssZ','0h','UTC+2'"), context);
        assertThat(zonedResult)
                .contains("T")
                .endsWith("+0200");

        String rfc3399Result = fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd'T'hh:mm:ssXXX','0h','UTC+2'"), context);
        assertThat(rfc3399Result)
                .contains("T")
                .endsWith("+02:00");

        calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1M'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1d'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1h'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1m'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1s'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 10);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+10y'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        calendar.add(Calendar.MONTH, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y+1M'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y+1M+1d'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar.add(Calendar.HOUR, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y+1M+1d+1h'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar.add(Calendar.MINUTE, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y+1M+1d+1h+1m'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar.add(Calendar.SECOND, 1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y+1M+1d+1h+1m+1s'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1y'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1M'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1d'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1h'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1m'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1s'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1y+1M-1d'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        calendar.add(Calendar.MONTH, -1);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y-1M-1d'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));

        calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Assert.assertEquals(fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss','0h','UTC'"), context), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar));
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testWrongParameterUsage() {
        fixture.execute(Collections.singletonList("no date format string"), context);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNoParameters() {
        Assert.assertEquals(fixture.execute(Collections.EMPTY_LIST, context), String.format("%1$td.%1$tm.%1$tY", Calendar.getInstance()));
    }
}
