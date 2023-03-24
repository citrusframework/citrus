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

package org.citrusframework.integration.container;

import java.io.IOException;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.FailAction.Builder.fail;
import static org.citrusframework.container.Assert.Builder.assertException;

/**
 * @author Christoph Deppisch
 */
@Test
public class AssertJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void assertAction() {
        variable("failMessage", "Something went wrong!");

        run(assertException()
                .exception(CitrusRuntimeException.class)
                .when(fail("Fail once")));

        run(assertException()
                .exception(CitrusRuntimeException.class)
                .message("Fail again")
                .when(fail("Fail again")));

        run(assertException()
                .exception(CitrusRuntimeException.class)
                .message("${failMessage}")
                .when(fail("${failMessage}")));

        run(assertException()
                .exception(CitrusRuntimeException.class)
                .message("@contains('wrong')@")
                .when(fail("${failMessage}")));

        run(assertException()
                .exception(ValidationException.class)
                .when(assertException()
                        .exception(IOException.class)
                        .when(fail("Fail another time"))
                ));

        run(assertException()
                .exception(ValidationException.class)
                .when(assertException()
                        .exception(CitrusRuntimeException.class)
                        .message("Fail again")
                        .when(fail("Fail with nice error message"))
                ));

        run(assertException()
                .exception(ValidationException.class)
                .when(assertException()
                        .exception(CitrusRuntimeException.class)
                        .when(echo("Nothing fails here"))
                ));

        run(assertException()
                .exception(ValidationException.class)
                .when(assertException()
                        .exception(CitrusRuntimeException.class)
                        .message("Must be failing")
                        .when(echo("Nothing fails here either"))
                ));

        run(assertException()
                .exception(CitrusRuntimeException.class).message("Unknown variable 'foo'")
                .when(action(context -> context.getVariable("foo"))));
    }
}
