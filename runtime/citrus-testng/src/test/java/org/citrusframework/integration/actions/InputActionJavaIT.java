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

package org.citrusframework.integration.actions;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.InputAction.Builder.input;

/**
 * @author Christoph Deppisch
 */
@Test
public class InputActionJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void inputAction() {
        variable("userinput", "");
        variable("userinput1", "");
        variable("userinput2", "y");
        variable("userinput3", "yes");
        variable("userinput4", "");

        run(input());
        run(echo("user input was: ${userinput}"));
        run(input().message("Now press enter:").result("userinput1"));
        run(echo("user input was: ${userinput1}"));
        run(input().message("Do you want to continue?").answers("y", "n").result("userinput2"));
        run(echo("user input was: ${userinput2}"));
        run(input().message("Do you want to continue?").answers("yes", "no").result("userinput3"));
        run(echo("user input was: ${userinput3}"));
        run(input().result("userinput4"));
        run(echo("user input was: ${userinput4}"));
    }
}
