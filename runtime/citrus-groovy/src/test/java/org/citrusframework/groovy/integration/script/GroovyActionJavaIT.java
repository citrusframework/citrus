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

package org.citrusframework.groovy.integration.script;

import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.spi.Resources;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class GroovyActionJavaIT implements TestActionSupport {

    /** OS new line */
    private static final String NEWLINE = System.lineSeparator();

    @Test
    @CitrusTest
    public void groovyAction(@CitrusResource TestCaseRunner runner) {
        runner.variable("date", "citrus:currentDate()");
        runner.variable("greetingText", "Hello Citrus!");

        runner.description("This example executes groovy scripts using both inline script definition and external file resource.");

        runner.run(script().groovy("println 'Hello Citrus'"));

        runner.run(script().groovy("println 'Current date is ${date}!'"));

        runner.run(script().groovy("import org.citrusframework.*" + NEWLINE +
                "import org.citrusframework.variable.*" + NEWLINE +
                "import org.citrusframework.context.TestContext" + NEWLINE +
                "import org.citrusframework.groovy.actions.GroovyAction.ScriptExecutor" + NEWLINE +
                "import org.testng.Assert" + NEWLINE +
                "public class GScript implements ScriptExecutor {" + NEWLINE +
                    "public void execute(TestContext context) {" + NEWLINE +
                        "Assert.assertEquals(context.getVariable(\"greetingText\"), \"Hello Citrus!\")" + NEWLINE +
                        "Assert.assertEquals(context.getVariable(\"greetingText\"), \"${greetingText}\")" + NEWLINE +
                    "}" + NEWLINE +
                "}"));

        runner.run(script().groovy("println context.getVariable(\"date\")" + NEWLINE +
                "assert context.getVariable(\"greetingText\").equals(\"Hello Citrus!\")" + NEWLINE +
                "assert context.getVariable(\"greetingText\").equals(\"${greetingText}\")"));

        runner.run(script().groovy("println 'Hello Citrus'").skipTemplate());

        runner.run(script().groovy("Assert.assertEquals(context.getVariable(\"scriptTemplateVar\"), \"It works!\")" + NEWLINE +
                "Assert.assertEquals(context.getVariable(\"greetingText\"), \"Hello Citrus!\")" + NEWLINE +
                "Assert.assertEquals(context.getVariable(\"greetingText\"), \"${greetingText}\")")
                .template("classpath:org/citrusframework/groovy/integration/script/custom-script-template.groovy"));

        runner.run(script().groovy("import org.testng.Assert" + NEWLINE +
                  "Assert.assertEquals(context.getVariable(\"scriptTemplateVar\"), \"It works!\")" + NEWLINE +
                  "Assert.assertEquals(context.getVariable(\"greetingText\"), \"Hello Citrus!\")" + NEWLINE +
                  "Assert.assertEquals(context.getVariable(\"greetingText\"), \"${greetingText}\")"));

        runner.run(script().groovy("public class MyCustomClass {" + NEWLINE +
                    "public void run() {" + NEWLINE +
                        "println 'Just executed a custom class with run method!'" + NEWLINE +
                    "}" + NEWLINE +
                  "}"));

        runner.run(script().groovy(Resources.fromClasspath("org/citrusframework/groovy/integration/script/example.groovy")));
    }
}
