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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class BehaviorNamesTest {

    @DataProvider
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

    @Test(dataProvider = "namedTypes")
    public void shouldDeCamelCaseNamedClass(Class<?> type, String expected) {
        assertEquals(BehaviorNames.of(type), expected);
    }

    @Test
    public void shouldUseSimpleNameOfRecord() {
        assertEquals(BehaviorNames.of(SayHelloRecord.class), "say hello record");
    }

    @Test
    public void shouldDropEnclosingClassOfNestedClass() {
        assertEquals(BehaviorNames.of(Outer.SayHello.class), "say hello");
    }

    @Test
    public void shouldUseSimpleNameOfLocalClass() {
        class CheckTheStock {}

        assertEquals(BehaviorNames.of(CheckTheStock.class), "check the stock");
    }

    @Test
    public void shouldUseGenericNameForLambda() {
        Supplier<String> lambda = () -> "foo";

        assertEquals(BehaviorNames.of(lambda.getClass()), BehaviorNames.GENERIC_NAME);
        assertEquals(BehaviorNames.of(lambda.getClass()), "behavior");
    }

    @Test
    public void shouldUseGenericNameForAnonymousClass() {
        Supplier<String> anonymous = new Supplier<>() {
            @Override
            public String get() {
                return "foo";
            }
        };

        assertEquals(BehaviorNames.of(anonymous.getClass()), "behavior");
    }

    @Test
    public void shouldPreferExplicitName() {
        assertEquals(TestBehavior.named("sign in", runner -> {}).getName(), "sign in");
    }

    @Test
    public void shouldNameCompositeRecursively() {
        TestBehavior signIn = TestBehavior.named("sign in", runner -> {});
        TestBehavior placeAnOrder = new PlaceAnOrderBehavior();

        assertEquals(signIn.andThen(placeAnOrder).getName(), "sign in then place an order behavior");
        assertEquals(signIn.andThen(placeAnOrder.andThen(signIn)).getName(),
                "sign in then place an order behavior then sign in");
    }

    @Test
    public void shouldIgnoreGeneratedSuffix() {
        assertEquals(BehaviorNames.of(PlaceAnOrder$$SpringCGLIB$$0.class), "place an order");
    }

    @Test
    public void shouldUseGenericNameForMissingType() {
        assertEquals(BehaviorNames.of(null), "behavior");
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
