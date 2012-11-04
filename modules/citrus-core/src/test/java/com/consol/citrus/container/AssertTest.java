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

package com.consol.citrus.container;

import org.testng.annotations.Test;

import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.actions.FailAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;

/**
 * @author Christoph Deppisch
 */
public class AssertTest extends AbstractTestNGUnitTest {

    @Test
    public void testAssertDefaultException() {
        Assert assertAction = new Assert();
        
        assertAction.setAction(new FailAction());
        
        assertAction.execute(context);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testAssertException() {
        Assert assertAction = new Assert();
        
        assertAction.setAction(new FailAction());
        
        Class exceptionClass = CitrusRuntimeException.class;
        assertAction.setException(exceptionClass);
        
        assertAction.execute(context);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
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
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testVariableSupport() {
        Assert assertAction = new Assert();
        
        context.setVariable("message", "This went wrong!");
        
        FailAction fail = new FailAction();
        fail.setMessage("This went wrong!");
        
        assertAction.setAction(fail);
        
        Class exceptionClass = CitrusRuntimeException.class;
        assertAction.setException(exceptionClass);
        assertAction.setMessage("${message}");
        
        assertAction.execute(context);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testValidationMatcherSupport() {
        Assert assertAction = new Assert();
        
        FailAction fail = new FailAction();
        fail.setMessage("This went wrong!");
        
        assertAction.setAction(fail);
        
        Class exceptionClass = CitrusRuntimeException.class;
        assertAction.setException(exceptionClass);
        assertAction.setMessage("@contains('wrong')@");
        
        assertAction.execute(context);
    }
    
    @Test(expectedExceptions=CitrusRuntimeException.class)
    @SuppressWarnings({ "unchecked", "rawtypes" })
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testMissingException() {
        Assert assertAction = new Assert();
        
        assertAction.setAction(new EchoAction());
        
        Class exceptionClass = CitrusRuntimeException.class;
        assertAction.setException(exceptionClass);
        
        assertAction.execute(context);
    }
}
