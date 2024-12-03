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

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestActorTest {

    @Test
    public void shouldBeEnabledByDefault() {
        Assert.assertFalse(new TestActor().isDisabled());
        Assert.assertFalse(new TestActor("foo").isDisabled());
        Assert.assertTrue(new TestActor("foo", true).isDisabled());

        Assert.assertTrue(new TestActor("foo", true).isDisabled());
    }

    @Test
    public void shouldDisableViaSystemProperty() {
        System.setProperty("citrus.test.actor.foo.enabled", "false");
        Assert.assertTrue(new TestActor("foo").isDisabled());
        Assert.assertFalse(new TestActor("bar").isDisabled());
    }

}
