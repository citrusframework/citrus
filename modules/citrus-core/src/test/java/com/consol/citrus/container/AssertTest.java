/*
 * Copyright 2006-2009 ConSol* Software GmbH.
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

package com.consol.citrus.container;

import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.actions.FailAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * @author Christoph Christoph Deppisch Consol* Software GmbH
 */
public class AssertTest extends AbstractBaseTest {

    @Test
    public void testAssertDefaultException() {
        Assert assertAction = new Assert();
        
        assertAction.setAction(new FailAction());
        
        assertAction.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testAssertException() {
        Assert assertAction = new Assert();
        
        assertAction.setAction(new FailAction());
        
        Class exceptionClass = CitrusRuntimeException.class;
        assertAction.setException(exceptionClass);
        
        assertAction.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testAssertExceptionMessageCheck() {
        Assert assertAction = new Assert();
        
        FailAction fail = new FailAction();
        fail.setMessage("This went wrong!");
        
        assertAction.setAction(fail);
        
        Class exceptionClass = CitrusRuntimeException.class;
        assertAction.setException(exceptionClass);
        assertAction.setMessage("This went wrong!");
        
        assertAction.execute(context);
    }
    
    @Test(expectedExceptions=CitrusRuntimeException.class)
    @SuppressWarnings("unchecked")
    public void testAssertExceptionWrongMessageCheck() {
        Assert assertAction = new Assert();
        
        FailAction fail = new FailAction();
        fail.setMessage("This went wrong!");
        
        assertAction.setAction(fail);
        
        Class exceptionClass = CitrusRuntimeException.class;
        assertAction.setException(exceptionClass);
        assertAction.setMessage("Excpected error is something else");
        
        assertAction.execute(context);
    }
    
    @Test(expectedExceptions=CitrusRuntimeException.class)
    @SuppressWarnings("unchecked")
    public void testMissingException() {
        Assert assertAction = new Assert();
        
        assertAction.setAction(new EchoAction());
        
        Class exceptionClass = CitrusRuntimeException.class;
        assertAction.setException(exceptionClass);
        
        assertAction.execute(context);
    }
}
