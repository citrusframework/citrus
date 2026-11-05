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

package org.citrusframework.groovy.integration.dsl;

import java.io.IOException;

import org.citrusframework.Citrus;
import org.citrusframework.TestActionRunner;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.groovy.dsl.actions.ActionsScript;
import org.citrusframework.spi.Resources;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.citrusframework.util.FileUtils;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class ActionsScriptIT {

    @CitrusFramework
    private Citrus citrus;

    @Test
    @CitrusTest
    public void shouldRunActionsScript(@CitrusResource TestActionRunner runner,
                                       @CitrusResource TestContext context) throws IOException {
        ActionsScript script = new ActionsScript(FileUtils.readToString(Resources.fromClasspath("org/citrusframework/groovy/dsl/actions.groovy")), citrus);
        script.execute(runner, context);
    }
}
