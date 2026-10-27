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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestActorTest {

    @Test
    public void shouldBeEnabledByDefault() {
        Assertions.assertFalse(new TestActor().isDisabled());
        Assertions.assertFalse(new TestActor("foo").isDisabled());
        Assertions.assertTrue(new TestActor("foo", true).isDisabled());

        Assertions.assertTrue(new TestActor("foo", true).isDisabled());
    }

    @Test
    public void shouldDisableViaSystemProperty() {
        System.setProperty("citrus.test.actor.foo.enabled", "false");
        try {
            Assertions.assertTrue(new TestActor("foo").isDisabled());
            Assertions.assertFalse(new TestActor("bar").isDisabled());
        } finally {
            System.clearProperty("citrus.test.actor.foo.enabled");
        }
    }

}
