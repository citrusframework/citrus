/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.actions;

import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractBaseTest;

public class TraceVariablesActionTest extends AbstractBaseTest {
	
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
