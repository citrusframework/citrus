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

package org.citrusframework.dsl;

import java.util.stream.Collectors;

import org.citrusframework.AbstractTestContainerBuilder;
import org.citrusframework.DefaultTestActionBuilder;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActionContainerBuilder;
import org.citrusframework.TestActionContainers;
import org.citrusframework.actions.*;
import org.citrusframework.condition.Condition;
import org.citrusframework.container.*;

public interface BaseTestActionSupport extends BaseTestActions, TestActionContainers {

    @Override
    default DefaultTestActionBuilder action(TestAction action) {
        return new DefaultTestActionBuilder(action);
    }

    @Override
    default <T extends TestActionContainer, B extends TestActionContainerBuilder<T, B>> TestActionContainerBuilder<T, B> container(T container) {
        return new AbstractTestContainerBuilder<>() {
            @Override
            public T doBuild() {
                container.setActions(actions.stream()
                        .map(TestActionBuilder::build)
                        .collect(Collectors.toList()));

                return container;
            }

            @Override
            public T build() {
                if (!container.getActions().isEmpty()) {
                    return container;
                }

                return super.build();
            }
        };
    }

    @Override
    default AntRunAction.Builder antrun() {
        return new AntRunAction.Builder();
    }

    @Override
    default ApplyTestBehaviorAction.Builder apply() {
        return new ApplyTestBehaviorAction.Builder();
    }

    @Override
    default CreateVariablesAction.Builder createVariables() {
        return new CreateVariablesAction.Builder();
    }

    @Override
    default CreateEndpointAction.Builder createEndpoint() {
        return new CreateEndpointAction.Builder();
    }

    @Override
    default EchoAction.Builder echo() {
        return new EchoAction.Builder();
    }

    @Override
    default ReceiveTimeoutAction.Builder expectTimeout() {
        return new ReceiveTimeoutAction.Builder();
    }

    @Override
    default FailAction.Builder fail() {
        return new FailAction.Builder();
    }

    @Override
    default InputAction.Builder input() {
        return new InputAction.Builder();
    }

    @Override
    default LoadPropertiesAction.Builder load() {
        return new LoadPropertiesAction.Builder();
    }

    @Override
    default PurgeEndpointAction.Builder purge() {
        return new PurgeEndpointAction.Builder();
    }

    @Override
    default ReceiveActionBuilder<? extends ReceiveMessageAction, ? extends ReceiveMessageBuilderFactory<?, ?>, ? extends ReceiveActionBuilder<?, ?, ?>> receive() {
        return new ReceiveMessageAction.Builder();
    }

    @Override
    default SleepAction.Builder sleep() {
        return new SleepAction.Builder();
    }

    @Override
    default SendActionBuilder<? extends SendMessageAction, ? extends SendMessageBuilderFactory<?, ?>, ? extends SendActionBuilder<?, ?, ?>> send() {
        return new SendMessageAction.Builder();
    }

    @Override
    default StartServerAction.Builder startServer() {
        return new StartServerAction.Builder();
    }

    @Override
    default StopServerAction.Builder stopServer() {
        return new StopServerAction.Builder();
    }

    @Override
    default StopTimeAction.Builder stopTime() {
        return new StopTimeAction.Builder();
    }

    @Override
    default StopTimerAction.Builder stopTimer() {
        return new StopTimerAction.Builder();
    }

    @Override
    default TraceVariablesAction.Builder trace() {
        return new TraceVariablesAction.Builder();
    }

    @Override
    default TransformAction.Builder transform() {
        return new TransformAction.Builder();
    }

    /**
     * Test action containers
     */

    @Override
    default Template.Builder applyTemplate() {
        return new Template.Builder();
    }

    @Override
    default Assert.Builder assertException() {
        return new Assert.Builder();
    }

    @Override
    default Async.Builder async() {
        return new Async.Builder();
    }

    @Override
    default Catch.Builder catchException() {
        return new Catch.Builder();
    }

    @Override
    default Conditional.Builder conditional() {
        return new Conditional.Builder();
    }

    @Override
    default FinallySequence.Builder doFinally() {
        return new FinallySequence.Builder();
    }

    @Override
    default Iterate.Builder iterate() {
        return new Iterate.Builder();
    }

    @Override
    default Parallel.Builder parallel() {
        return new Parallel.Builder();
    }

    @Override
    default RepeatOnErrorUntilTrue.Builder repeatOnError() {
        return new RepeatOnErrorUntilTrue.Builder();
    }

    @Override
    default RepeatUntilTrue.Builder repeat() {
        return new RepeatUntilTrue.Builder();
    }

    @Override
    default Sequence.Builder sequential() {
        return new Sequence.Builder();
    }

    @Override
    default Timer.Builder timer() {
        return new Timer.Builder();
    }

    @Override
    default WaitContainerBuilder<Wait, Wait.Builder<Condition>, Condition> waitFor() {
        return new Wait.Builder<>();
    }
}
