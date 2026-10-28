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

package org.citrusframework.base.functions.core;

import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.functions.FunctionParameterHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Collections;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CurrentDateFunctionTest extends UnitTestSupport {

    private final CurrentDateFunction fixture = new CurrentDateFunction();

    @Test
    public void testFunction() {
        Calendar calendar = Calendar.getInstance();
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd'"), context));
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss'"), context));
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$tdT%1$tI:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd'T'hh:mm:ss'"), context));

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
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y'"), context));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1M'"), context));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1d'"), context));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1);
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1h'"), context));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 1);
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1m'"), context));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 1);
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1s'"), context));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 10);
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+10y'"), context));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        calendar.add(Calendar.MONTH, 1);
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y+1M'"), context));

        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y+1M+1d'"), context));

        calendar.add(Calendar.HOUR, 1);
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y+1M+1d+1h'"), context));

        calendar.add(Calendar.MINUTE, 1);
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y+1M+1d+1h+1m'"), context));

        calendar.add(Calendar.SECOND, 1);
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y+1M+1d+1h+1m+1s'"), context));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1y'"), context));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1M'"), context));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1d'"), context));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -1);
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1h'"), context));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -1);
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1m'"), context));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, -1);
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1s'"), context));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '-1y+1M-1d'"), context));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        calendar.add(Calendar.MONTH, -1);
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss', '+1y-1M-1d'"), context));

        calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Assertions.assertEquals(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", calendar), fixture.execute(FunctionParameterHelper.getParameterList("'yyyy-MM-dd HH:mm:ss','0h','UTC'"), context));
    }

    @Test
    public void testWrongParameterUsage() {
        assertThrows(IllegalArgumentException.class, () -> {
            fixture.execute(Collections.singletonList("no date format string"), context);
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNoParameters() {
        Assertions.assertEquals(String.format("%1$td.%1$tm.%1$tY", Calendar.getInstance()), fixture.execute(Collections.EMPTY_LIST, context));
    }
}
