/*
 * Copyright 2006-2014 the original author or authors.
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

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.*;
import com.consol.citrus.container.*;
import com.consol.citrus.dsl.definition.*;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.jms.actions.PurgeJmsQueuesAction;
import com.consol.citrus.script.GroovyAction;
import com.consol.citrus.server.Server;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.actions.*;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.server.WebServiceServer;
import org.springframework.core.io.Resource;

import javax.jms.ConnectionFactory;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Christoph Deppisch
 * @since 2.0
 * @deprecated since 2.3
 */
public final class TestActions {

    /**
     * Prevent instantiation.
     */
    private TestActions() {
    }

    /**
     * Action creating new test variables during a test.
     *
     * @return
     */
    public static CreateVariablesActionDefinition createVariables() {
        CreateVariablesAction action = new CreateVariablesAction();
        return new CreateVariablesActionDefinition(action);
    }

    /**
     * Action creating a new test variable during a test.
     *
     * @return
     */
    public static CreateVariablesAction createVariable(String variableName, String value) {
        CreateVariablesAction action = new CreateVariablesAction();
        action.getVariables().put(variableName, value);
        return action;
    }

    /**
     * Creates a new ANT run action definition
     * for further configuration.
     *
     * @param buildFilePath
     * @return
     */
    public static AntRunActionDefinition antrun(String buildFilePath) {
        AntRunAction action = new AntRunAction();
        action.setBuildFilePath(buildFilePath);
        return new AntRunActionDefinition(action);
    }

    /**
     * Creates a new echo action.
     *
     * @param message
     * @return
     */
    public static EchoAction echo(String message) {
        EchoAction action = new EchoAction();
        action.setMessage(message);
        return action;
    }

    /**
     * Creates a new executePLSQL action definition
     * for further configuration.
     *
     * @param dataSource
     * @return
     */
    public static ExecutePLSQLActionDefinition plsql(DataSource dataSource) {
        ExecutePLSQLAction action = new ExecutePLSQLAction();
        action.setDataSource(dataSource);
        return new ExecutePLSQLActionDefinition(action);
    }

    /**
     * Creates a new executeSQL action definition
     * for further configuration.
     *
     * @param dataSource
     * @return
     */
    public static ExecuteSQLActionDefinition sql(DataSource dataSource) {
        ExecuteSQLAction action = new ExecuteSQLAction();
        action.setDataSource(dataSource);
        return new ExecuteSQLActionDefinition(action);
    }

    /**
     * Creates a new executesqlquery action definition
     * for further configuration.
     *
     * @param dataSource
     * @return
     */
    public static ExecuteSQLQueryActionDefinition query(DataSource dataSource) {
        ExecuteSQLQueryAction action = new ExecuteSQLQueryAction();
        action.setDataSource(dataSource);
        return new ExecuteSQLQueryActionDefinition(action);
    }

    /**
     * Creates a new receive timeout action definition
     * for further configuration.
     *
     * @param messageEndpoint
     * @return
     */
    public static ReceiveTimeoutActionDefinition expectTimeout(Endpoint messageEndpoint) {
        ReceiveTimeoutAction action = new ReceiveTimeoutAction();
        action.setEndpoint(messageEndpoint);
        return new ReceiveTimeoutActionDefinition(action);
    }

    /**
     * Creates a new receive timeout action definition from message endpoint name as String.
     *
     * @param messageEndpointUri
     * @return
     */
    public static ReceiveTimeoutActionDefinition expectTimeout(String messageEndpointUri) {
        ReceiveTimeoutAction action = new ReceiveTimeoutAction();
        action.setEndpointUri(messageEndpointUri);
        return new ReceiveTimeoutActionDefinition(action);
    }

    /**
     * Creates a new fail action.
     *
     * @param message
     * @return
     */
    public static FailAction fail(String message) {
        FailAction action = new FailAction();
        action.setMessage(message);
        return action;
    }

    /**
     * Creates a new input action.
     *
     * @return
     */
    public static InputActionDefinition input() {
        InputAction action = new InputAction();
        return new InputActionDefinition(action);
    }

