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

package org.citrusframework.groovy.dsl.actions;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActionRunner;
import org.citrusframework.container.FinallySequence;
import org.citrusframework.context.TestContext;
import org.citrusframework.groovy.dsl.test.VariablesConfiguration;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

/**
 * @author Christoph Deppisch
 */
public class ActionsConfiguration implements ActionsBuilder {

    private final TestActionRunner runner;
    private final TestContext context;

    public ActionsConfiguration(TestActionRunner runner, TestContext context) {
        this.runner = runner;
        this.context = context;
    }

    public void actions(@DelegatesTo(ActionsConfiguration.class) Closure<?> callable) {
        callable.setResolveStrategy(Closure.DELEGATE_FIRST);
        callable.setDelegate(this);
        callable.call();
    }

    public void $actions(@DelegatesTo(ActionsConfiguration.class) Closure<?> callable) {
        this.actions(callable);
    }
    public void $finally(@DelegatesTo(FinallyActionsBuilder.class) Closure<?> callable) {
        this.doFinally(callable);
    }

    public void variables(@DelegatesTo(VariablesConfiguration.class) Closure<?> callable) {
        callable.setResolveStrategy(Closure.DELEGATE_FIRST);
        callable.setDelegate(new VariablesConfiguration(context));
        callable.call();
    }

    public FinallySequence.Builder doFinally(@DelegatesTo(FinallyActionsBuilder.class) Closure<?> callable) {
        FinallyActionsBuilder builder = new FinallyActionsBuilder();
        if (callable != null) {
            callable.setResolveStrategy(Closure.DELEGATE_FIRST);
            callable.setDelegate(builder);
            callable.call();

            runner.run(builder.get());
        }

        return builder.get();
    }

    @Override
    public <T extends TestAction> T $(TestActionBuilder<T> builder) {
        return runner.run(builder);
    }
}
