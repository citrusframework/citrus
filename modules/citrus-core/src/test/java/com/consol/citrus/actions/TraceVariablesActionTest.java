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

import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;

/**
 * @author Christoph Deppisch
 */
public class TraceVariablesActionTest extends AbstractTestNGUnitTest {
	
	@Test
	public void testTraceVariables() throws InterruptedException {
		TraceVariablesAction trace = new TraceVariablesAction();
		
		trace.execute(context);
	}
	
	@Test
    public void testTraceSelectedVariables() throws InterruptedException {
        TraceVariablesAction trace = new TraceVariablesAction();
        
        context.setVariable("myVariable", "traceMe");
        
        List<String> variables = Collections.singletonList("myVariable");
        trace.setVariableNames(variables);
        trace.execute(context);
    }
	
	@Test(expectedExceptions=CitrusRuntimeException.class)
    public void testTraceUnknownVariable() throws InterruptedException {
        TraceVariablesAction trace = new TraceVariablesAction();
        
        List<String> variables = Collections.singletonList("myVariable");
        trace.setVariableNames(variables);
        trace.execute(context);
    }
}
