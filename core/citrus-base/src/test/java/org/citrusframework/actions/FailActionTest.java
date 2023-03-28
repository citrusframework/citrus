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
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class FailActionTest extends UnitTestSupport {

	@Test
	public void testFailStandardMessage() {
		FailAction fail = new FailAction.Builder().build();

		try {
		    fail.execute(context);
		} catch(CitrusRuntimeException e) {
		    Assert.assertEquals("Generated error to interrupt test execution", e.getMessage());
		    return;
		}

		Assert.fail("Missing CitrusRuntimeException");
	}

	@Test
    public void testFailCustomizedMessage() {
        FailAction fail = new FailAction.Builder()
                .message("Failed because I said so")
                .build();

        try {
            fail.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals("Failed because I said so", e.getMessage());
            return;
        }

        Assert.fail("Missing CitrusRuntimeException");
    }

	@Test
    public void testFailCustomizedMessageWithVariables() {
        FailAction fail = new FailAction.Builder()
                .message("Failed because I said so, ${text}")
                .build();

        context.setVariable("text", "period!");

        try {
            fail.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals("Failed because I said so, period!", e.getMessage());
            return;
        }

        Assert.fail("Missing CitrusRuntimeException");
    }
}
