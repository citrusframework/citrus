/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.actions;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractBaseTest;

import edu.emory.mathcs.backport.java.util.Collections;

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
