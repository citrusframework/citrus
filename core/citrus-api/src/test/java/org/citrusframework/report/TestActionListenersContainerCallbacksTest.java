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

package org.citrusframework.report;

import org.citrusframework.api.container.IteratingActionContainer;
import org.citrusframework.context.TestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TestActionListenersContainerCallbacksTest {

    @Mock(extraInterfaces = TestActionContainerListener.class)
    private TestActionListener containerAwareListenerMock;

    @Mock
    private TestActionListener plainListenerMock;

    @Mock
    private IteratingActionContainer containerMock;

    @Mock
    private TestContext contextMock;

    private final Throwable cause = new IllegalStateException("failed");

    private TestActionListeners fixture;

    @BeforeEach
    void beforeEach() {
        fixture = new TestActionListeners();
        fixture.addTestActionListener(plainListenerMock);
        fixture.addTestActionListener(containerAwareListenerMock);
    }

    @Test
    void onContainerStart_isDispatchedToContainerListenersOnly() {
        fixture.onContainerStart(containerMock, contextMock);

        verify(containerListener()).onContainerStart(containerMock, contextMock);
        verifyNoInteractions(plainListenerMock);
    }

    @Test
    void onContainerFinish_isDispatchedToContainerListenersOnly() {
        fixture.onContainerFinish(containerMock, contextMock);

        verify(containerListener()).onContainerFinish(containerMock, contextMock);
        verifyNoInteractions(plainListenerMock);
    }

    @Test
    void onContainerFailed_isDispatchedToContainerListenersOnly() {
        fixture.onContainerFailed(containerMock, contextMock, cause);

        verify(containerListener()).onContainerFailed(containerMock, contextMock, cause);
        verifyNoInteractions(plainListenerMock);
    }

    @Test
    void onIterationStart_isDispatchedToContainerListenersOnly() {
        fixture.onIterationStart(containerMock, 1, contextMock);

        verify(containerListener()).onIterationStart(containerMock, 1, contextMock);
        verifyNoInteractions(plainListenerMock);
    }

    @Test
    void onIterationFinish_isDispatchedToContainerListenersOnly() {
        fixture.onIterationFinish(containerMock, 1, contextMock);

        verify(containerListener()).onIterationFinish(containerMock, 1, contextMock);
        verifyNoInteractions(plainListenerMock);
    }

    @Test
    void onIterationFailed_isDispatchedToContainerListenersOnly() {
        fixture.onIterationFailed(containerMock, 1, contextMock, cause);

        verify(containerListener()).onIterationFailed(containerMock, 1, contextMock, cause);
        verifyNoInteractions(plainListenerMock);
    }

    private TestActionContainerListener containerListener() {
        return (TestActionContainerListener) containerAwareListenerMock;
    }
}
