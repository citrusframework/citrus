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

package org.citrusframework.junit;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.citrusframework.common.TestSourceAware;
import org.junit.runners.model.FrameworkMethod;

/**
 * Special framework method also holding test name and package coming from the Citrus test annotation. This way
 * execution can decide which test to invoke in case the annotation has more than one test name defined or a package scan is
 * used in the annotation.
 */
public class CitrusFrameworkMethod extends FrameworkMethod implements TestSourceAware {

    private final String testName;
    private final String packageName;

    private String sourceType;
    private String source;

    private final Map<String, Object> attributes = new HashMap<>();

    private RuntimeException error;

    public interface Runner {
        /**
         * Reads Citrus test annotation from framework method and executes test case.
         * @param frameworkMethod
         */
        void run(CitrusFrameworkMethod frameworkMethod);
    }

    /**
     * Returns a new {@code FrameworkMethod} for {@code method}
     *
     * @param method
     */
    public CitrusFrameworkMethod(Method method, String testName, String packageName) {
        this(method, "", testName, packageName);
    }

    /**
     * Returns a new {@code FrameworkMethod} for {@code method}
     *
     * @param method
     */
    public CitrusFrameworkMethod(Method method, String type, String testName, String packageName) {
        super(method);
        this.sourceType = type;
        this.testName = testName;
        this.packageName = packageName;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String type) {
        this.sourceType = type;
    }

    /**
     * Gets the test name.
     * @return
     */
    public String getTestName() {
        return testName;
    }

    /**
     * Gets the test package name.
     * @return
     */
    public String getPackageName() {
        return packageName;
    }

    @Override
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Gets the test source.
     * @return
     */
    public String getSource() {
        return source;
    }

    /**
     * Adds attribute value to framework method.
     * @param key
     * @param value
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * Gets attribute value from framework method.
     * @param key
     * @return
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Adds error to this framework method.
     * @param error
     * @return
     */
    public CitrusFrameworkMethod withError(RuntimeException error) {
        this.setError(error);
        return this;
    }

    /**
     * Checks if this framework method has an error.
     * @return
     */
    public boolean hasError() {
        return this.error != null;
    }

    /**
     * Sets an error on the framework method. Error will be thrown once the framework method is about to be executed.
     * @param error
     */
    public void setError(RuntimeException error) {
        this.error = error;
    }

    /**
     * Gets the optional error on this framework method.
     * The caller of this framework method should check the error and throw it accordingly before execution.
     * @return
     */
    public RuntimeException getError() {
        return error;
    }
}
