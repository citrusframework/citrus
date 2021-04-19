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

package com.consol.citrus.integration.actions;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.EchoAction.Builder.echo;
import static com.consol.citrus.actions.LoadPropertiesAction.Builder.load;
import static com.consol.citrus.script.GroovyAction.Builder.groovy;

/**
 * @author Christoph Deppisch
 */
@Test
public class LoadPropertiesJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void loadPropertiesAction() {
        variable("checkDate", "citrus:currentDate('yyyy-MM-dd')");

        run(load("classpath:com/consol/citrus/integration/actions/load.properties"));

        run(echo("Use variables coming from property file"));

        run(echo("Variables are: ${user}, ${welcomeText}, ${todayDate}"));

        run(echo("Verify variables support (replacement in properties)"));

        run(groovy("import com.consol.citrus.*\n" +
          "import com.consol.citrus.variable.*\n" +
          "import com.consol.citrus.context.TestContext\n" +
          "import com.consol.citrus.script.GroovyAction.ScriptExecutor\n" +
          "import org.testng.Assert;\n" +
          "public class GScript implements ScriptExecutor {\n" +
              "public void execute(TestContext context) {\n" +
                  "Assert.assertEquals(\"${welcomeText}\", \"Hello Mr. X\")\n" +
                  "Assert.assertEquals(\"${todayDate}\", \"${checkDate}\")\n" +
              "}\n" +
          "}\n"));
    }
}
