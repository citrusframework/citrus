/*
 * Copyright 2006-2017 the original author or authors.
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

package org.citrusframework.junit.jupiter.integration;

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.junit.jupiter.CitrusSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;

/**
 * @author Christoph Deppisch
 */
@CitrusSupport
public class ContextInjectionJUnit5RunnerIT {

    @CitrusResource
    private GherkinTestActionRunner runner;

    @CitrusResource
    private TestContext globalContext;

    @Test
    @CitrusTest
    @SuppressWarnings("squid:S2699")
    void contextInjection(@CitrusResource TestContext context) {
        context.setVariable("message", "Injection worked!");

        Assertions.assertEquals(context, globalContext);
        runner.given(echo("${message}"));
    }
}
