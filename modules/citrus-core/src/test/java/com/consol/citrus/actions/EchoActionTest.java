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

import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;

/**
 * @author Christoph Deppisch
 */
public class EchoActionTest extends AbstractTestNGUnitTest {
	
	@Test
	public void testEchoMessage() {
		EchoAction echo = new EchoAction();
		echo.setMessage("Hello Citrus!");
		
		echo.execute(context);
	}
	
	@Test
	public void testEchoMessageWithVariables() {
		EchoAction echo = new EchoAction();
		context.setVariable("greeting", "Hello");
		
		echo.setMessage("${greeting} Citrus!");
		
		echo.execute(context);
	}
	
	@Test
	public void testEchoMessageWithFunctions() {
		EchoAction echo = new EchoAction();
		echo.setMessage("Today is citrus:currentDate()");
		
		echo.execute(context);
	}
	
	@Test(expectedExceptions = {CitrusRuntimeException.class})
	public void testEchoMessageWithUnknownVariables() {
		EchoAction echo = new EchoAction();
		echo.setMessage("${greeting} Citrus!");
		
		echo.execute(context);
	}
}
