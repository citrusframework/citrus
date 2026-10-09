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

package org.citrusframework.junit.jupiter.integration;

import java.util.List;

import org.citrusframework.TestActionRunner;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.TestBehavior;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.junit.jupiter.CitrusExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.citrusframework.junit.jupiter.integration.ApplyTestBehaviorIT.PlaceAnOrder.placeAnOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(CitrusExtension.class)
public class ApplyTestBehaviorIT implements TestActionSupport {

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

    @Test
    @CitrusTest
    public void shouldApplyInContainerWithoutRunner(@CitrusResource TestActionRunner runner) {
        runner.run(sequential()
                .actions(
                        echo("In Germany they say:"),
                        apply(new SayHelloBehavior("Hallo")),
                        echo("In Spain they say:"),
                        apply().behavior(new SayHelloBehavior("Hola"))
                ));
    }

    @Test
    @CitrusTest
    public void shouldApplyComposedBehavior(@CitrusResource TestActionRunner runner) {
        TestBehavior sayGoodbye = TestBehavior.named("say goodbye", behavior -> behavior.run(echo("Goodbye Citrus!")));

        runner.run(apply(new SayHelloBehavior().andThen(sayGoodbye)));

        runner.run(apply(List.of(new SayHelloBehavior("Hi"), sayGoodbye)));
    }

    @Test
    @CitrusTest
    public void shouldApplyIsolatedBehavior(@CitrusResource TestActionRunner runner, @CitrusResource TestContext context) {
        runner.run(createVariable("greeting", "Hello"));

        runner.run(apply(new GreetAndCount("Hi"))
                .requires("greeting")
                .isolated()
                .publish("greetings"));

        assertEquals("Hello", context.getVariable("greeting"));
        assertEquals("1", context.getVariable("greetings"));
        assertFalse(context.getVariables().containsKey("scratch"));
    }

    @Test
    @CitrusTest
    public void shouldApplyFluentBehavior(@CitrusResource TestActionRunner runner, @CitrusResource TestContext context) {
        runner.run(createVariable("sku", "SKU-1"));

        runner.run(apply(placeAnOrder().forItem("${sku}").inQuantity(5)));

        assertEquals("5 x SKU-1", context.getVariable("order"));

        runner.run(sequential()
                .actions(
                        apply(placeAnOrder().forItem("SKU-2"))
                ));

        assertEquals("1 x SKU-2", context.getVariable("order"));
    }

    private record SayHelloBehavior(String greeting) implements TestBehavior, TestActionSupport {
            public SayHelloBehavior() {
                this("Hello");
            }

        @Override
            public void apply(TestActionRunner runner) {
                runner.run(echo(String.format("%s Citrus!", greeting)));
            }
        }

    private record GreetAndCount(String greeting) implements TestBehavior, TestActionSupport {

        @Override
        public void apply(TestActionRunner runner) {
            runner.run(echo("${greeting} Citrus!"));
            runner.run(createVariables()
                    .variable("greeting", greeting)
                    .variable("greetings", "1")
                    .variable("scratch", "not published"));
            runner.run(echo("${greeting} Citrus!"));
        }
    }

    record PlaceAnOrder(String item, int quantity) implements TestBehavior, TestActionSupport {

        static PlaceAnOrder placeAnOrder() {
            return new PlaceAnOrder(null, 1);
        }

        PlaceAnOrder forItem(String item) {
            return new PlaceAnOrder(item, quantity);
        }

        PlaceAnOrder inQuantity(int quantity) {
            return new PlaceAnOrder(item, quantity);
        }

        @Override
        public void apply(TestActionRunner runner) {
            if (item == null) {
                throw new CitrusRuntimeException("Missing item to order - use forItem(..)");
            }

            runner.run(echo("Ordering %d x %s".formatted(quantity, item)));
            runner.run(createVariable("order", "%d x %s".formatted(quantity, item)));
        }
    }
}
