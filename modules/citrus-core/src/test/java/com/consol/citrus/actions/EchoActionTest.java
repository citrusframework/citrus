/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.actions;

import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
 */
public class EchoActionTest extends AbstractBaseTest {
	
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
