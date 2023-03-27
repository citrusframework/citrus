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

package org.citrusframework.citrus.integration.design;

import org.citrusframework.citrus.dsl.testng.TestNGCitrusTestDesigner;
import org.citrusframework.citrus.annotations.CitrusTest;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class LoadPropertiesJavaIT extends TestNGCitrusTestDesigner {

    @CitrusTest
    public void loadPropertiesAction() {
        variable("checkDate", "citrus:currentDate('yyyy-MM-dd')");

        load("classpath:org/citrusframework/citrus/actions/load.properties");

        echo("Use variables coming from property file");

        echo("Variables are: ${user}, ${welcomeText}, ${todayDate}");

        echo("Verify variables support (replacement in properties)");

        groovy("import org.citrusframework.citrus.*\n" +
          "import org.citrusframework.citrus.variable.*\n" +
          "import org.citrusframework.citrus.context.TestContext\n" +
          "import org.citrusframework.citrus.script.GroovyAction.ScriptExecutor\n" +
          "import org.testng.Assert;\n" +
          "public class GScript implements ScriptExecutor {\n" +
              "public void execute(TestContext context) {\n" +
                  "Assert.assertEquals(\"${welcomeText}\", \"Hello Mr. X\")\n" +
                  "Assert.assertEquals(\"${todayDate}\", \"${checkDate}\")\n" +
              "}\n" +
          "}\n");
    }
}
