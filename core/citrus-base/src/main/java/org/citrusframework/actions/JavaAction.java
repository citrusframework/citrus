/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.actions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.ReflectionHelper;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to enable class invocation through java reflection
 *
 * @author Christoph Deppisch
 * @since 2006
 */
public class JavaAction extends AbstractTestAction {
    /** Instance to be invoked, injected through java reflection */
    private final Object instance;

    /** Name of class */
    private final String className;

    /** Name of method to invoke */
    private final String methodName;

    /** Method args */
    private final List<Object> methodArgs;

    /** Constructor args */
    private final List<Object> constructorArgs;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(JavaAction.class);

    /**
     * Default constructor.
     */
    public JavaAction(Builder builder) {
        super("java", builder);

        this.className = builder.className;
        this.instance = builder.instance;
        this.methodName = builder.methodName;
        this.methodArgs = builder.methodArgs;
        this.constructorArgs = builder.constructorArgs;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void doExecute(TestContext context) {
        try {
            final Object instanceToUse;
            if (instance != null) {
                instanceToUse = instance;
            } else {
                instanceToUse = getObjectInstanceFromClass(context);
            }

            Class<?>[] methodTypes = new Class<?>[methodArgs.size()];
            Object[] methodObjects = new Object[methodArgs.size()];
            for (int i = 0; i < methodArgs.size(); i++) {
                methodTypes[i] = methodArgs.get(i).getClass();

                if (methodArgs.get(i).getClass().equals(List.class)) {
                    String[] converted = ((List<String>)methodArgs.get(i)).toArray(new String[]{});

                    for (int j = 0; j < converted.length; j++) {
                        converted[j] = context.replaceDynamicContentInString(converted[j]);
                    }

                    methodObjects[i] = converted;
                } else if (methodArgs.get(i).getClass().equals(String[].class)) {
                    String[] params = (String[])methodArgs.get(i);
                    String[] converted = Arrays.copyOf(params, params.length);

                    for (int j = 0; j < converted.length; j++) {
                        converted[j] = context.replaceDynamicContentInString(converted[j]);
                    }

                    methodObjects[i] = converted;
                } else if (methodArgs.get(i).getClass().equals(String.class)) {
                    methodObjects[i] = context.replaceDynamicContentInString(methodArgs.get(i).toString());
                } else {
                    methodObjects[i] = methodArgs.get(i);
                }
            }

            invokeMethod(instanceToUse, methodTypes, methodObjects);
        } catch (RuntimeException e) {
            throw new CitrusRuntimeException("Failed to invoke Java method due to runtime error", e);
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to invoke Java method", e);
        }
    }

    private void invokeMethod(Object instance, Class<?>[] methodTypes, Object[] methodObjects) throws IllegalArgumentException, InvocationTargetException, IllegalAccessException, CitrusRuntimeException {
        Method methodToRun = ReflectionHelper.findMethod(instance.getClass(), methodName, methodTypes);

        if (methodToRun == null) {
            throw new CitrusRuntimeException("Unable to find method '" + methodName + "(" +
                    Arrays.stream(methodTypes).map(Class::getSimpleName).collect(Collectors.joining(",")) + ")' for class '" + instance.getClass() + "'");
        }

        logger.info("Invoking method '" + methodToRun.toString() + "' on instance '" + instance.getClass() + "'");

        methodToRun.invoke(instance, methodObjects);
    }

    /**
     * Instantiate class for name. Constructor arguments are supported if
     * specified.
     *
     * @param context the current test context.
     * @return
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IllegalArgumentException
     */
    private Object getObjectInstanceFromClass(TestContext context) throws ClassNotFoundException, SecurityException, NoSuchMethodException,
        IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {

        if (!StringUtils.hasText(className)) {
            throw new CitrusRuntimeException("Neither class name nor object instance reference " +
                "is set for Java reflection call");
        }

        logger.info("Instantiating class for name '" + className + "'");

        Class<?> classToRun = Class.forName(className);

        Class<?>[] constructorTypes = new Class<?>[constructorArgs.size()];
        Object[] constructorObjects = new Object[constructorArgs.size()];
        for (int i = 0; i < constructorArgs.size(); i++) {
            constructorTypes[i] = constructorArgs.get(i).getClass();

            if (constructorArgs.get(i).getClass().equals(String.class)) {
                constructorObjects[i] = context.replaceDynamicContentInString(constructorArgs.get(i).toString());
            } else {
                constructorObjects[i] = constructorArgs.get(i);
            }
        }

        Constructor<?> constr = classToRun.getConstructor(constructorTypes);
        return constr.newInstance(constructorObjects);
    }

    /**
     * Gets the instance.
     * @return the instance
     */
    public Object getInstance() {
        return instance;
    }

    /**
     * Gets the className.
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * Gets the methodName.
     * @return the methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Gets the methodArgs.
     * @return the methodArgs
     */
    public List<Object> getMethodArgs() {
        return methodArgs;
    }

    /**
     * Gets the constructorArgs.
     * @return the constructorArgs
     */
    public List<Object> getConstructorArgs() {
        return constructorArgs;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<JavaAction, Builder> {

        private Object instance;
        private String className;
        private String methodName;
        private final List<Object> methodArgs = new ArrayList<>();
        private final List<Object> constructorArgs = new ArrayList<>();

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder java(String className) {
            Builder builder = new Builder();
            builder.className(className);
            return builder;
        }

        public static Builder java(Class<?> clazz) {
            Builder builder = new Builder();
            builder.className(clazz.getSimpleName());
            return builder;
        }

        public static Builder java(Object instance) {
            Builder builder = new Builder();
            builder.instance(instance);
            return builder;
        }

        public Builder instance(Object instance) {
            this.instance = instance;
            return this;
        }

        public Builder className(String className) {
            this.className = className;
            return this;
        }

        /**
         * Method to call via reflection.
         * @param methodName
         */
        public Builder method(String methodName) {
            this.methodName = methodName;
            return this;
        }

        /**
         * Constructor arguments.
         * @param constructorArgs
         */
        public Builder constructorArgs(Object... constructorArgs) {
            return constructorArgs(Arrays.asList(constructorArgs));
        }

        /**
         * Constructor arguments.
         * @param constructorArgs
         */
        public Builder constructorArgs(List<Object> constructorArgs) {
            this.constructorArgs.addAll(constructorArgs);
            return this;
        }

        /**
         * Setter for method arguments
         * @param methodArgs
         */
        public Builder methodArgs(Object... methodArgs) {
            return methodArgs(Arrays.asList(methodArgs));
        }

        /**
         * Setter for method arguments
         * @param methodArgs
         */
        public Builder methodArgs(List<Object> methodArgs) {
            this.methodArgs.addAll(methodArgs);
            return this;
        }

        @Override
        public JavaAction build() {
            return new JavaAction(this);
        }
    }
}
