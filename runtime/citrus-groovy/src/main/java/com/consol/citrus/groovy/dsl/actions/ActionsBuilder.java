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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.actions.SleepAction;
import com.consol.citrus.container.Wait;
import com.consol.citrus.groovy.dsl.test.TestCaseScript;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MissingMethodException;
import org.springframework.util.ReflectionUtils;

import static com.consol.citrus.actions.EchoAction.Builder.echo;

/**
 * @author Christoph Deppisch
 */
public interface ActionsBuilder {

    /**
     * Short hand method running given test action builder.
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
     * Workaround method selection errors because Object classes do also use sleep method
     * signatures and Groovy may not know which one of them to invoke.
     * @return
     */
    default GroovyTestActionWrapper<SleepAction> delay() {
        return new GroovyTestActionWrapper<>(this, new SleepAction.Builder());
    }

    /**
     * Workaround method selection errors because Object classes do also use wait method
     * signatures and Groovy may not know which one of them to invoke.
     * @return
     */
    default GroovyTestActionWrapper<Wait> waitFor() {
        return new GroovyTestActionWrapper<>(this, new Wait.Builder());
    }

    /**
     * Special send message action wrapper adding Groovy closure support when specifying the message to send.
     * @return
     */
    default GroovyTestActionWrapper<SendMessageAction> send() {
        return new GroovyTestActionWrapper<>(this, new SendActionBuilderWrapper());
    }

    /**
     * Special receive message action wrapper adding Groovy closure support when specifying the message to receive.
     * @return
     */
    default GroovyTestActionWrapper<ReceiveMessageAction> receive() {
        return new GroovyTestActionWrapper<>(this, new ReceiveActionBuilderWrapper());
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

        return new GroovyTestActionWrapper<>(this, actionBuilder);
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
        TestActionBuilder<?> builder = TestActionBuilder.lookup(id).orElseThrow(() -> new MissingMethodException(id, TestCaseScript.class, args));

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
            Method initializer = ReflectionUtils.findMethod(builder.getClass(), id, paramTypes);
            if (initializer == null) {
                throw new GroovyRuntimeException(String.format("Failed to find initializing method %s(%s) for " +
                        "action builder type %s", Arrays.toString(paramTypes), id, builder.getClass().getName()));
            }
            return (TestActionBuilder<?>) initializer.invoke(null, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new GroovyRuntimeException("Failed to get action builder", e);
        }
    }

    class GroovyTestActionWrapper<T extends TestAction> extends GroovyObjectSupport implements TestActionBuilder<T> {
        private final ActionsBuilder builder;
        private TestActionBuilder<T> delegate;

        public GroovyTestActionWrapper(ActionsBuilder builder, TestActionBuilder<T> delegate) {
            this.builder = builder;
            this.delegate = delegate;
        }

        /**
         * Helper method overloading the -- operator to finish the action building. Allows to use the operator as bulletin point style
         * list of actions.
         * @return
         */
        public Object negative() {
            return builder.$(delegate);
        }

        public Object methodMissing(String name, Object argLine) {
            Object[] args = Optional.ofNullable(argLine).map(Object[].class::cast).orElse(new Object[]{});

            try {
                Method m;
                if (args.length > 0) {
                    Class<?>[] paramTypes = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
                    m = ReflectionUtils.findMethod(delegate.getClass(), name, paramTypes);

                    if (m == null) {
                        List<Method> methods = Arrays.stream(ReflectionUtils.getAllDeclaredMethods(delegate.getClass()))
                                .filter(candidate -> candidate.getName().equals(name))
                                .collect(Collectors.toList());

                        for (Method method : methods) {
                            if (method.getParameterTypes().length != paramTypes.length) {
                                continue;
                            }

                            boolean fullParamMatch = true;
                            for (int i = 0; i < method.getParameterTypes().length && fullParamMatch; i++) {
                                fullParamMatch = method.getParameterTypes()[i].isAssignableFrom(paramTypes[i]) ||
                                        (method.getParameterTypes()[i].isPrimitive() && method.getParameterTypes()[i].getSimpleName().equalsIgnoreCase(paramTypes[i].getSimpleName()));
                            }

                            if (fullParamMatch) {
                                m = method;
                                break;
                            }
                        }
                    }
                } else {
                    m = ReflectionUtils.findMethod(delegate.getClass(), name);
                }

                if (m != null) {
                    Object result = m.invoke(delegate, args);

                    if (result instanceof TestActionBuilder) {
                        delegate = (TestActionBuilder<T>) result;
                    }

                    return this;
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new MissingMethodException(name, delegate.getClass(), args);
            }

            throw new MissingMethodException(name, delegate.getClass(), args);
        }

        @Override
        public T build() {
            return delegate.build();
        }
    }
}