    /**
     * Creates a new Java action definition from class name.
     *
     * @param className
     * @return
     */
    public static JavaActionDefinition java(String className) {
        JavaAction action = new JavaAction();
        action.setClassName(className);
        return new JavaActionDefinition(action);
    }

    /**
     * Creates a new Java action definition from Java class.
     *
     * @param clazz
     * @return
     */
    public static JavaActionDefinition java(Class<?> clazz) {
        JavaAction action = new JavaAction();
        action.setClassName(clazz.getSimpleName());
        return new JavaActionDefinition(action);
    }

    /**
     * Creates a new Java action definition from Java object instance.
     *
     * @param instance
     * @return
     */
    public static JavaActionDefinition java(Object instance) {
        JavaAction action = new JavaAction();
        action.setInstance(instance);
        return new JavaActionDefinition(action);
    }

    /**
     * Creates a new load properties action.
     *
     * @param filePath path to properties file.
     * @return
     */
    public static LoadPropertiesAction load(String filePath) {
        LoadPropertiesAction action = new LoadPropertiesAction();
        action.setFilePath(filePath);
        return action;
    }

    /**
     * Creates a new purge jms queues action definition
     * for further configuration.
     *
     * @param connectionFactory
     * @return
     */
    public static PurgeJmsQueueActionDefinition purgeQueues(ConnectionFactory connectionFactory) {
        PurgeJmsQueuesAction action = new PurgeJmsQueuesAction();
        action.setConnectionFactory(connectionFactory);
        return new PurgeJmsQueueActionDefinition(action);
    }

    /**
     * Creates a new purge message channel action definition
     * for further configuration.
     *
     * @return
     */
    public static PurgeMessageChannelActionDefinition purgeChannels() {
        PurgeMessageChannelAction action = new PurgeMessageChannelAction();
        return new PurgeMessageChannelActionDefinition(action);
    }

    /**
     * Creates special SOAP receive message action definition with web service server instance.
     *
     * @param server
     * @return
     */
    public static ReceiveSoapMessageActionDefinition receive(WebServiceServer server) {
        ReceiveSoapMessageAction action = new ReceiveSoapMessageAction();
        action.setEndpoint(server);
        return new ReceiveSoapMessageActionDefinition(action);
    }

    /**
     * Creates receive message action definition with message endpoint instance.
     *
     * @param messageEndpoint
     * @return
     */
    public static ReceiveMessageActionDefinition receive(Endpoint messageEndpoint) {
        ReceiveMessageAction action = new ReceiveMessageAction();
        action.setEndpoint(messageEndpoint);
        return new ReceiveMessageActionDefinition(action);
    }

    /**
     * Creates receive message action definition with message endpoint name.
     *
     * @param messageEndpointUri
     * @return
     */
    public static ReceiveMessageActionDefinition receive(String messageEndpointUri) {
        ReceiveMessageAction action = new ReceiveMessageAction();
        action.setEndpointUri(messageEndpointUri);
        return new ReceiveMessageActionDefinition(action);
    }

    /**
     * Create special SOAP send message action definition with web service client instance.
     *
     * @param client
     * @return
     */
    public static SendSoapMessageActionDefinition send(WebServiceClient client) {
        SendSoapMessageAction action = new SendSoapMessageAction();
        action.setEndpoint(client);
        return new SendSoapMessageActionDefinition(action);
    }

    /**
     * Create send message action definition with message endpoint instance.
     *
     * @param messageEndpoint
     * @return
     */
    public static SendMessageActionDefinition send(Endpoint messageEndpoint) {
        SendMessageAction action = new SendMessageAction();
        action.setEndpoint(messageEndpoint);
        return new SendMessageActionDefinition<SendMessageAction, SendMessageActionDefinition>(action);
    }

    /**
     * Create send message action definition with message endpoint name. According to message endpoint type
     * we can create a SOAP specific message sending action.
     *
     * @param messageEndpointUri
     * @return
     */
    public static SendMessageActionDefinition send(String messageEndpointUri) {
        SendMessageAction action = new SendMessageAction();
        action.setEndpointUri(messageEndpointUri);
        return new SendMessageActionDefinition<SendMessageAction, SendMessageActionDefinition>(action);
    }

