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

package org.citrusframework.groovy.integration.actions;

import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class LoadPropertiesJavaIT implements TestActionSupport {

    @Test
    @CitrusTest
    public void loadPropertiesAction(@CitrusResource TestCaseRunner runner) {
        runner.variable("checkDate", "citrus:currentDate('yyyy-MM-dd')");

        runner.run(load("classpath:org/citrusframework/groovy/integration/actions/load.properties"));

        runner.run(echo("Use variables coming from property file"));

        runner.run(echo("Variables are: ${user}, ${welcomeText}, ${todayDate}"));

        runner.run(echo("Verify variables support (replacement in properties)"));

        runner.run(script().groovy("""
                import org.citrusframework.*
                import org.citrusframework.variable.*
                import org.citrusframework.context.TestContext
                import org.citrusframework.groovy.actions.GroovyAction.ScriptExecutor
                import org.testng.Assert;
                
                public class GScript implements ScriptExecutor {
                    public void execute(TestContext context) {
                        Assert.assertEquals("${welcomeText}", "Hello Mr. X")
                        Assert.assertEquals("${todayDate}", "${checkDate}")
                    }
                }
                """));
    }
}
