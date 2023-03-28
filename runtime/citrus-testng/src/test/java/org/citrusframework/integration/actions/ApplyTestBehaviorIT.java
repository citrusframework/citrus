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

package org.citrusframework.integration.actions;

import java.util.concurrent.atomic.AtomicInteger;

import org.citrusframework.TestActionRunner;
import org.citrusframework.TestBehavior;
import org.citrusframework.actions.ApplyTestBehaviorAction;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.container.Sequence;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.ApplyTestBehaviorAction.Builder.apply;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.container.FinallySequence.Builder.doFinally;
import static org.citrusframework.container.Sequence.Builder.sequential;

/**
 * @author Christoph Deppisch
 */
@Test
public class ApplyTestBehaviorIT extends TestNGCitrusSpringSupport {

    @CitrusTest
    public void shouldApply() {
        run(apply(new SayHelloBehavior()));

        run(echo("Hipsters say:"));
        run(applyBehavior(new SayHelloBehavior("Hi")));
    }

    @CitrusTest
    public void shouldApplyTwice() {
        AtomicInteger invocationCount = new AtomicInteger();
        TestBehavior behavior = runner -> invocationCount.getAndIncrement();

        run(apply(behavior));
        run(applyBehavior(behavior));

        Assert.assertEquals(invocationCount.get(), 2L);
    }

    @CitrusTest
    public void shouldApplyInContainer() {
        Sequence sequence = run(sequential()
                .actions(
                        echo("In Germany they say:"),
                        apply().behavior(new SayHelloBehavior("Hallo")).on(this),
                        echo("In Spain they say:"),
                        applyBehavior(new SayHelloBehavior("Hola"))
                ));

        Assert.assertEquals(sequence.getActionCount(), 4);

        Assert.assertEquals(sequence.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)sequence.getActions().get(0)).getMessage(), "In Germany they say:");

        Assert.assertEquals(sequence.getActions().get(1).getClass(), ApplyTestBehaviorAction.class);

        Assert.assertEquals(sequence.getActions().get(2).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)sequence.getActions().get(2)).getMessage(), "In Spain they say:");

        Assert.assertEquals(sequence.getActions().get(3).getClass(), ApplyTestBehaviorAction.class);
    }

    @CitrusTest
    public void shouldApplyInContainerTwice() {
        SayHelloBehavior sayHello = new SayHelloBehavior();

        Sequence sequence = run(sequential()
                .actions(
                        echo("before"),
                        apply().behavior(sayHello).on(this),
                        echo("after"),
                        applyBehavior(sayHello)
                ));

        Assert.assertEquals(sequence.getActionCount(), 4);

        Assert.assertEquals(sequence.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)sequence.getActions().get(0)).getMessage(), "before");

        Assert.assertEquals(sequence.getActions().get(1).getClass(), ApplyTestBehaviorAction.class);

        Assert.assertEquals(sequence.getActions().get(2).getClass(), EchoAction.class);
        Assert.assertEquals(((EchoAction)sequence.getActions().get(2)).getMessage(), "after");

        Assert.assertEquals(sequence.getActions().get(3).getClass(), ApplyTestBehaviorAction.class);
    }

    @CitrusTest
    public void shouldApplyWithFinally() {
        run(echo("starting"));

        run(doFinally()
                .actions(echo("finally")));

        run(applyBehavior(runner -> {
            runner.run(doFinally()
                    .actions(echo("finally in behavior")));
            runner.run(echo("behavior"));
        }));
    }

    @CitrusTest
    public void shouldApplyInContainerWithFinally() {
        run(sequential()
                .actions(
                        applyBehavior(runner -> {
                            runner.run(doFinally()
                                    .actions(echo("Finally say GoodBye!")));
                        }),
                        echo("In Germany they say:"),
                        apply().behavior(new SayHelloBehavior("Hallo")).on(this),
                        echo("In Spain they say:"),
                        applyBehavior(new SayHelloBehavior("Hola"))
                ));
    }

    @CitrusTest
    public void shouldApplyRecursive() {
        run(applyBehavior(new InceptionBehavior()));

        run(applyBehavior(new InceptionBehavior("Hi")));

        run(sequential()
                .actions(
                        echo("In Germany they say:"),
                        apply().behavior(new InceptionBehavior("Hallo")).on(this),
                        echo("In Spain they say:"),
                        applyBehavior(new InceptionBehavior("Hola"))
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

    private static class InceptionBehavior implements TestBehavior {
        private final String greeting;

        public InceptionBehavior() {
            this("Hello");
        }

        public InceptionBehavior(String greeting) {
            this.greeting = greeting;
        }

        @Override
        public void apply(TestActionRunner runner) {
            runner.applyBehavior(new SayHelloBehavior(greeting));

            runner.run(sequential()
                    .actions(
                            echo("Now try inception:"),
                            runner.applyBehavior(new SayHelloBehavior(greeting))
                    ));
        }
    }
}

