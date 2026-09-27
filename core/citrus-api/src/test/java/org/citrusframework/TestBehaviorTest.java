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

package org.citrusframework;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

public class TestBehaviorTest {

    private final TestActionRunner runner = mock(TestActionRunner.class);

    @Test
    public void shouldStayAssignableFromLambda() {
        TestBehavior behavior = runner -> {};

        behavior.apply(runner);
    }

    @Test
    public void shouldNameNamedClass() {
        assertEquals(new PlaceAnOrder(new ArrayList<>()).getName(), "place an order");
    }

    @Test
    public void shouldNameLambdaGenerically() {
        TestBehavior behavior = runner -> {};

        assertEquals(behavior.getName(), "behavior");
    }

    @Test
    public void shouldUseExplicitName() {
        List<String> applied = new ArrayList<>();
        TestBehavior behavior = TestBehavior.named("sign in", runner -> applied.add("signed in"));

        assertEquals(behavior.getName(), "sign in");

        behavior.apply(runner);
        assertEquals(applied, List.of("signed in"));
    }

    @Test
    public void shouldRejectMissingName() {
        assertThrows(IllegalArgumentException.class, () -> TestBehavior.named(" ", runner -> {}));
        assertThrows(NullPointerException.class, () -> TestBehavior.named("sign in", null));
    }

    @Test
    public void shouldComposeInOrder() {
        List<String> applied = new ArrayList<>();
        TestBehavior composite = TestBehavior.named("sign in", runner -> applied.add("signed in"))
                .andThen(new PlaceAnOrder(applied));

        composite.apply(runner);

        assertEquals(applied, List.of("signed in", "order placed"));
    }

    @Test
    public void shouldNameComposite() {
        TestBehavior signIn = TestBehavior.named("sign in", runner -> {});
        TestBehavior checkOut = TestBehavior.named("check out", runner -> {});

        assertEquals(signIn.andThen(new PlaceAnOrder(new ArrayList<>())).getName(), "sign in then place an order");
        assertEquals(signIn.andThen(new PlaceAnOrder(new ArrayList<>())).andThen(checkOut).getName(),
                "sign in then place an order then check out");
    }

    @Test
    public void shouldRejectMissingNextBehavior() {
        TestBehavior behavior = runner -> {};

        assertThrows(NullPointerException.class, () -> behavior.andThen(null));
    }

    private record PlaceAnOrder(List<String> applied) implements TestBehavior {
        @Override
        public void apply(TestActionRunner runner) {
            applied.add("order placed");
        }
    }
}
