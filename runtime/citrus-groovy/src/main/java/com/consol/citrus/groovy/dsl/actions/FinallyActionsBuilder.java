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

package com.consol.citrus.groovy.dsl.actions;

import java.util.function.Supplier;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.actions.SleepAction;
import com.consol.citrus.container.FinallySequence;
import com.consol.citrus.container.Wait;
import groovy.lang.GroovyObjectSupport;

/**
 * @author Christoph Deppisch
 */
public class FinallyActionsBuilder extends GroovyObjectSupport implements ActionsBuilder, Supplier<FinallySequence.Builder> {

    private final FinallySequence.Builder builder = new FinallySequence.Builder();

    @Override
    public GroovyTestActionWrapper<Wait> waitFor() {
        GroovyTestActionWrapper<Wait> waitFor = ActionsBuilder.super.waitFor();
        builder.actions(waitFor);
        return waitFor;
    }

    @Override
    public GroovyTestActionWrapper<SleepAction> delay() {
        GroovyTestActionWrapper<SleepAction> delay = ActionsBuilder.super.delay();
        builder.actions(delay);
        return delay;
    }

    @Override
    public <T extends TestAction> T $(TestActionBuilder<T> nested) {
        builder.actions(nested);
        return nested.build();
    }

    @Override
    public Object methodMissing(String name, Object argLine) {
        TestActionBuilder<?> actionBuilder = (TestActionBuilder<?>) ActionsBuilder.super.methodMissing(name, argLine);
        builder.actions(actionBuilder);
        return actionBuilder;
    }

    @Override
    public FinallySequence.Builder get() {
        return builder;
    }
}
