/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.actions;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractBaseTest;

public class FailActionTest extends AbstractBaseTest {
	
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
