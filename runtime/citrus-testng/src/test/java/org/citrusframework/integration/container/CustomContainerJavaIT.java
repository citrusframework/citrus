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

package org.citrusframework.integration.container;

import org.citrusframework.TestActionContainerBuilder;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.container.AbstractActionContainer;
import org.citrusframework.context.TestContext;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.annotations.Test;

import static org.citrusframework.AbstractTestContainerBuilder.container;
import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariable;
import static org.citrusframework.actions.EchoAction.Builder.echo;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
@Test
public class CustomContainerJavaIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void shouldExecuteReverseContainer() {
        run(reverse().actions(
            echo("${text}"),
            echo("Does it work?"),
            createVariable("text", "Yes it works!")
        ));
    }

    public TestActionContainerBuilder<ReverseActionContainer, ?> reverse() {
        return container(new ReverseActionContainer());
    }

    private static class ReverseActionContainer extends AbstractActionContainer {
        @Override
        public void doExecute(TestContext context) {
            for (int i = getActions().size(); i > 0; i--) {
                executeAction(getActions().get(i - 1), context);
            }
        }
    }
}
