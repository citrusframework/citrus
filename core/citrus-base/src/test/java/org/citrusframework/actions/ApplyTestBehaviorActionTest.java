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

package org.citrusframework.actions;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestBehavior;
import org.citrusframework.base.UnitTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ApplyTestBehaviorActionTest extends UnitTestSupport {

    @Mock
    private TestActionRunner runner;

    @Mock
    private TestAction mock;

    @Test
    public void shouldApply() {
        ApplyTestBehaviorAction applyBehavior = new ApplyTestBehaviorAction.Builder()
                .behavior(new FooBehavior())
                .on(runner)
                .build();
        applyBehavior.execute(context);

        verify(mock).execute(context);
        assertEquals(1, applyBehavior.getExecutedActions().size());
        assertSame(mock, applyBehavior.getExecutedActions().get(0));
    }

    private class FooBehavior implements TestBehavior {
        @Override
        public void apply(TestActionRunner runner) {
            runner.run(mock);
        }
    }
}
