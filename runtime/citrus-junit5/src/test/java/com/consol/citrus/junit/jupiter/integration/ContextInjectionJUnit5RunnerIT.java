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

package com.consol.citrus.junit.jupiter.integration;

import com.consol.citrus.GherkinTestActionRunner;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.junit.jupiter.CitrusExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.consol.citrus.actions.EchoAction.Builder.echo;

/**
 * @author Christoph Deppisch
 */
@ExtendWith(CitrusExtension.class)
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