    /**
     * Create SOAP fault send message action definition with message endpoint name. Returns SOAP fault definition with
     * specific properties for SOAP fault messages.
     *
     * @param messageEndpointUri
     * @return
     */
    public static SendSoapFaultActionDefinition sendSoapFault(String messageEndpointUri) {
        SendSoapFaultAction action = new SendSoapFaultAction();
        action.setEndpointUri(messageEndpointUri);
        return new SendSoapFaultActionDefinition(action);
    }

    /**
     * Create SOAP fault send message action definition with message endpoint instance. Returns SOAP fault definition with
     * specific properties for SOAP fault messages.
     *
     * @param messageEndpoint
     * @return
     */
    public static SendSoapFaultActionDefinition sendSoapFault(Endpoint messageEndpoint) {
        SendSoapFaultAction action = new SendSoapFaultAction();
        action.setEndpoint(messageEndpoint);
        return new SendSoapFaultActionDefinition(action);
    }

    /**
     * Add sleep action with default delay time.
     */
    public static SleepAction sleep() {
        return new SleepAction();
    }

    /**
     * Add sleep action with time in milliseconds.
     *
     * @param milliseconds
     */
    public static SleepAction sleep(long milliseconds) {
        SleepAction action = new SleepAction();
        action.setMilliseconds(String.valueOf(milliseconds));
        return action;
    }

    /**
     * Add sleep action with time in seconds.
     *
     * @param seconds
     */
    public static SleepAction sleep(double seconds) {
        SleepAction action = new SleepAction();
        action.setSeconds(String.valueOf(seconds));
        return action;
    }

    /**
     * Creates a new start server action definition
     * for further configuration.
     *
     * @param servers
     * @return
     */
    public static StartServerAction start(Server... servers) {
        StartServerAction action = new StartServerAction();
        action.getServerList().addAll(Arrays.asList(servers));
        return action;
    }

    /**
     * Creates a new start server action definition
     * for further configuration.
     *
     * @param server
     * @return
     */
    public static StartServerAction start(Server server) {
        StartServerAction action = new StartServerAction();
        action.setServer(server);
        return action;
    }

    /**
     * Creates a new stop server action definition
     * for further configuration.
     *
     * @param servers
     * @return
     */
    public static StopServerAction stop(Server... servers) {
        StopServerAction action = new StopServerAction();
        action.getServerList().addAll(Arrays.asList(servers));
        return action;
    }

    /**
     * Creates a new stop server action definition
     * for further configuration.
     *
     * @param server
     * @return
     */
    public static StopServerAction stop(Server server) {
        StopServerAction action = new StopServerAction();
        action.setServer(server);
        return action;
    }

    /**
     * Creates a new stop time action.
     *
     * @return
     */
    public static StopTimeAction stopTime() {
        return new StopTimeAction();
    }

    /**
     * Creates a new stop time action.
     *
     * @param id
     * @return
     */
    public static StopTimeAction stopTime(String id) {
        StopTimeAction action = new StopTimeAction();
        action.setId(id);
        return action;
    }

    /**
     * Creates a new trace variables action definition
     * that prints variable values to the console/logger.
     *
     * @return
     */
    public static TraceVariablesAction traceVariables() {
        return new TraceVariablesAction();
    }

    /**
     * Creates a new trace variables action definition
     * that prints variable values to the console/logger.
     *
     * @param variables
     * @return
     */
    public static TraceVariablesAction traceVariables(String... variables) {
        TraceVariablesAction action = new TraceVariablesAction();
        action.setVariableNames(Arrays.asList(variables));
        return action;
    }

    /**
     * Creates a new groovy action definition with
     * script code.
     *
     * @param script
     * @return
     */
    public static GroovyActionDefinition groovy(String script) {
        GroovyAction action = new GroovyAction();
        action.setScript(script);
        return new GroovyActionDefinition(action);
    }

