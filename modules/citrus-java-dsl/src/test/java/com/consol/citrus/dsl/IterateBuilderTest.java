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
        
        assertEquals(((Iterate)(builder.getTestCase().getActions().get(0))).getIndexName(), "i");
        assertEquals(((Iterate)(builder.getTestCase().getActions().get(0))).getCondition(), "i lt 5");
        assertEquals(((Iterate)(builder.getTestCase().getActions().get(0))).getStep(), 1);
        assertEquals(((Iterate)(builder.getTestCase().getActions().get(0))).getIndex(), 0);
	}
}
