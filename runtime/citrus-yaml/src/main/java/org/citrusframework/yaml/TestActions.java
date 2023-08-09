/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.citrusframework.yaml.container.Iterate;
import org.citrusframework.yaml.container.Parallel;
import org.citrusframework.yaml.container.Repeat;
import org.citrusframework.yaml.container.RepeatOnError;
import org.citrusframework.yaml.container.Sequence;
import org.citrusframework.yaml.container.Timer;
import org.citrusframework.yaml.container.WaitFor;

/**
 * @author Christoph Deppisch
 */
public class TestActions implements Supplier<TestActionBuilder<?>> {

    private TestActionBuilder<?> builder;

    public void setAction(Action builder) {
        this.builder = builder;
    }

    public void setApplyTemplate(ApplyTemplate builder) {
        this.builder = builder;
    }

    public void setDelay(Delay builder) {
        this.builder = builder;
    }

    public void setSleep(Sleep builder) {
        this.builder = builder;
    }

    public void setAnt(AntRun builder) {
        this.builder = builder;
    }

    public void setEcho(Echo builder) {
        this.builder = builder;
    }

    public void setPrint(Print builder) {
        this.builder = builder;
    }

    public void setPurge(PurgeEndpoint builder) {
        this.builder = builder;
    }

    public void setStart(Start builder) {
        this.builder = builder;
    }

    public void setStop(Stop builder) {
        this.builder = builder;
    }

    public void setStopTime(StopTime builder) {
        this.builder = builder;
    }

    public void setStopTimer(StopTimer builder) {
        this.builder = builder;
    }

    public void setTrace(TraceVariables builder) {
        this.builder = builder;
    }

    public void setTransform(Transform builder) {
        this.builder = builder;
    }

    public void setLoad(LoadProperties builder) {
        this.builder = builder;
    }

    public void setCreateVariables(CreateVariables builder) {
        this.builder = builder;
    }

    public void setSend(Send builder) {
        this.builder = builder;
    }

    public void setReceive(Receive builder) {
        this.builder = builder;
    }

    public void setExpectTimeout(ExpectTimeout builder) {
        this.builder = builder;
    }

    public void setFail(Fail builder) {
        this.builder = builder;
    }

    public void setAssert(Assert builder) {
        this.builder = builder;
    }

    public void setWaitFor(WaitFor builder) {
        this.builder = builder;
    }

    public void setCatch(Catch builder) {
        this.builder = builder;
    }

    public void setConditional(Conditional builder) {
        this.builder = builder;
    }

    public void setSequential(Sequence builder) {
        this.builder = builder;
    }

    public void setIterate(Iterate builder) {
        this.builder = builder;
    }

    public void setParallel(Parallel builder) {
        this.builder = builder;
    }

    public void setRepeat(Repeat builder) {
        this.builder = builder;
    }

    public void setRepeatOnError(RepeatOnError builder) {
        this.builder = builder;
    }

    public void setTimer(Timer builder) {
        this.builder = builder;
    }

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
