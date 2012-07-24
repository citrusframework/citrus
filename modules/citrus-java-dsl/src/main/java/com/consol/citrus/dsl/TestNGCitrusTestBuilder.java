/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl;

import java.util.*;

import org.springframework.integration.Message;
import org.springframework.util.CollectionUtils;
import org.testng.ITestContext;

import com.consol.citrus.*;
import com.consol.citrus.actions.*;
import com.consol.citrus.container.*;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;
import com.consol.citrus.util.MessageUtils;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;

/**
 * Test case builder offers methods for constructing a test case with several
 * test actions in Java DSL language.
 * 
 * @author Christoph Deppisch
 */
public class TestNGCitrusTestBuilder extends AbstractTestNGCitrusTest {

    /** This builders test case */
    private TestCase testCase;
    
    /** The test variables to set before execution */
    private Map<String, String> variables = new LinkedHashMap<String, String>();
    
    /**
     * Default constructor.
     */
    public TestNGCitrusTestBuilder() {
        testCase = new TestCase();
        testCase.setBeanName(this.getClass().getSimpleName());
        testCase.setName(this.getClass().getSimpleName());
        testCase.setPackageName(this.getClass().getPackage().getName());
        
        testCase.setVariableDefinitions(variables);
    }
    
    @Override
    protected void executeTest(ITestContext testContext) {
        configure();
        super.executeTest(testContext);
    }
    
    /**
     * Configures the test case with test actions. Subclasses may override this method in order
     * to contribute test actions to this test case.
     */
    protected void configure() {
    }
    
    /**
     * Adds description to the test case. 
     * @param description
     */
    protected void description(String description) {
        testCase.setDescription(description);
    }
    
    /**
     * Adds author to the test case.
     * @param author
     */
    protected void author(String author) {
        testCase.getMetaInfo().setAuthor(author);
    }

    /**
     * Sets test case status.
     * @param status
     */
    protected void status(TestCaseMetaInfo.Status status) {
        testCase.getMetaInfo().setStatus(status);
    }
    
    /**
     * Sets the creation date.
     * @param date
     */
    protected void creationDate(Date date) {
        testCase.getMetaInfo().setCreationDate(date);
    }
    
    /**
     * Adds a new variable definition to the set of test variables 
     * for this test case.
     * @param name
     * @param value
     */
    protected void variable(String name, String value) {
        variables.put(name, value);
    }
    
    /**
     * Creates a new echo action.
     * @param message
     */
    protected EchoAction echo(String message) {
        EchoAction action = new EchoAction();
        action.setMessage(message);
        testCase.addTestAction(action);
        
        return action;
    }
    
    protected TraceTimeAction traceTime(String timer) {
    	TraceTimeAction action = new TraceTimeAction();
    	
    	action.track(timer);
    	testCase.addTestAction(action);
    	
    	return action;
    }
    
    protected TraceTimeAction traceTime() {
    	return traceTime(TraceTimeAction.DEFAULT);
    }
    
    /**
     * Basic send method just returning a new empty send action 
     * definition for further configuration.
     * @return
     */
    protected SendMessageActionDefinition send() {
        SendMessageAction action = new SendMessageAction();
        testCase.addTestAction(action);
        return new SendMessageActionDefinition(action, applicationContext);
    }
    
    /**
     * Send action definition with message payload and message headers.
     * @param message
     * @return
     */
    protected SendMessageActionDefinition send(Message<String> message) {
        SendMessageAction action = new SendMessageAction();
        
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData(message.getPayload());
        
        Map<String, Object> headers = new HashMap<String, Object>();
        for (String headerName : message.getHeaders().keySet()) {
            if (!MessageUtils.isSpringInternalHeader(headerName)) {
                headers.put(headerName, message.getHeaders().get(headerName));
            }
        }
        
        messageBuilder.setMessageHeaders(headers);
        
        action.setMessageBuilder(messageBuilder);
        
        testCase.addTestAction(action);
        
        return new SendMessageActionDefinition(action, applicationContext);
    }
    
    /**
     * Basic receive method creates empty receive action definition 
     * for further configuration.
     * @return
     */
    protected ReceiveMessageActionDefinition receive() {
        ReceiveMessageAction action = new ReceiveMessageAction();
        testCase.addTestAction(action);
        return new ReceiveMessageActionDefinition(action, applicationContext);
    }
    
    /**
     * Receive message action definition with control message.
     * @param controlMessage
     */
    protected ReceiveMessageActionDefinition receive(Message<?> controlMessage) {
        ReceiveMessageAction action = new ReceiveMessageAction();
        
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setControlMessage(controlMessage);
        
        action.getValidationContexts().add(validationContext);
        
        testCase.addTestAction(action);
        
        return new ReceiveMessageActionDefinition(action, applicationContext);
    }
    
    protected JavaActionDefinition java(String className) {
    	JavaAction action = new JavaAction();
    	action.setClassName(className);
    	testCase.addTestAction(action);
    	return new JavaActionDefinition(action);
    }
    
    /**
     * Add sleep action with time in milliseconds.
     * @param time
     */
    protected SleepAction sleep(long time) {
        SleepAction action = new SleepAction();
        action.setDelay(String.valueOf((double)time / 1000));
        
        testCase.addTestAction(action);
        
        return action;
    }
    
    /**
     * Add sleep action with time in seconds.
     * @param time
     */
    protected SleepAction sleep(double time) {
        SleepAction action = new SleepAction();
        action.setDelay(String.valueOf(time));
        
        testCase.addTestAction(action);
        
        return action;
    }
    
    protected InputActionDefinition input() {
    	InputAction action = new InputAction();
    	testCase.addTestAction(action);
    	return new InputActionDefinition(action);
    	
    }
    
    /**
     * Adds sequential container with nested test actions.
     * @param actions
     * @return
     */
    protected Sequence sequential(TestAction ... actions) {
        Sequence container = new Sequence();
        
        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                testCase.getActions().remove(((AbstractActionDefinition<?>) action).getAction());
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else {
                testCase.getActions().remove(action);
                container.addTestAction(action);
            }
        }
        
        testCase.getActions().add(container);
        
        return container;
    }
    
    /**
     * Adds parallel container with nested test actions.
     * @param actions
     * @return
     */
    protected Parallel parallel(TestAction ... actions) {
        Parallel container = new Parallel();
        
        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                testCase.getActions().remove(((AbstractActionDefinition<?>) action).getAction());
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else {
                testCase.getActions().remove(action);
                container.addTestAction(action);
            }
        }
        
        testCase.getActions().add(container);
        
        return container;
    }
    
    /**
     * Adds iterate container with nested test actions.
     * @param actions
     * @return
     */
    protected IterateDefinition iterate(TestAction ... actions) {
        Iterate container = new Iterate();
        
        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                testCase.getActions().remove(((AbstractActionDefinition<?>) action).getAction());
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else {
                testCase.getActions().remove(action);
                container.addTestAction(action);
            }
        }
        
        testCase.getActions().add(container);
        
        return new IterateDefinition(container);
    }
    
    /**
     * Adds sequence of test actions to finally block.
     * @param actions
     */
    @SuppressWarnings("unchecked")
    protected void doFinally(TestAction ... actions) {
        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                testCase.getActions().remove(((AbstractActionDefinition<?>) action).getAction());
            } else {
                testCase.getActions().remove(action);
            }
        }
        
        testCase.getFinallyChain().addAll(CollectionUtils.arrayToList(actions));
    }

    /**
     * Gets the testCase.
     * @return the testCase the testCase to get.
     */
    public TestCase getTestCase() {
        return testCase;
    }
}
