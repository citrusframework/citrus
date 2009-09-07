package com.consol.citrus.actions;

import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.exceptions.CitrusRuntimeException;

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
