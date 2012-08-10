package com.consol.citrus.dsl;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.container.Catch;

public class CatchBuilderTest {
	@Test
	public void testCatchBuilder() {
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
                catchException("CitrusRuntimeException", echo("${var}"));
            }
        };
        
        builder.configure();
        
        assertEquals(builder.getTestCase().getActions().size(), 1);
        assertEquals(builder.getTestCase().getActions().get(0).getClass(), Catch.class);
        assertEquals(builder.getTestCase().getActions().get(0).getName(), Catch.class.getSimpleName());
        
        Catch container = (Catch)builder.getTestCase().getActions().get(0);
        assertEquals(container.getActions().size(), 1);
        assertEquals(container.getException(), "CitrusRuntimeException");
        assertEquals(((EchoAction)(container.getActions().get(0))).getMessage(), "${var}");
	}
}
