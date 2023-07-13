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

import java.util.Optional;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.lang.GroovyObjectSupport;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.container.Template;

/**
 * @author Christoph Deppisch
 */
public class TemplateConfiguration implements ActionsBuilder {

    private final Template.Builder target;

    public TemplateConfiguration(Template.Builder builder) {
        this.target = builder;
    }

    public void actions(@DelegatesTo(TemplateConfiguration.class) Closure<?> callable) {
        callable.setResolveStrategy(Closure.DELEGATE_FIRST);
        callable.setDelegate(this);
        callable.call();
    }

    public void name(String templateName) {
        target.templateName(templateName);
    }

    public void description(String description) {
        target.description(description);
    }

    public void globalContext(boolean global) {
        target.globalContext(global);
    }

    public void $actions(@DelegatesTo(TemplateConfiguration.class) Closure<?> callable) {
        this.actions(callable);
    }

    public void parameters(@DelegatesTo(ParameterConfiguration.class) Closure<?> callable) {
        callable.setResolveStrategy(Closure.DELEGATE_FIRST);
        callable.setDelegate(new ParameterConfiguration(target));
        callable.call();
    }

    @Override
    public <T extends TestAction> T $(TestActionBuilder<T> builder) {
        T action = builder.build();
        target.actions(action);
        return action;
    }

    private static class ParameterConfiguration extends GroovyObjectSupport {

        private final Template.Builder target;
        public ParameterConfiguration(Template.Builder builder) {
            this.target = builder;
        }

        public void propertyMissing(String name, Object value) {
            target.parameter(name, Optional.ofNullable(value).map(Object::toString).orElse("").trim());
        }
    }
}
