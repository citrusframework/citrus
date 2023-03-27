/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.citrus.integration.runner;

import org.citrusframework.citrus.actions.AbstractTestAction;
import org.citrusframework.citrus.annotations.CitrusTest;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.dsl.testng.TestNGCitrusTestRunner;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.citrusframework.citrus.exceptions.ValidationException;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * @author Christoph Deppisch
 */
@Test
public class AssertExceptionTestRunnerIT extends TestNGCitrusTestRunner {

    @CitrusTest
    public void assertAction() {
        variable("failMessage", "Something went wrong!");

        assertException().when(fail("Fail once"));

        assertException().exception(CitrusRuntimeException.class)
                        .message("Fail again")
                .when(fail("Fail again"));


        assertException().exception(CitrusRuntimeException.class)
                        .message("${failMessage}")
                .when(fail("${failMessage}"));

        assertException().exception(CitrusRuntimeException.class)
                        .message("@contains('wrong')@")
                .when(fail("${failMessage}"));

        assertException().exception(ValidationException.class)
                .when(assertException().exception(IOException.class)
                        .when(fail("Fail another time")));

        assertException().exception(ValidationException.class)
                .when(assertException().exception(CitrusRuntimeException.class)
                                .message("Fail again")
                        .when(fail("Fail with nice error message")));

        assertException().exception(ValidationException.class)
                .when(assertException().exception(CitrusRuntimeException.class)
                        .when(echo("Nothing fails here")));

        assertException().exception(ValidationException.class)
                .when(assertException().exception(CitrusRuntimeException.class)
                                .message("Must be failing")
                        .when(echo("Nothing fails here either")));


        assertException().exception(CitrusRuntimeException.class)
                        .message("Unknown variable 'foo'")
                .when(new AbstractTestAction() {
                    @Override
                    public void doExecute(TestContext context) {
                        context.getVariable("foo");
                    }
                });
    }
}
