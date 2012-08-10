package com.consol.citrus.dsl;

import java.util.Arrays;

import com.consol.citrus.actions.JavaAction;

public class JavaActionDefinition extends AbstractActionDefinition<JavaAction> {

	public JavaActionDefinition(JavaAction action) {
	    super(action);
    }
	
	public JavaActionDefinition methodName(String methodName) {
		action.setMethodName(methodName);
		return this;
	}
	
	public JavaActionDefinition constructorArgs(Object... constructorArgs) {
		action.setConstructorArgs(Arrays.asList(constructorArgs));
		return this;
	}
	
	public JavaActionDefinition methodArgs(Object... methodArgs) {
		action.setMethodArgs(Arrays.asList(methodArgs));
		return this;
	}
}
