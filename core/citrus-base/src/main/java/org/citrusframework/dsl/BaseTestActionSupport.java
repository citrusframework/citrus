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

import org.citrusframework.TestContainers;
import org.citrusframework.actions.*;
import org.citrusframework.condition.Condition;
import org.citrusframework.container.*;

public interface BaseTestActionSupport extends BaseTestActions, TestContainers {

    @Override
    default AntRunActionBuilder<AntRunAction> antrun() {
        return new AntRunAction.Builder();
    }

    @Override
    default ApplyTestBehaviorActionBuilder<ApplyTestBehaviorAction> apply() {
        return new ApplyTestBehaviorAction.Builder();
    }

    @Override
    default CreateVariablesActionBuilder<CreateVariablesAction> createVariables() {
        return new CreateVariablesAction.Builder();
    }

    @Override
    default CreateEndpointActionBuilder<CreateEndpointAction> createEndpoint() {
        return new CreateEndpointAction.Builder();
    }

    @Override
    default EchoActionBuilder<EchoAction> echo() {
        return new EchoAction.Builder();
    }

    @Override
    default ReceiveTimeoutActionBuilder<ReceiveTimeoutAction> expectTimeout() {
        return new ReceiveTimeoutAction.Builder();
    }

    @Override
    default FailActionBuilder<FailAction> fail() {
        return new FailAction.Builder();
    }

    @Override
    default InputActionBuilder<InputAction> input() {
        return new InputAction.Builder();
    }

    @Override
    default LoadPropertiesActionBuilder<LoadPropertiesAction> load() {
        return new LoadPropertiesAction.Builder();
    }

    @Override
    default PurgeEndpointActionBuilder<PurgeEndpointAction> purge() {
        return new PurgeEndpointAction.Builder();
    }

    @Override
    default ReceiveActionBuilder<? extends ReceiveMessageAction, ? extends ReceiveMessageBuilderFactory<?, ?>> receive() {
        return new ReceiveMessageAction.Builder();
    }

    @Override
    default SleepActionBuilder<SleepAction> sleep() {
        return new SleepAction.Builder();
    }

    @Override
    default SendActionBuilder<? extends SendMessageAction, ? extends SendMessageBuilderFactory<?, ?>> send() {
        return new SendMessageAction.Builder();
    }

    @Override
    default StartServerActionBuilder<StartServerAction> startServer() {
        return new StartServerAction.Builder();
    }

    @Override
    default StopServerActionBuilder<StopServerAction> stopServer() {
        return new StopServerAction.Builder();
    }

    @Override
    default StopTimeActionBuilder<StopTimeAction> stopTime() {
        return new StopTimeAction.Builder();
    }

    @Override
    default StopTimerActionBuilder<StopTimerAction> stopTimer() {
        return new StopTimerAction.Builder();
    }

    @Override
    default TraceVariablesActionBuilder<TraceVariablesAction> trace() {
        return new TraceVariablesAction.Builder();
    }

    @Override
    default TransformActionBuilder<TransformAction> transform() {
        return new TransformAction.Builder();
    }

    /**
     * Test action containers
     */

    @Override
    default ApplyTemplateBuilder<Template, Template.Builder> applyTemplate() {
        return new Template.Builder();
    }

    @Override
    default AssertContainerBuilder<Assert, Assert.Builder> assertException() {
        return new Assert.Builder();
    }

    @Override
    default AsyncContainerBuilder<Async, Async.Builder> async() {
        return new Async.Builder();
    }

    @Override
    default CatchContainerBuilder<Catch, Catch.Builder> catchException() {
        return new Catch.Builder();
    }

    @Override
    default ConditionalContainerBuilder<Conditional, Conditional.Builder> conditional() {
        return new Conditional.Builder();
    }

    @Override
    default FinallyContainerBuilder<FinallySequence, FinallySequence.Builder> doFinally() {
        return new FinallySequence.Builder();
    }

    @Override
    default IterateContainerBuilder<Iterate, Iterate.Builder> iterate() {
        return new Iterate.Builder();
    }

    @Override
    default ParallelContainerBuilder<Parallel, Parallel.Builder> parallel() {
        return new Parallel.Builder();
    }

    @Override
    default RepeatOnErrorUntilTrueContainerBuilder<RepeatOnErrorUntilTrue, RepeatOnErrorUntilTrue.Builder> repeatOnError() {
        return new RepeatOnErrorUntilTrue.Builder();
    }

    @Override
    default RepeatUntilTrueContainerBuilder<RepeatUntilTrue, RepeatUntilTrue.Builder> repeat() {
        return new RepeatUntilTrue.Builder();
    }

    @Override
    default SequentialContainerBuilder<Sequence, Sequence.Builder> sequential() {
        return new Sequence.Builder();
    }

    @Override
    default TimerContainerBuilder<Timer, Timer.Builder> timer() {
        return new Timer.Builder();
    }

    @Override
    default WaitContainerBuilder<Wait, Wait.Builder<Condition>, Condition> waitFor() {
        return new Wait.Builder<>();
    }
}
