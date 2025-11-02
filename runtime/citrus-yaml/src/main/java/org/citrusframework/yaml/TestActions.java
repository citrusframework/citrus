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

package org.citrusframework.yaml;

import java.util.function.Supplier;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.yaml.actions.*;
import org.citrusframework.yaml.container.Assert;
import org.citrusframework.yaml.container.Async;
import org.citrusframework.yaml.container.Catch;
import org.citrusframework.yaml.container.Conditional;
import org.citrusframework.yaml.container.DoFinally;
import org.citrusframework.yaml.container.Iterate;
import org.citrusframework.yaml.container.Parallel;
import org.citrusframework.yaml.container.Repeat;
import org.citrusframework.yaml.container.RepeatOnError;
import org.citrusframework.yaml.container.Sequential;
import org.citrusframework.yaml.container.Timer;
import org.citrusframework.yaml.container.WaitFor;

import static org.citrusframework.yaml.SchemaProperty.Kind.ACTION;
import static org.citrusframework.yaml.SchemaProperty.Kind.CONTAINER;

public class TestActions implements Supplier<TestActionBuilder<?>> {

    private TestActionBuilder<?> builder;

    @SchemaProperty(kind = ACTION, description = "Generic test action.")
    public void setAction(Action builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, description = "Apply template test action.")
    public void setApplyTemplate(ApplyTemplate builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, description = "Delay test action.")
    public void setDelay(Delay builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, description = "Sleep test action.")
    public void setSleep(Sleep builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, description = "Ant run test action.")
    public void setAnt(AntRun builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, description = "Echo test action.")
    public void setEcho(Echo builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, description = "Print test action.")
    public void setPrint(Print builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, description = "Purge test action.")
    public void setPurge(PurgeEndpoint builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, description = "Start server test action.")
    public void setStart(Start builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, description = "Stop server test action.")
    public void setStop(Stop builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, description = "Stop time test action.")
    public void setStopTime(StopTime builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, description = "Stop timer test action.")
    public void setStopTimer(StopTimer builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, description = "Trace variables test action.")
    public void setTrace(TraceVariables builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, description = "Transform test action.")
    public void setTransform(Transform builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, description = "Load properties test action.")
    public void setLoad(LoadProperties builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, description = "Create variables test action.")
    public void setCreateVariables(CreateVariables builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, description = "Create endpoints test action.")
    public void setCreateEndpoint(CreateEndpoint builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, description = "Send message test action.")
    public void setSend(Send builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, description = "Receive message test action.")
    public void setReceive(Receive builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, description = "Expect timeout test action.")
    public void setExpectTimeout(ExpectTimeout builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, description = "Fail test action.")
    public void setFail(Fail builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = ACTION, description = "Wait for test action.")
    public void setWaitFor(WaitFor builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = CONTAINER, description = "Assert exception test action.")
    public void setAssert(Assert builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = CONTAINER, description = "Catch exception test action.")
    public void setCatch(Catch builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = CONTAINER, description = "Runs test actions after the test.")
    public void setDoFinally(DoFinally builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = CONTAINER, description = "Conditional test action.")
    public void setConditional(Conditional builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = CONTAINER, description = "Sequential test action.")
    public void setSequential(Sequential builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = CONTAINER, description = "Iterate test action.")
    public void setIterate(Iterate builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = CONTAINER, description = "Parallel test action.")
    public void setParallel(Parallel builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = CONTAINER, description = "Repeat test action.")
    public void setRepeat(Repeat builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = CONTAINER, description = "Repeat on error test action.")
    public void setRepeatOnError(RepeatOnError builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = CONTAINER, description = "Timer test action.")
    public void setTimer(Timer builder) {
        this.builder = builder;
    }

    @SchemaProperty(kind = CONTAINER, description = "Async test action.")
    public void setAsync(Async builder) {
        this.builder = builder;
    }

    public void setAction(TestActionBuilder<?> builder) {
        this.builder = builder;
    }

    @Override
    public TestActionBuilder<?> get() {
        return builder;
    }
}
