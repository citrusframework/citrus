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

package org.citrusframework.groovy.dsl.test;

import org.citrusframework.Citrus;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.container.FinallySequence;
import org.citrusframework.context.TestContext;
import org.citrusframework.groovy.dsl.actions.ActionsBuilder;
import org.citrusframework.groovy.dsl.actions.FinallyActionsBuilder;
import org.citrusframework.groovy.dsl.configuration.ContextConfiguration;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.lang.GroovyObjectSupport;

/**
 * @author Christoph Deppisch
 */
public class TestCaseScript extends GroovyObjectSupport implements ActionsBuilder {

    private final Citrus citrus;
    private final TestCaseRunner runner;
    private final TestContext context;

    private final String basePath;

    public TestCaseScript(Citrus citrus, TestCaseRunner runner, TestContext context, String basePath) {
        this.citrus = citrus;
        this.runner = runner;
        this.context = context;
        this.basePath = basePath;
    }

    public ContextConfiguration configuration() {
        return new ContextConfiguration(citrus, context, basePath);
    }

    public void name(String name) {
        runner.name(name);
    }

    public void author(String author) {
        runner.author(author);
    }

    public void description(String description) {
        runner.description(description);
    }

    public void status(String status) {
        runner.status(TestCaseMetaInfo.Status.valueOf(status));
    }

    public void configuration(@DelegatesTo(ContextConfiguration.class) Closure<?> callable) {
        callable.setResolveStrategy(Closure.DELEGATE_FIRST);
        callable.setDelegate(new ContextConfiguration(citrus, context, basePath));
        callable.call();
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
