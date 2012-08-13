package com.consol.citrus.dsl;

import java.util.Arrays;

import com.consol.citrus.actions.JavaAction;

/**
 * Action to enable class invocation through java reflection
 */
public class JavaActionDefinition extends AbstractActionDefinition<JavaAction> {

	public JavaActionDefinition(JavaAction action) {
	    super(action);
    }
	
	/**
     * Setter for method name
     * @param methodName
     */
	public JavaActionDefinition methodName(String methodName) {
		action.setMethodName(methodName);
		return this;
	}
	
	/**
     * Setter for constructor args
     * @param constructorArgs
     */
	public JavaActionDefinition constructorArgs(Object... constructorArgs) {
		action.setConstructorArgs(Arrays.asList(constructorArgs));
		return this;
	}
	
	/**
     * Setter for method args
     * @param methodArgs
     */
	public JavaActionDefinition methodArgs(Object... methodArgs) {
		action.setMethodArgs(Arrays.asList(methodArgs));
		return this;
	}
}
