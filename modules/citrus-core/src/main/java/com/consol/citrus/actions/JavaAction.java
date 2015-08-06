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

package com.consol.citrus.actions;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Action to enable class invocation through java reflection
 * 
 * @author Christoph Deppisch
 * @since 2006
 */
public class JavaAction extends AbstractTestAction {
    /** Instance to be invoked, injected through java reflection */
    private Object instance;

    /** Name of class */
    private String className;

    /** Name of method to invoke */
    private String methodName;

    /** Method args */
    private List<Object> methodArgs = new ArrayList<Object>();

    /** Constructor args */
    private List<Object> constructorArgs = new ArrayList<Object>();

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JavaAction.class);

    /**
     * Default constructor.
     */
    public JavaAction() {
        setName("java");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void doExecute(TestContext context) {
        try {
            if (instance == null) {
                instance = getObjectInstanceFromClass(context);
            }

            Class<?>[] methodTypes = new Class<?>[methodArgs.size()];
            Object[] methodObjects = new Object[methodArgs.size()];
            for (int i = 0; i < methodArgs.size(); i++) {
                methodTypes[i] = methodArgs.get(i).getClass();
                
                if (methodArgs.get(i).getClass().equals(List.class)) {
                    String[] converted = StringUtils.toStringArray((List<String>)methodArgs.get(i));
                    
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

            invokeMethod(methodTypes, methodObjects);
        } catch (RuntimeException e) {
            throw new CitrusRuntimeException("Failed to invoke Java method due to runtime error", e);
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to invoke Java method", e);
        }
    }

    private void invokeMethod(Class<?>[] methodTypes, Object[] methodObjects) throws IllegalArgumentException, InvocationTargetException, IllegalAccessException, CitrusRuntimeException {
        Method methodToRun = ReflectionUtils.findMethod(instance.getClass(), methodName, methodTypes);

        if (methodToRun == null) {
            throw new CitrusRuntimeException("Unable to find method '" + methodName + "(" + 
                    StringUtils.arrayToCommaDelimitedString(methodTypes) + ")' for class '" + instance.getClass() + "'");
        }
        
        log.info("Invoking method '" + methodToRun.toString() + "' on instance '" + instance.getClass() + "'");

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
        
        log.info("Instantiating class for name '" + className + "'");
        
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
     * Setter for class name
     * @param className
     */
    public JavaAction setClassName(String className) {
        this.className = className;
        return this;
    }

    /**
     * Setter for constructor args
     * @param constructorArgs
     */
    public JavaAction setConstructorArgs(List<Object> constructorArgs) {
        this.constructorArgs = constructorArgs;
        return this;
    }

    /**
     * Setter for method args
     * @param methodArgs
     */
    public JavaAction setMethodArgs(List<Object> methodArgs) {
        this.methodArgs = methodArgs;
        return this;
    }

    /**
     * Setter for method name
     * @param methodName
     */
    public JavaAction setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    /**
     * Setter for object instance
     * @param instance
     */
    public JavaAction setInstance(Object instance) {
        this.instance = instance;
        return this;
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
}
