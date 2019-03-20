/*
 *    Copyright 2019 the original author or authors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.consol.citrus.dsl.builder;

import com.consol.citrus.condition.ActionCondition;
import com.consol.citrus.condition.Condition;
import com.consol.citrus.condition.FileCondition;
import com.consol.citrus.condition.HttpCondition;
import com.consol.citrus.condition.MessageCondition;
import com.consol.citrus.container.Wait;
import com.consol.citrus.dsl.runner.TestRunner;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

public class WaitBuilderTest {

    private TestRunner testRunnerMock = mock(TestRunner.class);
    private Wait waitAction;
    private WaitBuilder waitBuilder;

    @BeforeMethod
    public void setup(){
        waitAction = new Wait();
        waitBuilder = new WaitBuilder(testRunnerMock, waitAction);
    }

    @Test
    public void testConditionIsSet(){

        //GIVEN
        Condition conditionMock = mock(Condition.class);

        //WHEN
        waitBuilder.condition(conditionMock);

        //THEN
        assertEquals(waitAction.getCondition(), conditionMock);
    }

    @Test
    public void testWaitForHttpUrlIsSet(){

        //GIVEN
        String url = "google.de";

        //WHEN
        final WaitHttpConditionBuilder builder = waitBuilder.http(url);

        //THEN
        assertEquals(builder.getCondition().getUrl(), url);
    }

    @Test
    public void testWaitForHttpConditionIsCreated(){

        //GIVEN

        //WHEN
        final WaitHttpConditionBuilder builder = waitBuilder.http();

        //THEN
        assertEquals(builder.getCondition(), new HttpCondition());
    }

    @Test
    public void testWaitForMessageStringIsSet(){

        //GIVEN
        String message = "{ \"massage\": \"fancy\" }";

        //WHEN
        final WaitConditionBuilder builder = waitBuilder.message(message);

        //THEN
        final MessageCondition condition = (MessageCondition) builder.getCondition();
        assertEquals(condition.getMessageName(), message);
    }

    @Test
    public void testWaitForMessageConditionIsCreated(){

        //GIVEN

        //WHEN
        final WaitMessageConditionBuilder builder = waitBuilder.message();

        //THEN
        assertEquals(builder.getCondition(), new MessageCondition());
    }

    @Test
    public void testWaitForExecutionConditionIsCreated(){

        //GIVEN
        ActionCondition expectedCondition = new ActionCondition();

        //WHEN
        final WaitActionConditionBuilder builder = waitBuilder.execution();

        //THEN
        assertEquals(builder.getCondition(), expectedCondition);
        assertEquals(waitBuilder.getContainers().search(builder.getAction()),1 );
    }

    @Test
    public void testWaitForFilePathIsSet(){

        //GIVEN
        final String path = "/path/to/file";

        //WHEN
        final WaitFileConditionBuilder builder = waitBuilder.file(path);

        //THEN
        assertEquals(builder.getCondition().getFilePath(), path);
    }

    @Test
    public void testWaitForFileReferenceIsSet(){

        //GIVEN
        final File file = mock(File.class);

        //WHEN
        final WaitFileConditionBuilder builder = waitBuilder.file(file);

        //THEN
        assertEquals(builder.getCondition().getFile(), file);
    }

    @Test
    public void testWaitForFileConditionIsCreated(){

        //GIVEN

        //WHEN
        final WaitFileConditionBuilder builder = waitBuilder.file();

        //THEN
        assertEquals(builder.getCondition(), new FileCondition());
    }

    @Test
    public void testSecondsToWaitAreSet(){

        //GIVEN
        String seconds = "42";

        //WHEN
        final WaitBuilder builder = waitBuilder.seconds(seconds);

        //THEN
        assertEquals(builder.container.getSeconds(), seconds);
    }

    @Test
    public void testSecondsToWaitAsLongAreSet(){

        //GIVEN
        long seconds = 42L;

        //WHEN
        final WaitBuilder builder = waitBuilder.seconds(seconds);

        //THEN
        assertEquals(builder.container.getSeconds(), Long.toString(seconds));
    }

    @Test
    public void testMillisecondsToWaitAreSet(){

        //GIVEN
        String milliseconds = "42";

        //WHEN
        final WaitBuilder builder = waitBuilder.milliseconds(milliseconds);

        //THEN
        assertEquals(builder.container.getMilliseconds(), milliseconds);
    }

    @Test
    public void testMillisecondsToWaitAsLongAreSet(){

        //GIVEN
        long milliseconds = 42L;

        //WHEN
        final WaitBuilder builder = waitBuilder.milliseconds(milliseconds);

        //THEN
        assertEquals(builder.container.getMilliseconds(), Long.toString(milliseconds));
    }

    @Test
    public void testMsToWaitAreSet(){

        //GIVEN
        String milliseconds = "42";

        //WHEN
        final WaitBuilder builder = waitBuilder.ms(milliseconds);

        //THEN
        assertEquals(builder.container.getMilliseconds(), milliseconds);
    }

    @Test
    public void testMsToWaitAsLongAreSet(){

        //GIVEN
        long milliseconds = 42L;

        //WHEN
        final WaitBuilder builder = waitBuilder.ms(milliseconds);

        //THEN
        assertEquals(builder.container.getMilliseconds(), Long.toString(milliseconds));
    }

    @Test
    public void testIntervalToWaitAreSet(){

        //GIVEN
        String interval = "42";

        //WHEN
        final WaitBuilder builder = waitBuilder.interval(interval);

        //THEN
        assertEquals(builder.container.getInterval(), interval);
    }

    @Test
    public void testIntervalToWaitAsLongAreSet(){

        //GIVEN
        long interval = 42L;

        //WHEN
        final WaitBuilder builder = waitBuilder.interval(interval);

        //THEN
        assertEquals(builder.container.getInterval(), Long.toString(interval));
    }
}