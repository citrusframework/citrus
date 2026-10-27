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

package org.citrusframework.util;

import java.util.function.Supplier;

import org.citrusframework.TestActionRunner;
import org.citrusframework.TestBehavior;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BehaviorNamesTest {

    public static Object[][] namedTypes() {
        return new Object[][] {
                { PlaceAnOrder.class, "place an order" },
                { SendSOAPRequest.class, "send SOAP request" },
                { SendAMessage.class, "send a message" },
                { SOAP.class, "SOAP" },
                { Order2Items.class, "order2 items" },
                { Send_Order.class, "send order" },
                { SayHello.class, "say hello" }
        };
    }

    @ParameterizedTest
    @MethodSource("namedTypes")
    public void shouldDeCamelCaseNamedClass(Class<?> type, String expected) {
        assertEquals(expected, BehaviorNames.of(type));
    }

    @Test
    public void shouldUseSimpleNameOfRecord() {
        assertEquals("say hello record", BehaviorNames.of(SayHelloRecord.class));
    }

    @Test
    public void shouldDropEnclosingClassOfNestedClass() {
        assertEquals("say hello", BehaviorNames.of(Outer.SayHello.class));
    }

    @Test
    public void shouldUseSimpleNameOfLocalClass() {
        class CheckTheStock {}

        assertEquals("check the stock", BehaviorNames.of(CheckTheStock.class));
    }

    @Test
    public void shouldUseGenericNameForLambda() {
        Supplier<String> lambda = () -> "foo";

        assertEquals(BehaviorNames.GENERIC_NAME, BehaviorNames.of(lambda.getClass()));
        assertEquals("behavior", BehaviorNames.of(lambda.getClass()));
    }

    @Test
    public void shouldUseGenericNameForAnonymousClass() {
        Supplier<String> anonymous = new Supplier<>() {
            @Override
            public String get() {
                return "foo";
            }
        };

        assertEquals("behavior", BehaviorNames.of(anonymous.getClass()));
    }

    @Test
    public void shouldPreferExplicitName() {
        assertEquals("sign in", TestBehavior.named("sign in", runner -> {}).getName());
    }

    @Test
    public void shouldNameCompositeRecursively() {
        TestBehavior signIn = TestBehavior.named("sign in", runner -> {});
        TestBehavior placeAnOrder = new PlaceAnOrderBehavior();

        assertEquals("sign in then place an order behavior", signIn.andThen(placeAnOrder).getName());
        assertEquals("sign in then place an order behavior then sign in",
                signIn.andThen(placeAnOrder.andThen(signIn)).getName());
    }

    @Test
    public void shouldIgnoreGeneratedSuffix() {
        assertEquals("place an order", BehaviorNames.of(PlaceAnOrder$$SpringCGLIB$$0.class));
    }

    @Test
    public void shouldUseGenericNameForMissingType() {
        assertEquals("behavior", BehaviorNames.of(null));
    }

    private static class PlaceAnOrder {}

    private static class SendSOAPRequest {}

    private static class SendAMessage {}

    private static class SOAP {}

    private static class Order2Items {}

    private static class Send_Order {}

    private static class SayHello {}

    private record SayHelloRecord(String greeting) {}

    private static class Outer {
        private static class SayHello {}
    }

    private static class PlaceAnOrder$$SpringCGLIB$$0 {}

    private static class PlaceAnOrderBehavior implements TestBehavior {
        @Override
        public void apply(TestActionRunner runner) {
        }
    }
}
