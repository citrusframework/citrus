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

package org.citrusframework.actions;

import java.io.File;

import org.citrusframework.api.condition.Condition;
import org.citrusframework.container.Wait;
import org.citrusframework.container.WaitActionConditionBuilder;
import org.citrusframework.container.WaitFileConditionBuilder;
import org.citrusframework.container.WaitHttpConditionBuilder;
import org.citrusframework.container.WaitMessageConditionBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class WaitBuilderTest {

    private Wait.Builder<Condition> waitBuilder;

    @BeforeEach
    public void setup(){
        waitBuilder = new Wait.Builder<>();
    }

    @Test
    public void testConditionIsSet(){

        //GIVEN
        Condition conditionMock = mock(Condition.class);

        //WHEN
        waitBuilder.condition(conditionMock);

        //THEN
        assertEquals(conditionMock, waitBuilder.build().getCondition());
    }

    @Test
    public void testWaitForHttpUrlIsSet(){

        //GIVEN
        String url = "google.de";

        //WHEN
        final WaitHttpConditionBuilder builder = waitBuilder.http().url(url);

        //THEN
        assertEquals(url, builder.getCondition().getUrl());
    }

    @Test
    public void testWaitForMessageStringIsSet(){

        //GIVEN
        String messageName = "myMessage";

        //WHEN
        final WaitMessageConditionBuilder builder = waitBuilder.message().name(messageName);

        //THEN
        assertEquals(messageName, builder.getCondition().getMessageName());
    }

    @Test
    public void testWaitForExecutionConditionIsCreated(){

        //GIVEN
        EchoAction echo = new EchoAction.Builder().build();

        //WHEN
        final WaitActionConditionBuilder builder = waitBuilder.execution().action(echo);

        //THEN
        assertEquals(echo, builder.getCondition().getAction());
    }

    @Test
    public void testWaitForFilePathIsSet(){

        //GIVEN
        final String path = "/path/to/file";

        //WHEN
        final WaitFileConditionBuilder builder = waitBuilder.file().path(path);

        //THEN
        assertEquals(path, builder.getCondition().getFilePath());
    }

    @Test
    public void testWaitForFileReferenceIsSet(){

        //GIVEN
        final File file = mock(File.class);

        //WHEN
        final WaitFileConditionBuilder builder = waitBuilder.file().resource(file);

        //THEN
        assertEquals(file, builder.getCondition().getFile());
    }

    @Test
    public void testSecondsToWaitAreSet(){

        //GIVEN
        double seconds = 42.0;

        //WHEN
        final Wait.Builder<Condition> builder = waitBuilder.seconds(seconds);

        //THEN
        assertEquals("42000", builder.build().getTime());
    }

    @Test
    public void testSecondsToWaitAsLongAreSet(){

        //GIVEN
        long seconds = 42L;

        //WHEN
        final Wait.Builder<Condition> builder = waitBuilder.seconds(seconds);

        //THEN
        assertEquals("42000", builder.build().getTime());
    }

    @Test
    public void testMillisecondsToWaitAreSet(){

        //GIVEN
        long milliseconds = 400L;

        //WHEN
        final Wait.Builder<Condition> builder = waitBuilder.milliseconds(milliseconds);

        //THEN
        assertEquals("400", builder.build().getTime());
    }

    @Test
    public void testIntervalToWaitAreSet(){

        //GIVEN
        long interval = 100L;

        //WHEN
        final Wait.Builder<Condition> builder = waitBuilder.interval(interval);

        //THEN
        assertEquals("100", builder.build().getInterval());
    }
}
