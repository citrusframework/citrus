/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.junit.integration;

import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.junit.spring.JUnit4CitrusSpringSupport;
import org.junit.Assert;
import org.junit.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class ContextInjectionJUnit4IT extends JUnit4CitrusSpringSupport {

    @CitrusResource
    private TestContext globalContext;

    @Test
    @CitrusTest
    @SuppressWarnings("squid:S2699")
    public void contextInjection(@CitrusResource TestContext context) {
        context.setVariable("message", "Injection worked!");
        Assert.assertEquals(globalContext, context);
        run(echo("${message}"));
    }
}
