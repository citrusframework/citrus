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

package com.consol.citrus.junit.jupiter.integration;

import com.consol.citrus.TestActionRunner;
import com.consol.citrus.TestBehavior;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.junit.jupiter.CitrusSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.consol.citrus.actions.ApplyTestBehaviorAction.Builder.apply;
import static com.consol.citrus.actions.EchoAction.Builder.echo;
import static com.consol.citrus.container.Sequence.Builder.sequential;

/**
 * @author Christoph Deppisch
 */
@ExtendWith(CitrusSupport.class)
public class ApplyTestBehaviorIT {

    @Test
    @CitrusTest
    public void shouldApply(@CitrusResource TestActionRunner runner) {
        runner.run(apply(new SayHelloBehavior()));

        runner.run(runner.applyBehavior(new SayHelloBehavior("Hi")));
    }

    @Test
    @CitrusTest
    public void shouldApplyInContainer(@CitrusResource TestActionRunner runner) {
        runner.run(sequential()
                .actions(
                        echo("In Germany they say:"),
                        apply().behavior(new SayHelloBehavior("Hallo")).on(runner),
                        echo("In Spain they say:"),
                        runner.applyBehavior(new SayHelloBehavior("Hola"))
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

