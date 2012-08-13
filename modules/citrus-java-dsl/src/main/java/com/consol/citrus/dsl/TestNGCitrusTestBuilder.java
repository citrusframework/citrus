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

import javax.jms.ConnectionFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.integration.Message;
import org.springframework.util.CollectionUtils;
import org.testng.ITestContext;

import com.consol.citrus.*;
import com.consol.citrus.actions.*;
import com.consol.citrus.container.*;
import com.consol.citrus.script.GroovyAction;
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
    private Map<String, Object> variables = new LinkedHashMap<String, Object>();
    
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
    protected void variable(String name, Object value) {
        variables.put(name, value);
    }
    
    protected Map<String, Object> getVariables() {
    	return variables;
    }

    /**
     * Action creating new test variables during a test.
     * @return
     */
     protected CreateVariablesActionDefinition createVariables(){
     	CreateVariablesAction action = new CreateVariablesAction();
     	
     	testCase.addTestAction(action);
     	
     	return new CreateVariablesActionDefinition(action);	
     }
     
    /**
     * Creates a new echo action.
     * @param message
     * @return
     */
    protected EchoAction echo(String message) {
        EchoAction action = new EchoAction();
        action.setMessage(message);
        testCase.addTestAction(action);
        
        return action;
    }
    
    /**
     * Creates a new executePLSQL action definition
     * for further configuration.
     * @param dataSource
     * @return
     */
    protected ExecutePLSQLActionDefinition executePLSQL(DataSource dataSource) {
    	ExecutePLSQLAction action = new ExecutePLSQLAction();
    	action.setDataSource(dataSource);
    	testCase.addTestAction(action);
    	return new ExecutePLSQLActionDefinition(action);
    }
    
    /**
     * Creates a new executeSQL action definition
     * for further configuration.
     * @param dataSource
     * @return
     */
    protected ExecuteSQLActionDefinition executeSQL(DataSource dataSource) {
    	ExecuteSQLAction action = new ExecuteSQLAction();
    	action.setDataSource(dataSource);
    	testCase.addTestAction(action);
    	return new ExecuteSQLActionDefinition(action);
    }
    
    /**
     * Creates a new executesqlquery action definition 
     * for further configuration.
     * @param dataSource
     * @return
     */
    protected ExecuteSQLQueryActionDefinition executeSQLQuery(DataSource dataSource) {
    	ExecuteSQLQueryAction action = new ExecuteSQLQueryAction();
    	action.setDataSource(dataSource);
    	testCase.addTestAction(action);
    	return new ExecuteSQLQueryActionDefinition(action);
    }
    
    /**
     * Creates a new receive timeout action definition
     * for further configuration.
     * @return
     */
    protected ReceiveTimeoutActionDefinition receiveTimeout() {
    	ReceiveTimeoutAction action = new ReceiveTimeoutAction();
    	testCase.addTestAction(action);
    	return new ReceiveTimeoutActionDefinition(action);
    }
    
    /**
     * Creates a new fail action.
     * @param message
     * @return
     */
    protected FailAction fail(String message){
    	FailAction action = new FailAction();
    	action.setMessage(message);
    	testCase.addTestAction(action);
    	
    	return action;
    }
    
    /**
     * Creates a new input action.
     * @return
     */
    protected InputActionDefinition input() {
    	InputAction action = new InputAction();
    	testCase.addTestAction(action);
    	return new InputActionDefinition(action);
    }
    
    /**
     * Creates a new java action definition
     * for further configuration.
     * @param className
     * @return
     */
    protected JavaActionDefinition java(String className) {
    	JavaAction action = new JavaAction();
    	action.setClassName(className);
    	testCase.addTestAction(action);
    	return new JavaActionDefinition(action);
    }
    
    /**
     * Creates a new load properties action.
     * @param fileName the file to set
     * @return
     */
    protected LoadPropertiesAction load(String fileName) {
    	LoadPropertiesAction action = new LoadPropertiesAction();
    	action.setFile(fileName);
    	testCase.addTestAction(action);
    	return action;
    }
    
    /**
     * Creates a new purge jms queues action definition
     * for further configuration.
     * @param connectionFactory
     * @return
     */
    protected PurgeJMSQueuesActionDefinition purgeJMSQueues(ConnectionFactory connectionFactory) {
    	PurgeJmsQueuesAction action = new PurgeJmsQueuesAction();
    	action.setConnectionFactory(connectionFactory);
    	testCase.addTestAction(action);
    	return new PurgeJMSQueuesActionDefinition(action);
    }
    
    /**
     * Creates a new purge message channel action definition
     * for further configuration.
     * @param beanFactory
     * @return
     */
    protected PurgeMessageChannelActionDefinition purgeMessageChannels(BeanFactory beanFactory) {
    	PurgeMessageChannelAction action = new PurgeMessageChannelAction();
    	action.setBeanFactory(beanFactory);
    	testCase.addTestAction(action);
    	return new PurgeMessageChannelActionDefinition(action);
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
    
    /**
     * Creates a new start server action definition
     * for further configuration.
     * @return
     */
    protected StartServerActionDefinition startServer() {
    	StartServerAction action = new StartServerAction();
    	testCase.addTestAction(action);
    	return new StartServerActionDefinition(action);
    }
    
    /**
     * Creates a new stop server action definition
     * for further configuration.
     * @return
     */
    protected StopServerActionDefinition stopServer() {
    	StopServerAction action = new StopServerAction();
    	testCase.addTestAction(action);
    	return new StopServerActionDefinition(action);
    }
    
    /**
     * Creates a new stop time action.
     * @param period 
     * @return
     */
    protected StopTimeAction stopTime(String period) {
    	StopTimeAction action = new StopTimeAction();
    	action.setId(period);
    	testCase.addTestAction(action);
    	return new StopTimeAction();
    }
    
    /**
     * Creates a new trace time action.
     * @param timer
     * @return
     */
    protected TraceTimeAction traceTime(String timer) {
    	TraceTimeAction action = new TraceTimeAction();
    	
    	action.track(timer);
    	testCase.addTestAction(action);
    	
    	return action;
    }
    
    /**
     * Creates a new trace time action with a default time line.
     * @return
     */
    protected TraceTimeAction traceTime() {
    	return traceTime(TraceTimeAction.DEFAULT);
    }
    
    /**
     * Creates a new trace variables action definition
     * that prints variable values to the console/logger.
     * @return
     */
    protected TraceVariablesActionDefinition traceVariables(){
    	TraceVariablesAction action = new TraceVariablesAction();
    	
    	testCase.addTestAction(action);
    	
    	return new TraceVariablesActionDefinition(action);
	}
    
    /**
     * Creates a new groovy action definition
     * for further configuration.
     * @return
     */
    protected GroovyActionDefinition groovy() {
    	GroovyAction action = new GroovyAction();
    	testCase.addTestAction(action);
    	return new GroovyActionDefinition(action);
    }
    
    /**
     * Creates a new transform action definition
     * for further configuration.
     * @return
     */
    protected TransformActionDefinition transform(){
    	TransformAction action = new TransformAction();
    	testCase.addTestAction(action);
    	return new TransformActionDefinition(action);
    }
    
    /**
     * Assert exception to happen in nested test action.
     * @param testAction the nested testAction
     * @param message the message to set
     * @param exception the exception to set
     * @return
     */
    protected Assert assertException(TestAction testAction, String message, Class<? extends Throwable> exception)
    {
    	Assert action = new Assert();
    	action.setAction(testAction);
    	action.setMessage(message);
    	action.setException(exception);
    		
    	testCase.getActions().remove((testCase.getActions().size()) -1);
    	testCase.addTestAction(action);
    	
		return action;	
    }
    
    /**
     * Action catches possible exceptions in nested test actions.
     * @param exception the exception to be caught
     * @param actions nested test actions
     * @return
     */
    protected Catch catchException(String exception, TestAction ... actions)
    {
    	Catch container = new Catch();
    	container.setException(exception);
    	
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
     * Adds conditional container with nested test actions.
     * @param actions
     * @return
     */
    protected ConditionalDefinition conditional(TestAction ... actions) {
    	Conditional container = new Conditional();
    	
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
        
        return new ConditionalDefinition(container);
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
     * Adds repeat on error until true container with nested test actions.
     * @param actions
     * @return
     */
    protected RepeatOnErrorUntilTrueDefinition repeatOnErrorUntilTrue(TestAction... actions) {
    	RepeatOnErrorUntilTrue container = new RepeatOnErrorUntilTrue();
    	
    	for(TestAction action : actions) {
    		if (action instanceof AbstractActionDefinition<?>) {
                testCase.getActions().remove(((AbstractActionDefinition<?>) action).getAction());
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else {
                testCase.getActions().remove(action);
                container.addTestAction(action);
            }
    	}
    	
    	testCase.addTestAction(container);
    	return new RepeatOnErrorUntilTrueDefinition(container);
    }
    
    /**
     * Adds repeat until true container with nested test actions.
     * @param actions
     * @return
     */
    protected RepeatUntilTrueDefinition repeatUntilTrue(TestAction... actions) {
    	RepeatUntilTrue container = new RepeatUntilTrue();
    	
    	for(TestAction action : actions) {
    		if (action instanceof AbstractActionDefinition<?>) {
                testCase.getActions().remove(((AbstractActionDefinition<?>) action).getAction());
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else {
                testCase.getActions().remove(action);
                container.addTestAction(action);
            }
    	}
    	
    	testCase.addTestAction(container);
    	return new RepeatUntilTrueDefinition(container);
    }
    
    /**
     * Adds sequence after suite container with nested test actions.
     * @param actions
     * @return
     */
    protected SequenceAfterSuiteDefinition sequenceAfterSuite(TestAction... actions) {
    	SequenceAfterSuite container = new SequenceAfterSuite();
    	
    	for(TestAction action : actions) {
    		if (action instanceof AbstractActionDefinition<?>) {
                testCase.getActions().remove(((AbstractActionDefinition<?>) action).getAction());
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else {
                testCase.getActions().remove(action);
                container.addTestAction(action);
            }
    	}
    	
    	testCase.addTestAction(container);
    	return new SequenceAfterSuiteDefinition(container);
    }
    
    /**
     * Adds sequence before suite container with nested test actions.
     * @param actions
     * @return
     */
    protected SequenceBeforeSuiteDefinition sequenceBeforeSuite(TestAction... actions) {
    	SequenceBeforeSuite container = new SequenceBeforeSuite();
    	
    	for(TestAction action : actions) {
    		if (action instanceof AbstractActionDefinition<?>) {
                testCase.getActions().remove(((AbstractActionDefinition<?>) action).getAction());
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else {
                testCase.getActions().remove(action);
                container.addTestAction(action);
            }
    	}
    	
    	testCase.addTestAction(container);
    	return new SequenceBeforeSuiteDefinition(container);
    }
    
    /**
     * Adds sequence before container with nested test actions. 
     * @param actions
     * @return
     */
    protected SequenceBeforeTest sequenceBeforeTest(TestAction... actions) {
    	SequenceBeforeTest container = new SequenceBeforeTest();
    	
    	for(TestAction action : actions) {
    		if (action instanceof AbstractActionDefinition<?>) {
                testCase.getActions().remove(((AbstractActionDefinition<?>) action).getAction());
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else {
                testCase.getActions().remove(action);
                container.addTestAction(action);
            }
    	}
    	
    	testCase.addTestAction(container);
    	return container;
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
     * Adds template container with nested test actions.
     * @param templateName
     * @param actions
     * @return
     */
    protected TemplateDefinition template(String templateName, TestAction... actions) {
    	Template container = new Template();
    	container.setName(templateName);
    	
    	for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                testCase.getActions().remove(((AbstractActionDefinition<?>) action).getAction());
                container.getActions().add(((AbstractActionDefinition<?>) action).getAction());
            } else {
                testCase.getActions().remove(action);
                container.getActions().add(action);
            }
        }
    	
    	testCase.addTestAction(container);
    	return new TemplateDefinition(container);
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
