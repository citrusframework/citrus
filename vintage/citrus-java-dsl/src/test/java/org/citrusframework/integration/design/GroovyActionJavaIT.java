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

package org.citrusframework.integration.design;

import org.citrusframework.dsl.testng.TestNGCitrusTestDesigner;
import org.citrusframework.annotations.CitrusTest;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class GroovyActionJavaIT extends TestNGCitrusTestDesigner {

    /** OS new line */
    private static final String NEWLINE = System.getProperty("line.separator");

    @CitrusTest
    public void groovyAction() {
        variable("date", "citrus:currentDate()");
        variable("greetingText", "Hello Citrus!");

        description("This example executes groovy scripts using both inline script definition and external file resource.");

        groovy("println 'Hello Citrus'");

        groovy("println 'Current date is ${date}!'");

        groovy("import org.citrusframework.*" + NEWLINE +
                "import org.citrusframework.variable.*" + NEWLINE +
                "import org.citrusframework.context.TestContext" + NEWLINE +
                "import org.citrusframework.script.GroovyAction.ScriptExecutor" + NEWLINE +
                "import org.testng.Assert" + NEWLINE +
                "public class GScript implements ScriptExecutor {" + NEWLINE +
                    "public void execute(TestContext context) {" + NEWLINE +
                        "Assert.assertEquals(context.getVariable(\"greetingText\"), \"Hello Citrus!\")" + NEWLINE +
                        "Assert.assertEquals(context.getVariable(\"greetingText\"), \"${greetingText}\")" + NEWLINE +
                    "}" + NEWLINE +
                "}");

        groovy("println context.getVariable(\"date\")" + NEWLINE +
                "assert context.getVariable(\"greetingText\").equals(\"Hello Citrus!\")" + NEWLINE +
                "assert context.getVariable(\"greetingText\").equals(\"${greetingText}\")");

        groovy("println 'Hello Citrus'").skipTemplate();

        groovy("Assert.assertEquals(context.getVariable(\"scriptTemplateVar\"), \"It works!\")" + NEWLINE +
                "Assert.assertEquals(context.getVariable(\"greetingText\"), \"Hello Citrus!\")" + NEWLINE +
                "Assert.assertEquals(context.getVariable(\"greetingText\"), \"${greetingText}\")")
                .template("classpath:org/citrusframework/script/custom-script-template.groovy");

        groovy("import org.testng.Assert" + NEWLINE +
                  "Assert.assertEquals(context.getVariable(\"scriptTemplateVar\"), \"It works!\")" + NEWLINE +
                  "Assert.assertEquals(context.getVariable(\"greetingText\"), \"Hello Citrus!\")" + NEWLINE +
                  "Assert.assertEquals(context.getVariable(\"greetingText\"), \"${greetingText}\")");

        groovy("public class MyCustomClass {" + NEWLINE +
                    "public void run() {" + NEWLINE +
                        "println 'Just executed a custom class with run method!'" + NEWLINE +
                    "}" + NEWLINE +
                  "}");

        groovy(new ClassPathResource("org/citrusframework/script/example.groovy"));
    }
}
