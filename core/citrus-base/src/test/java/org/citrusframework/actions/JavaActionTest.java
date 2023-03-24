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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class JavaActionTest extends UnitTestSupport {

	@Test
	public void testJavaCallNoMethodParameter() {
		JavaAction action = new JavaAction.Builder()
				.className("org.citrusframework.util.InvocationDummy")
				.method("invoke")
				.build();
		action.execute(context);
	}

	@Test
	public void testJavaCallSingleMethodParameter() {
		List<Object> args = new ArrayList<Object>();
		args.add("Test");

		JavaAction action = new JavaAction.Builder()
				.className("org.citrusframework.util.InvocationDummy")
				.method("invoke")
				.methodArgs(args)
				.build();
		action.execute(context);
	}

	@Test
	public void testJavaCallMethodParameters() {
		List<Object> args = new ArrayList<Object>();
		args.add(4);
		args.add("Test");
		args.add(true);

		JavaAction action = new JavaAction.Builder()
				.className("org.citrusframework.util.InvocationDummy")
				.method("invoke")
				.methodArgs(args)
				.build();
		action.execute(context);
	}

	@Test
	public void testJavaCallConstructorNoArgs() {
		JavaAction action = new JavaAction.Builder()
				.className("org.citrusframework.util.InvocationDummy")
				.method("invoke")
				.constructorArgs(Collections.emptyList())
				.build();
		action.execute(context);
	}

	@Test
	public void testJavaCallSingleConstructorArg() {
		List<Object> args = new ArrayList<Object>();
        args.add("Test");

		JavaAction action = new JavaAction.Builder()
				.className("org.citrusframework.util.InvocationDummy")
				.method("invoke")
				.constructorArgs(args)
				.build();
		action.execute(context);
	}

	@Test
	public void testJavaCallConstructorArgs() {
		List<Object> args = new ArrayList<Object>();
		args.add(4);
		args.add("Test");
		args.add(true);

		JavaAction action = new JavaAction.Builder()
				.className("org.citrusframework.util.InvocationDummy")
				.method("invoke")
				.constructorArgs(args)
				.build();
		action.execute(context);
	}

	@Test
    public void testJavaCallConstructorArgsVariableSupport() {
        context.setVariable("text", "Test");

        List<Object> args = new ArrayList<Object>();
        args.add(4);
        args.add("${text}");
        args.add(true);

		JavaAction action = new JavaAction.Builder()
				.className("org.citrusframework.util.InvocationDummy")
				.method("invoke")
				.constructorArgs(args)
				.build();
        action.execute(context);
    }

	@Test
	public void testJavaCall() {
		List<Object> args = new ArrayList<Object>();
		args.add(4);
		args.add("Test");
		args.add(true);

		JavaAction action = new JavaAction.Builder()
				.className("org.citrusframework.util.InvocationDummy")
				.method("invoke")
				.constructorArgs(args)
				.methodArgs(args)
				.build();
		action.execute(context);
	}

	@Test
    public void testJavaCallVariableSupport() {
        context.setVariable("text", "Test");

        List<Object> args = new ArrayList<Object>();
        args.add(4);
        args.add("${text}");
        args.add(true);

		JavaAction action = new JavaAction.Builder()
				.className("org.citrusframework.util.InvocationDummy")
				.method("invoke")
				.constructorArgs(args)
				.methodArgs(args)
				.build();
        action.execute(context);
    }

	@Test(expectedExceptions = {CitrusRuntimeException.class})
	public void testJavaCallWrongConstructorArgs() {
		List<Object> args = new ArrayList<Object>();
		args.add("Wrong");
		args.add(4);
		args.add(true);

		JavaAction action = new JavaAction.Builder()
				.className("org.citrusframework.util.InvocationDummy")
				.method("invoke")
				.constructorArgs(args)
				.build();
		action.execute(context);
	}

	@Test(expectedExceptions = {CitrusRuntimeException.class})
	public void testJavaCallWrongMethodParameters() {
		List<Object> args = new ArrayList<Object>();
		args.add("Wrong");
		args.add(4);
		args.add(true);

		JavaAction action = new JavaAction.Builder()
				.className("org.citrusframework.util.InvocationDummy")
				.method("invoke")
				.methodArgs(args)
				.build();
		action.execute(context);
	}

	@Test(expectedExceptions = {CitrusRuntimeException.class})
	public void testJavaCallClassNotFound() {
		JavaAction action = new JavaAction.Builder()
				.className("org.citrusframework.util.DoesNotExist")
				.method("invoke")
				.build();
		action.execute(context);
	}

	@Test(expectedExceptions = {CitrusRuntimeException.class})
	public void testJavaCallNoSuchMethod() {
		JavaAction action = new JavaAction.Builder()
				.className("org.citrusframework.util.InvocationDummy")
				.method("doesNotExist")
				.build();
		action.execute(context);
	}
}
