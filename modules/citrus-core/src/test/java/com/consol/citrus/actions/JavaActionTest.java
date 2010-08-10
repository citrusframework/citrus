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

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractBaseTest;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * @author Christoph Deppisch
 */
public class JavaActionTest extends AbstractBaseTest {

	@Test
	public void testJavaCallNoMethodParameter() {
		JavaAction action = new JavaAction();
		action.setClassName("com.consol.citrus.util.InvocationDummy");
		action.setMethodName("invoke");
		
		action.execute(context);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testJavaCallSingleMethodParameter() {
		JavaAction action = new JavaAction();
		action.setClassName("com.consol.citrus.util.InvocationDummy");
		action.setMethodName("invoke");
		
		action.setMethodArgs(Collections.singletonList("Test"));
		
		action.execute(context);
	}
	
	@Test
	public void testJavaCallMethodParameters() {
		JavaAction action = new JavaAction();
		action.setClassName("com.consol.citrus.util.InvocationDummy");
		action.setMethodName("invoke");
		
		List<Object> args = new ArrayList<Object>();
		args.add(4);
		args.add("Test");
		args.add(true);
		
		action.setMethodArgs(args);
		
		action.execute(context);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testJavaCallConstructorNoArgs() {
		JavaAction action = new JavaAction();
		action.setClassName("com.consol.citrus.util.InvocationDummy");
		action.setMethodName("invoke");
		
		action.setConstructorArgs(Collections.emptyList());
		
		action.execute(context);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testJavaCallSingleConstructorArg() {
		JavaAction action = new JavaAction();
		action.setClassName("com.consol.citrus.util.InvocationDummy");
		action.setMethodName("invoke");
		
		action.setConstructorArgs(Collections.singletonList("Test"));
		
		action.execute(context);
	}
	
	@Test
	public void testJavaCallConstructorArgs() {
		JavaAction action = new JavaAction();
		action.setClassName("com.consol.citrus.util.InvocationDummy");
		action.setMethodName("invoke");
		
		List<Object> args = new ArrayList<Object>();
		args.add(4);
		args.add("Test");
		args.add(true);
		
		action.setConstructorArgs(args);
		
		action.execute(context);
	}
	
	@Test
	public void testJavaCall() {
		JavaAction action = new JavaAction();
		action.setClassName("com.consol.citrus.util.InvocationDummy");
		action.setMethodName("invoke");
		
		List<Object> args = new ArrayList<Object>();
		args.add(4);
		args.add("Test");
		args.add(true);
		
		action.setConstructorArgs(args);
		action.setMethodArgs(args);
		
		action.execute(context);
	}
	
	@Test(expectedExceptions = {CitrusRuntimeException.class})
	public void testJavaCallWrongConstructorArgs() {
		JavaAction action = new JavaAction();
		action.setClassName("com.consol.citrus.util.InvocationDummy");
		action.setMethodName("invoke");
		
		List<Object> args = new ArrayList<Object>();
		args.add("Wrong");
		args.add(4);
		args.add(true);
		
		action.setConstructorArgs(args);
		
		action.execute(context);
	}
	
	@Test(expectedExceptions = {CitrusRuntimeException.class})
	public void testJavaCallWrongMethodParameters() {
		JavaAction action = new JavaAction();
		action.setClassName("com.consol.citrus.util.InvocationDummy");
		action.setMethodName("invoke");
		
		List<Object> args = new ArrayList<Object>();
		args.add("Wrong");
		args.add(4);
		args.add(true);
		
		action.setMethodArgs(args);
		
		action.execute(context);
	}
	
	@Test(expectedExceptions = {CitrusRuntimeException.class})
	public void testJavaCallClassNotFound() {
		JavaAction action = new JavaAction();
		action.setClassName("com.consol.citrus.util.DoesNotExist");
		action.setMethodName("invoke");
		
		action.execute(context);
	}
	
	@Test(expectedExceptions = {CitrusRuntimeException.class})
	public void testJavaCallNoSuchMethod() {
		JavaAction action = new JavaAction();
		action.setClassName("com.consol.citrus.util.InvocationDummy");
		action.setMethodName("doesNotExist");
		
		action.execute(context);
	}
}
