/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.citrus.integration.dsl;

import java.io.IOException;

import org.citrusframework.citrus.TestActionRunner;
import org.citrusframework.citrus.annotations.CitrusResource;
import org.citrusframework.citrus.annotations.CitrusTest;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.groovy.dsl.actions.ActionsScript;
import org.citrusframework.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.citrus.util.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class ActionsScriptIT extends TestNGCitrusSpringSupport {

    @Test
    @Parameters({"runner", "context"})
    @CitrusTest
    public void shouldRunActionsScript(@Optional @CitrusResource TestActionRunner runner,
                                       @Optional @CitrusResource TestContext context) throws IOException {
        ActionsScript script = new ActionsScript(FileUtils.readToString(new ClassPathResource("org/citrusframework/citrus/groovy/dsl/actions.groovy")), citrus);
        script.execute(runner, context);
    }
}
