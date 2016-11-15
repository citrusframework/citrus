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

package com.consol.citrus.javadsl.design;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * @author Christoph Deppisch
 */
@Test
public class AssertJavaIT extends TestNGCitrusTestDesigner {
    
    @CitrusTest
    public void assertAction() {
        variable("failMessage", "Something went wrong!");

        assertException()
                .exception(CitrusRuntimeException.class)
                .when(fail("Fail once"));

        assertException()
                .exception(CitrusRuntimeException.class)
                .message("Fail again")
                .when(fail("Fail again"));

        assertException()
                .exception(CitrusRuntimeException.class)
                .message("${failMessage}")
                .when(fail("${failMessage}"));

        assertException()
                .exception(CitrusRuntimeException.class)
                .message("@contains('wrong')@")
                .when(fail("${failMessage}"));

        assertException()
                .exception(ValidationException.class)
                .when(assertException()
                        .exception(IOException.class)
                        .when(fail("Fail another time"))
                );

        assertException()
                .exception(ValidationException.class)
                .when(assertException()
                        .exception(CitrusRuntimeException.class)
                        .message("Fail again")
                        .when(fail("Fail with nice error message"))
                );

        assertException()
                .exception(ValidationException.class)
                .when(assertException()
                        .exception(CitrusRuntimeException.class)
                        .when(echo("Nothing fails here"))
                );

        assertException()
                .exception(ValidationException.class)
                .when(assertException()
                        .exception(CitrusRuntimeException.class)
                        .message("Must be failing")
                        .when(echo("Nothing fails here either"))
                );

        assertException()
                .exception(CitrusRuntimeException.class).message("Unknown variable 'foo'")
                .when(new AbstractTestAction() {
                    @Override
                    public void doExecute(TestContext context) {
                        context.getVariable("foo");
                    }
                });
    }
}