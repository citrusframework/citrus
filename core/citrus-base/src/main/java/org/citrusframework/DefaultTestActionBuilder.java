/*
 * Copyright 2020 the original author or authors.
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

package org.citrusframework;

import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.context.TestContext;

/**
 * @author Christoph Deppisch
 */
public class DefaultTestActionBuilder extends AbstractTestActionBuilder<AbstractTestAction, AbstractTestActionBuilder<AbstractTestAction, ?>> {

    private final TestAction delegate;

    /**
     * Private constructor.
     * @param delegate
     */
    private DefaultTestActionBuilder(TestAction delegate) {
        this.delegate = delegate;
    }

    /**
     * Static fluent entry method for Java DSL
     * @param action
     * @return
     */
    public static DefaultTestActionBuilder action(TestAction action) {
        return new DefaultTestActionBuilder(action);
    }

    @Override
    public AbstractTestAction build() {
        AbstractTestAction testAction = new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                delegate.execute(context);
            }
        };

        testAction.setName(this.getName());
        testAction.setDescription(this.getDescription());
        testAction.setActor(this.getActor());

        return testAction;
    }
}
