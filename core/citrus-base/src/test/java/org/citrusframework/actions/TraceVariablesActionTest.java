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

package org.citrusframework.actions;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TraceVariablesActionTest extends UnitTestSupport {

	@Test
	public void testTraceVariables() {
        TraceVariablesAction trace = new TraceVariablesAction.Builder()
                .build();
		trace.execute(context);
	}

	@Test
    public void testTraceSelectedVariables() {
        context.setVariable("myVariable", "traceMe");

        TraceVariablesAction trace = new TraceVariablesAction.Builder()
                .variable("myVariable")
                .build();
        trace.execute(context);
    }

	@Test(expectedExceptions=CitrusRuntimeException.class)
    public void testTraceUnknownVariable() {
        TraceVariablesAction trace = new TraceVariablesAction.Builder()
                .variable("myVariable")
                .build();
        trace.execute(context);
    }
}
