/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.actions.JavaAction;

import java.util.Arrays;

/**
 * Action to enable class invocation through java reflection
 * 
 * @author Christoph Deppisch
 * @since 2.3
 */
public class JavaActionBuilder extends AbstractTestActionBuilder<JavaAction> {

	public JavaActionBuilder(JavaAction action) {
	    super(action);
    }
	
	/**
     * Method to call via reflection.
     * @param methodName
     */
	public JavaActionBuilder method(String methodName) {
		action.setMethodName(methodName);
		return this;
	}
	
	/**
     * Constructor arguments.
     * @param constructorArgs
     */
	public JavaActionBuilder constructorArgs(Object... constructorArgs) {
		action.setConstructorArgs(Arrays.asList(constructorArgs));
		return this;
	}
	
	/**
     * Setter for method arguments
     * @param methodArgs
     */
	public JavaActionBuilder methodArgs(Object... methodArgs) {
		action.setMethodArgs(Arrays.asList(methodArgs));
		return this;
	}
}
