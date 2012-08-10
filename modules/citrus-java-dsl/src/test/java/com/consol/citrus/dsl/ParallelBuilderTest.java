package com.consol.citrus.dsl;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.container.Parallel;

public class ParallelBuilderTest {
	@Test
	public void testParallelBuilder() {
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
                parallel(echo("${var}"), sleep(2.0), echo("ASDF"));
            }
        };
        
        builder.configure();
        
        assertEquals(builder.getTestCase().getActions().size(), 1);
        assertEquals(builder.getTestCase().getActions().get(0).getClass(), Parallel.class);
        assertEquals(builder.getTestCase().getActions().get(0).getName(), Parallel.class.getSimpleName());
        
        Parallel container = (Parallel)builder.getTestCase().getActions().get(0); 
        assertEquals(container.getActions().size(), 3);
        assertEquals(container.getTestAction(0).getClass(), EchoAction.class);
	}
}
