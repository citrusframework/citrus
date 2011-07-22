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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;

/**
 * @author Christoph Deppisch
 */
public class FailActionTest extends AbstractTestNGUnitTest {
	
	@Test
	public void testFailStandardMessage() {
		FailAction fail = new FailAction();
		
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
        FailAction fail = new FailAction();
        
        fail.setMessage("Failed because I said so");
        
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
        FailAction fail = new FailAction();
        
        context.setVariable("text", "period!");
        fail.setMessage("Failed because I said so, ${text}");
        
        try {
            fail.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals("Failed because I said so, period!", e.getMessage());
            return;
        }
        
        Assert.fail("Missing CitrusRuntimeException");
    }
}
