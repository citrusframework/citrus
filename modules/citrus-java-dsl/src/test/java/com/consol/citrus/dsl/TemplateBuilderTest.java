package com.consol.citrus.dsl;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.container.Template;

public class TemplateBuilderTest {
	@Test
	public void testTemplateBuilder() {
		TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
            	template("TestTemplate", echo("${var}"), sleep(2.0))
            	.globalContext(true)
            	.parameters("var", "bla");
            }
        };
        
        builder.configure();
        
        assertEquals(builder.getTestCase().getActions().size(), 1);
        assertEquals(builder.getTestCase().getActions().get(0).getClass(), Template.class);
        assertEquals(builder.getTestCase().getActions().get(0).getName(), "TestTemplate");
        
        Template container = (Template)builder.getTestCase().getActions().get(0);
        assertEquals(container.isGlobalContext(), true);
        assertEquals(container.getParameter().entrySet().iterator().next().toString(), "var=bla");
        assertEquals(container.getActions().size(), 2);
        assertEquals(container.getActions().get(0).getClass(), EchoAction.class);
	}
}
