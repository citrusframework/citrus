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
package org.citrusframework.cucumber.backend;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.CucumberInvocationTargetException;
import io.cucumber.core.backend.StaticHookDefinition;

/**
 * Static hook definition invoking a static no-argument method once per Cucumber run.
 */
public class CitrusStaticHookDefinition implements StaticHookDefinition {

    private final Method method;
    private final int order;
    private final HookType hookType;

    public CitrusStaticHookDefinition(Method method, int order, HookType hookType) {
        this.method = method;
        this.order = order;
        this.hookType = hookType;
    }

    @Override
    public void execute() {
        try {
            method.invoke(null);
        } catch (InvocationTargetException e) {
            throw new CucumberInvocationTargetException(this, e);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new CucumberBackendException("Failed to invoke " + method, e);
        }
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public Optional<String> getName() {
        return Optional.of(method.getDeclaringClass().getSimpleName() + "." + method.getName());
    }

    @Override
    public Optional<HookType> getHookType() {
        return Optional.of(hookType);
    }

    @Override
    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return stackTraceElement.getClassName().equals(method.getDeclaringClass().getName())
                && stackTraceElement.getMethodName().equals(method.getName());
    }

    @Override
    public String getLocation() {
        return method.getDeclaringClass().getName() + "#" + method.getName();
    }
}
