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

import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.actions.FailAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class AssertTest extends UnitTestSupport {

    @Test
    public void testAssertDefaultException() {
        Assert assertAction = new Assert.Builder()
                .actions(new FailAction.Builder())
                .build();
        assertAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testAssertException() {
        Class exceptionClass = CitrusRuntimeException.class;

        Assert assertAction = new Assert.Builder()
                .actions(new FailAction.Builder())
                .exception(exceptionClass)
                .build();
        assertAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testAssertExceptionMessageCheck() {
        FailAction.Builder fail = new FailAction.Builder()
                .message("This went wrong!");

        Class exceptionClass = CitrusRuntimeException.class;

        Assert assertAction = new Assert.Builder()
                .actions(fail)
                .exception(exceptionClass)
                .message("This went wrong!")
                .build();
        assertAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testVariableSupport() {
        context.setVariable("message", "This went wrong!");

        FailAction.Builder fail = new FailAction.Builder()
                .message("This went wrong!");

        Class exceptionClass = CitrusRuntimeException.class;

        Assert assertAction = new Assert.Builder()
                .actions(fail)
                .exception(exceptionClass)
                .message("${message}")
                .build();
        assertAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testValidationMatcherSupport() {
        FailAction.Builder fail = new FailAction.Builder()
                .message("This went wrong!");

        Class exceptionClass = CitrusRuntimeException.class;

        Assert assertAction = new Assert.Builder()
                .actions(fail)
                .exception(exceptionClass)
                .message("@contains('wrong')@")
                .build();
        assertAction.execute(context);
    }

    @Test(expectedExceptions=CitrusRuntimeException.class)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testAssertExceptionWrongMessageCheck() {
        FailAction.Builder fail = new FailAction.Builder()
                .message("This went wrong!");

        Class exceptionClass = CitrusRuntimeException.class;

        Assert assertAction = new Assert.Builder()
                .actions(fail)
                .exception(exceptionClass)
                .message("Excpected error is something else")
                .build();
        assertAction.execute(context);
    }

    @Test(expectedExceptions=CitrusRuntimeException.class)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testMissingException() {
        Class exceptionClass = CitrusRuntimeException.class;

        Assert assertAction = new Assert.Builder()
                .actions(new EchoAction.Builder())
                .exception(exceptionClass)
                .build();
        assertAction.execute(context);
    }
}
