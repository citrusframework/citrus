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

import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.context.TestContext;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultTestActionBuilderTest {

    @Test
    public void build_withDelegateName_perDefault() {
        TestAction delegate = delegateAction("delegate-name");

        TestAction action = new DefaultTestActionBuilder(delegate).build();

        assertThat(action.getName())
                .isEqualTo("delegate-name");
    }

    @Test
    public void build_withName_overridesDelegateName() {
        TestAction delegate = delegateAction("delegate-name");

        TestAction action = new DefaultTestActionBuilder(delegate)
                .name("custom-name")
                .build();

        assertThat(action.getName())
                .isEqualTo("custom-name");
    }

    @Test
    public void build_withDelegateActor_perDefault() {
        TestActor delegateActor = new TestActor("delegate-actor");
        TestAction delegate = delegateAction("delegate-name", delegateActor);

        TestAction action = new DefaultTestActionBuilder(delegate).build();

        assertThat(action.getActor())
                .isSameAs(delegateActor);
    }

    @Test
    public void build_withActor_overridesDelegateActor() {
        TestActor delegateActor = new TestActor("delegate-actor");
        TestActor customActor = new TestActor("custom-actor");
        TestAction delegate = delegateAction("delegate-name", delegateActor);

        TestAction action = new DefaultTestActionBuilder(delegate)
                .actor(customActor)
                .build();

        assertThat(action.getActor())
                .isSameAs(customActor);
    }

    private TestAction delegateAction(String name) {
        return delegateAction(name, null);
    }

    private TestAction delegateAction(String name, TestActor actor) {
        return new AbstractTestAction() {

            @Override
            public String getName() {
                return name;
            }

            @Override
            public void doExecute(TestContext context) {
                // no-op
            }
        }.setActor(actor);
    }
}
