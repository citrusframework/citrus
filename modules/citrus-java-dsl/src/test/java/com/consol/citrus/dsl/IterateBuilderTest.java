package com.consol.citrus.dsl;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

import com.consol.citrus.container.Iterate;

public class IterateBuilderTest {
	@Test
    public void testIterateBuilder() {		
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
                iterate(createVariables().add("index", "${i}")).index("i").startsWith(0).step(1).condition("i lt 5");
            }
        };
        
        builder.configure();
        
        assertEquals(builder.getTestCase().getActions().size(), 1);
        assertEquals(builder.getTestCase().getActions().get(0).getClass(), Iterate.class);
        assertEquals(builder.getTestCase().getActions().get(0).getName(), Iterate.class.getSimpleName());
        
        Iterate container = (Iterate)builder.getTestCase().getActions().get(0);   
        assertEquals(container.getIndexName(), "i");
        assertEquals(container.getCondition(), "i lt 5");
        assertEquals(container.getStep(), 1);
        assertEquals(container.getIndex(), 0);
	}
}
