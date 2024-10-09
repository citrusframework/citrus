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

import java.lang.reflect.Method;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.TestCaseState;
import org.citrusframework.util.ReflectionHelper;

public class CitrusHookDefinition implements HookDefinition {

    private final Method method;
    private final Lookup lookup;
    private final String tagExpression;
    private final int order;

    public CitrusHookDefinition(Method method, String tagExpression, int order, Lookup lookup) {
        this.method = method;
        this.tagExpression = tagExpression;
        this.order = order;
        this.lookup = lookup;
    }

    @Override
    public void execute(TestCaseState state) {
        Object[] args;
        if (method.getParameterTypes().length == 1) {
            args = new Object[]{new Scenario(state)};
        } else {
            args = new Object[0];
        }

        try {
            ReflectionHelper.invokeMethod(method, lookup.getInstance(method.getDeclaringClass()), args);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new CucumberBackendException("Failed to invoke " + method, e);
        }
    }

    @Override
    public String getTagExpression() {
        return tagExpression;
    }

    @Override
    public int getOrder() {
        return order;
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