    /**
     * Creates a new groovy action definition with
     * script file resource.
     *
     * @param scriptResource
     * @return
     */
    public static GroovyActionDefinition groovy(Resource scriptResource) {
        GroovyAction action = new GroovyAction();
        try {
            action.setScript(FileUtils.readToString(scriptResource));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read script resource", e);
        }

        return new GroovyActionDefinition(action);
    }

    /**
     * Creates a new transform action definition
     * for further configuration.
     *
     * @return
     */
    public static TransformActionDefinition transform() {
        return new TransformActionDefinition();
    }

    /**
     * Assert exception to happen in nested test action.
     *
     * @param testAction the nested testAction
     * @return
     */
    public static AssertDefinition assertException(TestAction testAction) {
        Assert action = new Assert();

        if (testAction instanceof AbstractActionDefinition<?>) {
            action.setAction(((AbstractActionDefinition<?>) testAction).getAction());
        } else {
            action.setAction(testAction);
        }

        return new AssertDefinition(action);
    }

    /**
     * Action catches possible exceptions in nested test actions.
     *
     * @param actions   nested test actions
     * @return
     */
    public static CatchDefinition catchException(TestAction... actions) {
        Catch container = new Catch();

        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else {
                container.addTestAction(action);
            }
        }

        return new CatchDefinition(container);
    }

    /**
     * Assert SOAP fault during action execution.
     *
     * @param testAction
     * @return
     */
    public static AssertSoapFaultDefinition assertSoapFault(TestAction testAction) {
        AssertSoapFault action = new AssertSoapFault();

        if (testAction instanceof AbstractActionDefinition<?>) {
            action.setAction(((AbstractActionDefinition<?>) testAction).getAction());
        } else {
            action.setAction(testAction);
        }

        return new AssertSoapFaultDefinition(action);
    }

    /**
     * Adds conditional container with nested test actions.
     *
     * @param actions
     * @return
     */
    public static ConditionalDefinition conditional(TestAction... actions) {
        Conditional container = new Conditional();

        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else {
                container.addTestAction(action);
            }
        }

        return new ConditionalDefinition(container);
    }

    /**
     * Adds iterate container with nested test actions.
     *
     * @param actions
     * @return
     */
    public static IterateDefinition iterate(TestAction... actions) {
        Iterate container = new Iterate();

        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else {
                container.addTestAction(action);
            }
        }

        return new IterateDefinition(container);
    }

    /**
     * Adds parallel container with nested test actions.
     *
     * @param actions
     * @return
     */
    public static Parallel parallel(TestAction... actions) {
        Parallel container = new Parallel();

        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else {
                container.addTestAction(action);
            }
        }

        return container;
    }

    /**
     * Adds repeat on error until true container with nested test actions.
     *
     * @param actions
     * @return
     */
    public static RepeatOnErrorUntilTrueDefinition repeatOnError(TestAction... actions) {
        RepeatOnErrorUntilTrue container = new RepeatOnErrorUntilTrue();

        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else {
                container.addTestAction(action);
            }
        }

        return new RepeatOnErrorUntilTrueDefinition(container);
    }

    /**
     * Adds repeat until true container with nested test actions.
     *
     * @param actions
     * @return
     */
    public static RepeatUntilTrueDefinition repeat(TestAction... actions) {
        RepeatUntilTrue container = new RepeatUntilTrue();

        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else {
                container.addTestAction(action);
            }
        }

        return new RepeatUntilTrueDefinition(container);
    }

    /**
     * Adds sequential container with nested test actions.
     *
     * @param actions
     * @return
     */
    public static Sequence sequential(TestAction... actions) {
        Sequence container = new Sequence();

        for (TestAction action : actions) {
            if (action instanceof AbstractActionDefinition<?>) {
                container.addTestAction(((AbstractActionDefinition<?>) action).getAction());
            } else {
                container.addTestAction(action);
            }
        }

        return container;
    }

    /**
     * Adds template container with nested test actions.
     *
     * @param name
     * @return
     */
    public static TemplateDefinition template(String name) {
        Template template = new Template();
        template.setName(name);
        return new TemplateDefinition(template);
    }
}
