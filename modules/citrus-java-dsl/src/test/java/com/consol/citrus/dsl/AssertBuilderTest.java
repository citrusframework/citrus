package com.consol.citrus.dsl;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.container.Assert;
import com.consol.citrus.exceptions.CitrusRuntimeException;

public class AssertBuilderTest {
	@Test
	public void testAssertBuilder() {
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
                assertException(echo("${Var}"), "Unknown variable 'Var'", CitrusRuntimeException.class);
            }
        };
        
        builder.configure();
        
        assertEquals(builder.getTestCase().getActions().size(), 1);
        assertEquals(builder.getTestCase().getActions().get(0).getClass(), Assert.class);
        assertEquals(builder.getTestCase().getActions().get(0).getName(), Assert.class.getSimpleName());
        
        Assert container = (Assert)(builder.getTestCase().getTestAction(0));
        
        assertEquals(container.getActions().size(), 1);
        assertEquals(container.getAction().getClass(), EchoAction.class);
        assertEquals(container.getException(), CitrusRuntimeException.class);
        assertEquals(container.getMessage(), "Unknown variable 'Var'");
        assertEquals(((EchoAction)(container.getAction())).getMessage(), "${Var}");
	}
}
