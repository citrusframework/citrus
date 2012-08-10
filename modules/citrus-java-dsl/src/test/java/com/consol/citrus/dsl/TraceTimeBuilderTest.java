package com.consol.citrus.dsl;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.consol.citrus.actions.TraceTimeAction;

public class TraceTimeBuilderTest {
	@Test
    public void testTraceTimeBuilder() {		
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            protected void configure() {
                traceTime("Test1");
                sleep(2);
                traceTime();
                traceTime("Test1");
                sleep(1);
                traceTime("Test1");
                traceTime();
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActionCount(), 7);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), TraceTimeAction.class);
        
        TraceTimeAction action = (TraceTimeAction)builder.getTestCase().getActions().get(0);
        Assert.assertEquals(action.getName(), TraceTimeAction.class.getSimpleName());
	}
}
