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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import groovy.lang.Closure;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MissingMethodException;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.groovy.dsl.test.TestCaseScript;
import org.citrusframework.util.ReflectionHelper;

import static org.citrusframework.actions.EchoAction.Builder.echo;

/**
 * @author Christoph Deppisch
 */
public interface ActionsBuilder {

    /**
     * Shortcut method running given test action builder.
     * @param builder
     * @param <T>
     * @return
     */
    <T extends TestAction> T $(TestActionBuilder<T> builder);

    default void actions(Closure<?> callable) {
        callable.setResolveStrategy(Closure.DELEGATE_FIRST);
        callable.setDelegate(this);
        callable.call();
    }

    default void given(Closure<?> callable) {
        actions(callable);
    }

    default void when(Closure<?> callable) {
        actions(callable);
    }

    default void then(Closure<?> callable) {
        actions(callable);
    }

    default void given(String description, Closure<?> callable) {
        $(echo(description));
        actions(callable);
    }

    default void when(String description, Closure<?> callable) {
        $(echo(description));
        actions(callable);
    }

    default void then(String description, Closure<?> callable) {
        $(echo(description));
        actions(callable);
    }

    /**
     * Special send message action wrapper adding Groovy closure support when specifying the message to send.
     * @return
     */
    default SendActionBuilderWrapper send() {
        return new SendActionBuilderWrapper();
    }

    /**
     * Special receive message action wrapper adding Groovy closure support when specifying the message to receive.
     * @return
     */
    default ReceiveActionBuilderWrapper receive() {
        return new ReceiveActionBuilderWrapper();
    }

    default Object methodMissing(String name, Object argLine) {
        if (argLine == null) {
            throw new MissingMethodException(name, TestCaseScript.class, null);
        }

        Object[] args = (Object[]) argLine;
        TestActionBuilder<?> actionBuilder = findTestActionBuilder(name, args);
        if (actionBuilder == null) {
            throw new MissingMethodException(name, TestCaseScript.class, args);
        }

        return actionBuilder;
    }

    /**
     * Lookup test action build by its name via resource path.
     * When given arguments are empty instantiates the test action builder with default constructor.
     * Otherwise tries to find matching static instantiation method and calls this method with given arguments.
     * @param id name of the test action builder. Must match the action builder key in resource path lookup.
     * @param args optional method arguments.
     * @return test action builder instance.
     */
    private TestActionBuilder<?> findTestActionBuilder(String id, Object[] args) {
        TestActionBuilder<?> builder = TestActionBuilder.lookup(id).orElseThrow(() -> new MissingMethodException(id, this.getClass(), args));

        if (args == null || args.length == 0) {
            return builder;
        }

        return initializeActionBuilder(builder, id, args);
    }

    /**
     * Finds static method on given test action builder that matches given arguments.
     * Calls the static method to instantiate the test action builder.
     * @param builder the test action builder instance.
     * @param id name of the test action builder is identical to the static method name to be called.
     * @param args method arguments.
     * @return test action builder instance.
     */
    private TestActionBuilder<?> initializeActionBuilder(TestActionBuilder<?> builder, String id, Object... args) {
        try {
            Class<?>[] paramTypes = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
            Method initializer = ReflectionHelper.findMethod(builder.getClass(), id, paramTypes);
            if (initializer == null) {
                throw new GroovyRuntimeException(String.format("Failed to find initializing method %s(%s) for " +
                        "action builder type %s", Arrays.toString(paramTypes), id, builder.getClass().getName()));
            }
            return (TestActionBuilder<?>) initializer.invoke(null, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new GroovyRuntimeException("Failed to get action builder", e);
        }
    }
}
