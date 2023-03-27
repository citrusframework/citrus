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

package org.citrusframework.citrus.integration.actions;

import org.citrusframework.citrus.annotations.CitrusTest;
import org.citrusframework.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import static org.citrusframework.citrus.actions.CreateVariablesAction.Builder.createVariable;
import static org.citrusframework.citrus.actions.EchoAction.Builder.echo;

/**
 * @author Christoph Deppisch
 */
@Test
public class CreateVariablesJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void createVariablesAction() {
        variable("myVariable", "12345");
        variable("newValue", "54321");

        run(echo("Current variable value: ${myVariable}"));

        run(createVariable("myVariable", "${newValue}"));
        run(createVariable("new", "This is a test"));

        run(echo("Current variable value: ${myVariable}"));

        run(echo("New variable 'new' has the value: ${new}"));

        run(createVariable("foo", "bar"));

        run(echo("foo = '${foo}'"));
    }
}
