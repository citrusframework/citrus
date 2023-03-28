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

package org.citrusframework.container;

import org.citrusframework.TestAction;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.actions.FailAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Christoph Deppisch
 */
public class CatchTest extends UnitTestSupport {

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testCatchDefaultException() {
        Catch catchAction = new Catch.Builder()
                .actions(new FailAction.Builder())
                .build();
        catchAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testCatchException() {
        Catch catchAction = new Catch.Builder()
                .actions(new FailAction.Builder())
                .exception(CitrusRuntimeException.class.getName())
                .build();
        catchAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testNothingToCatch() {
        Catch catchAction = new Catch.Builder()
                .actions(new EchoAction.Builder())
                .exception(CitrusRuntimeException.class.getName())
                .build();
        catchAction.execute(context);
    }

    @Test
    public void testCatchFirstActionFailing() {
        TestAction action = Mockito.mock(TestAction.class);

        reset(action);

        Catch catchAction = new Catch.Builder()
                .actions(new FailAction.Builder(), () -> action)
                .exception(CitrusRuntimeException.class.getName())
                .build();
        catchAction.execute(context);
        verify(action).execute(context);
    }

    @Test
    public void testCatchSomeActionFailing() {
        TestAction action = Mockito.mock(TestAction.class);

        reset(action);

        Catch catchAction = new Catch.Builder()
                .actions(() -> action, new FailAction.Builder(), () -> action)
                .exception(CitrusRuntimeException.class.getName())
                .build();
        catchAction.execute(context);
        verify(action, times(2)).execute(context);
    }
}
