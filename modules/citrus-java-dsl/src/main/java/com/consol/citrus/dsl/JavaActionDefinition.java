package com.consol.citrus.dsl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.consol.citrus.actions.JavaAction;

public class JavaActionDefinition extends AbstractActionDefinition<JavaAction> {

	//FIXME: fix everything
	public JavaActionDefinition(JavaAction action) {
	    super(action);
    }
	
	public JavaActionDefinition methodName(String methodName) {
		action.setMethodName(methodName);
		return this;
	}
	
	public JavaActionDefinition constructorArgs(List<Object> constructorArgs) {
		action.setConstructorArgs(constructorArgs);
		try {
			action.setInstance(makeInstance(action, constructorArgs));
		}
		catch (Exception e) {}
		return this;
	}
	
	public JavaActionDefinition methodArgs(List<Object> methodArgs) {
		action.setMethodArgs(methodArgs);
		return this;
	}
	
	public JavaActionDefinition instance(Object instance) {
		action.setInstance(instance);
		return this;
	}
	
	private static Object makeInstance(JavaAction action, List<Object> constructorArgs) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Class<?> cls = Class.forName(action.getClassName());
		Class<?>[] paramTypes = new Class<?>[constructorArgs.size()];
		Object[] argList = new Object[constructorArgs.size()];
		for(int i = 0; i < constructorArgs.size(); i++) {
			paramTypes[i] = constructorArgs.get(i).getClass();
			argList[i] = constructorArgs.get(i);
		}
		Constructor<?> ct = cls.getConstructor(paramTypes);
		Object instance = ct.newInstance(argList);
		
		return instance;
	}
}
