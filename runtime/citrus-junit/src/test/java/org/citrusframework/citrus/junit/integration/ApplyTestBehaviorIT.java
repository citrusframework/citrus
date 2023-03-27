/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.citrus.junit.integration;

import org.citrusframework.citrus.TestActionRunner;
import org.citrusframework.citrus.TestBehavior;
import org.citrusframework.citrus.annotations.CitrusTest;
import org.citrusframework.citrus.junit.spring.JUnit4CitrusSpringSupport;
import org.junit.Test;

import static org.citrusframework.citrus.actions.ApplyTestBehaviorAction.Builder.apply;
import static org.citrusframework.citrus.actions.EchoAction.Builder.echo;
import static org.citrusframework.citrus.container.Sequence.Builder.sequential;

/**
 * @author Christoph Deppisch
 */
public class ApplyTestBehaviorIT extends JUnit4CitrusSpringSupport {

    @Test
    @CitrusTest
    public void shouldApply() {
        run(apply(new SayHelloBehavior()));

        run(applyBehavior(new SayHelloBehavior("Hi")));
    }

    @Test
    @CitrusTest
    public void shouldApplyInContainer() {
        run(sequential()
                .actions(
                        echo("In Germany they say:"),
                        apply().behavior(new SayHelloBehavior("Hallo")).on(this),
                        echo("In Spain they say:"),
                        applyBehavior(new SayHelloBehavior("Hola"))
                ));
    }

    private static class SayHelloBehavior implements TestBehavior {
        private final String greeting;

        public SayHelloBehavior() {
            this("Hello");
        }

        public SayHelloBehavior(String greeting) {
            this.greeting = greeting;
        }

        @Override
        public void apply(TestActionRunner runner) {
            runner.run(echo(String.format("%s Citrus!", greeting)));
        }
    }
}

