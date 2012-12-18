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

import java.io.IOException;
import java.util.*;

import javax.jms.ConnectionFactory;
import javax.sql.DataSource;

import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.testng.ITestContext;

import com.consol.citrus.*;
import com.consol.citrus.actions.*;
import com.consol.citrus.container.*;
import com.consol.citrus.dsl.definition.*;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.message.MessageSender;
import com.consol.citrus.script.GroovyAction;
import com.consol.citrus.server.Server;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.actions.*;
import com.consol.citrus.ws.message.SoapReplyMessageReceiver;
import com.consol.citrus.ws.message.WebServiceMessageSender;

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
    
    /**
     * Get the test variables.
     * @return
     */
    protected Map<String, Object> getVariables() {
        return variables;
    }

    /**
     * Action creating new test variables during a test.
     * @return
     */
    protected CreateVariablesActionDefinition variables() {
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
    protected ExecutePLSQLActionDefinition plsql(DataSource dataSource) {
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
    protected ExecuteSQLActionDefinition sql(DataSource dataSource) {
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
    protected ExecuteSQLQueryActionDefinition query(DataSource dataSource) {
        ExecuteSQLQueryAction action = new ExecuteSQLQueryAction();
        action.setDataSource(dataSource);
        testCase.addTestAction(action);
        return new ExecuteSQLQueryActionDefinition(action);
    }
    
    /**
     * Creates a new receive timeout action definition
     * for further configuration.
     * @param messageReceiver
     * @return
     */
    protected ReceiveTimeoutActionDefinition expectTimeout(MessageReceiver messageReceiver) {
        ReceiveTimeoutAction action = new ReceiveTimeoutAction();
        action.setMessageReceiver(messageReceiver);
        testCase.addTestAction(action);
        return new ReceiveTimeoutActionDefinition(action);
    }
    
    /**
     * Creates a new fail action.
     * @param message
     * @return
     */
    protected FailAction fail(String message) {
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
     * Creates a new Java action definition from class name.
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
     * Creates a new Java action definition from Java class.
     * @param className
     * @return
     */
    protected JavaActionDefinition java(Class<?> clazz) {
        JavaAction action = new JavaAction();
        action.setClassName(clazz.getSimpleName());
        testCase.addTestAction(action);
        return new JavaActionDefinition(action);
    }
    
    /**
     * Creates a new Java action definition from Java object instance.
     * @param instance
     * @return
     */
    protected JavaActionDefinition java(Object instance) {
        JavaAction action = new JavaAction();
        action.setInstance(instance);
        testCase.addTestAction(action);
        return new JavaActionDefinition(action);
    }
    
    /**
     * Creates a new load properties action.
     * @param filePath path to properties file.
     * @return
     */
    protected LoadPropertiesAction load(String filePath) {
        LoadPropertiesAction action = new LoadPropertiesAction();
        action.setFilePath(filePath);
        testCase.addTestAction(action);
        return action;
    }
    
    /**
     * Creates a new purge jms queues action definition
     * for further configuration.
     * @param connectionFactory
     * @return
     */
    protected PurgeJMSQueuesActionDefinition purgeQueues(ConnectionFactory connectionFactory) {
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
    protected PurgeMessageChannelActionDefinition purgeChannels() {
        PurgeMessageChannelAction action = new PurgeMessageChannelAction();
        testCase.addTestAction(action);
        return new PurgeMessageChannelActionDefinition(action, applicationContext);
    }
    
    /**
     * Creates special SOAP receive message action definition with message receiver instance.
     * @param messageReceiver
     * @return
     */
    protected ReceiveSoapMessageActionDefinition receive(SoapReplyMessageReceiver messageReceiver) {
        ReceiveSoapMessageAction action = new ReceiveSoapMessageAction();
        action.setMessageReceiver(messageReceiver);
        
        testCase.addTestAction(action);
        return new ReceiveSoapMessageActionDefinition(action, applicationContext);
    }
    
    /**
     * Creates receive message action definition with message receiver instance.
     * @param messageReceiver
     * @return
     */
    protected ReceiveMessageActionDefinition receive(MessageReceiver messageReceiver) {
        ReceiveMessageAction action = new ReceiveMessageAction();
        action.setMessageReceiver(messageReceiver);
        
        testCase.addTestAction(action);
        return new ReceiveMessageActionDefinition(action, applicationContext, new PositionHandle(testCase.getActions()));
    }
    
    /**
     * Creates receive message action definition with messsage receiver name.
     * @param messageReceiverName
     * @return
     */
    protected ReceiveMessageActionDefinition receive(String messageReceiverName) {
        MessageReceiver messageReceiver = applicationContext.getBean(messageReceiverName, MessageReceiver.class);
        
        if (messageReceiver instanceof SoapReplyMessageReceiver) {
            ReceiveSoapMessageAction action = new ReceiveSoapMessageAction();
            action.setMessageReceiver(messageReceiver);
            testCase.addTestAction(action);
            
            return new ReceiveSoapMessageActionDefinition(action, applicationContext);
        } else {
            ReceiveMessageAction action = new ReceiveMessageAction();
            action.setMessageReceiver(messageReceiver);
            testCase.addTestAction(action);
            
            return new ReceiveMessageActionDefinition(action, applicationContext, new PositionHandle(testCase.getActions()));
        }
    }
    
    /**
     * Create special SOAP send message action definition with message sender instance.
     * @param messageSender
     * @return
     */
    protected SendSoapMessageActionDefinition send(WebServiceMessageSender messageSender) {
        SendSoapMessageAction action = new SendSoapMessageAction();
        action.setMessageSender(messageSender);
        
        testCase.addTestAction(action);
        return new SendSoapMessageActionDefinition(action);
    }
    
    /**
     * Create send message action definition with message sender instance.
     * @param messageSender
     * @return
     */
    protected SendMessageActionDefinition send(MessageSender messageSender) {
        SendMessageAction action = new SendMessageAction();
        action.setMessageSender(messageSender);
        
        testCase.addTestAction(action);
        return new SendMessageActionDefinition(action, new PositionHandle(testCase.getActions()));
    }
    
    /**
     * Create send message action definition with message sender name. According to message sender type
     * we can create a SOAP specific message sending action.
     * @param messageSenderName
     * @return
     */
    protected SendMessageActionDefinition send(String messageSenderName) {
        MessageSender messageSender = applicationContext.getBean(messageSenderName, MessageSender.class);
        
        if (messageSender instanceof WebServiceMessageSender) {
            SendSoapMessageAction action = new SendSoapMessageAction();
            action.setMessageSender(messageSender);
            
            testCase.addTestAction(action);
            return new SendSoapMessageActionDefinition(action);
        } else {
            SendMessageAction action = new SendMessageAction();
            action.setMessageSender(messageSender);
            
            testCase.addTestAction(action);
            return new SendMessageActionDefinition(action, new PositionHandle(testCase.getActions()));
        }
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
     * @param servers
     * @return
     */
    protected StartServerAction start(Server... servers) {
        StartServerAction action = new StartServerAction();
        action.getServerList().addAll(Arrays.asList(servers));
        testCase.addTestAction(action);
        return action;
    }
    
    /**
     * Creates a new start server action definition
     * for further configuration.
     * @param server
     * @return
     */
    protected StartServerAction start(Server server) {
        StartServerAction action = new StartServerAction();
        action.setServer(server);
        testCase.addTestAction(action);
        return action;
    }
    
    /**
     * Creates a new stop server action definition
     * for further configuration.
     * @param servers
     * @return
     */
    protected StopServerAction stop(Server... servers) {
        StopServerAction action = new StopServerAction();
        action.getServerList().addAll(Arrays.asList(servers));
        testCase.addTestAction(action);
        return action;
    }
    
    /**
     * Creates a new stop server action definition
     * for further configuration.
     * @param server
     * @return
     */
    protected StopServerAction stop(Server server) {
        StopServerAction action = new StopServerAction();
        action.setServer(server);
        testCase.addTestAction(action);
        return action;
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
     * Creates a new trace variables action definition
     * that prints variable values to the console/logger.
     * @return
     */
    protected TraceVariablesAction traceVariables() {
        TraceVariablesAction action = new TraceVariablesAction();

        testCase.addTestAction(action);
        return action;
    }
    
    /**
     * Creates a new trace variables action definition
     * that prints variable values to the console/logger.
     * @param variables
     * @return
     */
    protected TraceVariablesAction traceVariables(String... variables) {
        TraceVariablesAction action = new TraceVariablesAction();
        action.setVariableNames(Arrays.asList(variables));
        
        testCase.addTestAction(action);
        return action;
    }
    
    /**
     * Creates a new groovy action definition with
     * script code.
     * @param script
     * @return
     */
    protected GroovyActionDefinition groovy(String script) {
        GroovyAction action = new GroovyAction();
        action.setScript(script);
        
        testCase.addTestAction(action);
        
        return new GroovyActionDefinition(action);
    }
    
    /**
     * Creates a new groovy action definition with
     * script file resource.
     * @param scriptResource
     * @return
     */
    protected GroovyActionDefinition groovy(Resource scriptResource) {
        GroovyAction action = new GroovyAction();
        try {
            action.setScript(FileUtils.readToString(scriptResource));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read script resource", e);
        }
        
        testCase.addTestAction(action);
        
        return new GroovyActionDefinition(action);
    }
    
    /**
     * Creates a new transform action definition
     * for further configuration.
     * @return
     */
    protected TransformActionDefinition transform() {
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
    protected AssertDefinition assertException(TestAction testAction) {
        Assert action = new Assert();
        action.setAction(testAction);
            
        testCase.getActions().remove((testCase.getActions().size()) -1);
        testCase.addTestAction(action);
        
        return new AssertDefinition(action);
    }
    
    /**
     * Action catches possible exceptions in nested test actions.
     * @param exception the exception to be caught
     * @param actions nested test actions
     * @return
     */
    protected Catch catchException(String exception, TestAction ... actions) {
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
     * Action catches poissible exceptions in neste test actions.
     * @param exception
     * @param actions
     * @return
     */
    protected Catch catchException(Class<? extends Throwable> exception, TestAction ... actions) {
        return catchException(exception.getName(), actions);
    }
    
    /**
     * Assert SOAP fault during action execution.
     * 
     * @param testAction
     * @return
     */
    protected AssertSoapFaultDefinition soapFault(TestAction testAction) {
       AssertSoapFault action = new AssertSoapFault();
       action.setAction(testAction);
       
       testCase.getActions().remove((testCase.getActions().size()) -1);
       testCase.addTestAction(action);
       
       return new AssertSoapFaultDefinition(action);  
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
    protected RepeatOnErrorUntilTrueDefinition repeatOnError(TestAction... actions) {
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
    protected RepeatUntilTrueDefinition repeat(TestAction... actions) {
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
    protected TemplateDefinition template(String name) {
        Template template = new Template();
        template.setName(name);
        
        Template rootTemplate = applicationContext.getBean(name, Template.class);
        
        template.setGlobalContext(rootTemplate.isGlobalContext());
        template.setActor(rootTemplate.getActor());
        template.setActions(rootTemplate.getActions());
        template.setParameter(rootTemplate.getParameter());
        
        testCase.addTestAction(template);
        return new TemplateDefinition(template);
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
    protected TestCase getTestCase() {
        return testCase;
    }
}
