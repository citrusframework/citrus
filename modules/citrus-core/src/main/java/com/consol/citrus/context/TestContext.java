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

package com.consol.citrus.context;

import com.consol.citrus.Citrus;
import com.consol.citrus.TestCase;
import com.consol.citrus.TestResult;
import com.consol.citrus.container.StopTimer;
import com.consol.citrus.endpoint.EndpointFactory;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.VariableNullValueException;
import com.consol.citrus.functions.FunctionRegistry;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.message.DefaultMessageStore;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageStore;
import com.consol.citrus.report.MessageListeners;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.util.TypeConversionUtils;
import com.consol.citrus.validation.MessageValidatorRegistry;
import com.consol.citrus.validation.interceptor.GlobalMessageConstructionInterceptors;
import com.consol.citrus.validation.matcher.ValidationMatcherRegistry;
import com.consol.citrus.variable.GlobalVariables;
import com.consol.citrus.variable.VariableUtils;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;
import org.javatuples.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class holding and managing test variables. The test context also provides utility methods
 * for replacing dynamic content(variables and functions) in message payloads and headers.
 */
public class TestContext {
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(TestContext.class);

    /**
     * Local variables
     */
    protected Map<String, Object> variables;

    /**
     * Global variables
     */
    private GlobalVariables globalVariables;

    /**
     * Message store
     */
    private MessageStore messageStore = new DefaultMessageStore();

    /**
     * Function registry holding all available functions
     */
    private FunctionRegistry functionRegistry = new FunctionRegistry();

    /**
     * Endpoint factory creates endpoint instances
     */
    private EndpointFactory endpointFactory;

    /**
     * Bean reference resolver
     */
    private ReferenceResolver referenceResolver;

    /**
     * Registered message validators
     */
    private MessageValidatorRegistry messageValidatorRegistry = new MessageValidatorRegistry();

    /**
     * Registered validation matchers
     */
    private ValidationMatcherRegistry validationMatcherRegistry = new ValidationMatcherRegistry();

    /**
     * List of test listeners to be informed on test events
     */
    private TestListeners testListeners = new TestListeners();

    /**
     * List of message listeners to be informed on inbound and outbound message exchange
     */
    private MessageListeners messageListeners = new MessageListeners();

    /**
     * List of global message construction interceptors
     */
    private GlobalMessageConstructionInterceptors globalMessageConstructionInterceptors = new GlobalMessageConstructionInterceptors();

    /**
     * Central namespace context builder
     */
    private NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();

    /**
     * Spring bean application context
     */
    private ApplicationContext applicationContext;

    /**
     * Timers registered in test context, that can be stopped
     */
    protected Map<String, StopTimer> timers = new ConcurrentHashMap<>();

    /**
     * List of exceptions that actions raised during execution of forked operations
     */
    private List<CitrusRuntimeException> exceptions = new ArrayList<>();

    /**
     * Default constructor
     */
    public TestContext() {
        variables = new ConcurrentHashMap<>();
    }

    /**
     * Gets the value for the given variable expression. Expression usually is the
     * simple variable name, with optional expression prefix/suffix.
     * <p>
     * In case variable is not known to the context throw runtime exception.
     *
     * @param variableExpression expression to search for.
     * @return value of the variable
     * @throws CitrusRuntimeException
     */
    public String getVariable(final String variableExpression) {
        return getVariable(variableExpression, String.class);
    }

    /**
     * Gets typed variable value.
     *
     * @param variableExpression
     * @param type
     * @param <T>
     * @return
     */
    public <T> T getVariable(String variableExpression, Class<T> type) {
        return TypeConversionUtils.convertIfNecessary(getVariableObject(variableExpression), type);
    }

    /**
     * Gets the value for the given variable as object representation.
     * Use this method if you seek for test objects stored in the context.
     *
     * @param variableExpression expression to search for.
     * @return value of the variable as object
     * @throws CitrusRuntimeException
     */
    public Object getVariableObject(final String variableExpression) {
        String variableName = VariableUtils.cutOffVariablesPrefix(variableExpression);

        if (variableName.startsWith(Citrus.VARIABLE_ESCAPE) && variableName.endsWith(Citrus.VARIABLE_ESCAPE)) {
            return Citrus.VARIABLE_PREFIX + VariableUtils.cutOffVariablesEscaping(variableName) + Citrus.VARIABLE_SUFFIX;
        } else if (variables.containsKey(variableName)) {
            return variables.get(variableName);
        } else if (variableName.contains(".")) {
            String objectName = variableName.substring(0, variableName.indexOf("."));
            if (variables.containsKey(objectName)) {
                return getVariable(variables.get(objectName), variableName.substring(variableName.indexOf(".") + 1));
            }
        }

        throw new CitrusRuntimeException("Unknown variable '" + variableName + "'");
    }

    /**
     * Gets variable from path expression. Variable paths are translated to reflection fields on object instances.
     * Path separators are '.'. Each separator is handled as object hierarchy.
     *
     * @param instance
     * @param pathExpression
     */
    private Object getVariable(Object instance, String pathExpression) {
        String leftOver = null;
        String fieldName;
        if (pathExpression.contains(".")) {
            fieldName = pathExpression.substring(0, pathExpression.indexOf("."));
            leftOver = pathExpression.substring(pathExpression.indexOf(".") + 1);
        } else {
            fieldName = pathExpression;
        }

        Field field = ReflectionUtils.findField(instance.getClass(), fieldName);
        if (field == null) {
            throw new CitrusRuntimeException(String.format("Failed to get variable - unknown field '%s' on type %s", fieldName, instance.getClass().getName()));
        }

        ReflectionUtils.makeAccessible(field);
        Object fieldValue = ReflectionUtils.getField(field, instance);

        if (StringUtils.hasText(leftOver)) {
            return getVariable(fieldValue, leftOver);
        }

        return fieldValue;
    }

    /**
     * Creates a new variable in this test context with the respective value. In case variable already exists
     * variable is overwritten.
     *
     * @param variableName the name of the new variable
     * @param value        the new variable value
     * @return
     * @throws CitrusRuntimeException
     */
    public void setVariable(final String variableName, Object value) {
        if (!StringUtils.hasText(variableName) || VariableUtils.cutOffVariablesPrefix(variableName).length() == 0) {
            throw new CitrusRuntimeException("Can not create variable '" + variableName + "', please define proper variable name");
        }

        if (value == null) {
            throw new VariableNullValueException("Trying to set variable: " + VariableUtils.cutOffVariablesPrefix(variableName) + ", but variable value is null");
        }

        if (log.isDebugEnabled()) {
            log.debug("Setting variable: " + VariableUtils.cutOffVariablesPrefix(variableName) + " with value: '" + value + "'");
        }

        variables.put(VariableUtils.cutOffVariablesPrefix(variableName), value);
    }

    /**
     * Add variables to context.
     *
     * @param variableNames  the variable names to set
     * @param variableValues the variable values to set
     */
    public void addVariables(String[] variableNames, Object[] variableValues) {
        if (variableNames.length != variableValues.length) {
            throw new CitrusRuntimeException(String.format(
                    "Invalid context variable usage - received '%s' variables with '%s' values",
                    variableNames.length,
                    variableValues.length));
        }

        for (int i = 0; i < variableNames.length; i++) {
            if (variableValues[i] != null) {
                setVariable(variableNames[i], variableValues[i]);
            }
        }
    }

    /**
     * Add several new variables to test context. Existing variables will be
     * overwritten.
     *
     * @param variablesToSet the list of variables to set.
     */
    public void addVariables(Map<String, Object> variablesToSet) {
        for (Entry<String, Object> entry : variablesToSet.entrySet()) {
            if (entry.getValue() != null) {
                setVariable(entry.getKey(), entry.getValue());
            } else {
                setVariable(entry.getKey(), "");
            }
        }
    }

    /**
     * Replaces variables and functions inside a map with respective values and returns a new
     * map representation.
     *
     * @param map optionally having variable entries.
     * @return the constructed map without variable entries.
     */
    public <T> Map<String, T> resolveDynamicValuesInMap(final Map<String, T> map) {
        Map<String, T> target = new LinkedHashMap<>(map.size());

        for (Entry<String, T> entry : map.entrySet()) {
            final KeyValue<String, T> adaptedEntry = resolveDynamicContent(entry.getKey(), entry.getValue());
            target.put(adaptedEntry.getKey(), adaptedEntry.getValue());
        }
        return target;
    }

    /**
     * Resolves any dynamic content in the supplied key, value pair
     *
     * @param key   a key, optionally containing dynamic content
     * @param value a value, optionally containing dynamic content
     * @param <K>
     * @param <V>
     * @return a tuple containing a copy of the {@code key} and {@code value} with dynamic content replaced
     */
    private <K, V> KeyValue<K, V> resolveDynamicContent(K key, V value) {
        final K adaptedKey = resolveDynamicContentIfRequired(key);
        final V adaptedValue = resolveDynamicContentIfRequired(value);
        return KeyValue.with(adaptedKey, adaptedValue);
    }

    /**
     * Replaces variables and functions in a list with respective values and
     * returns the new list representation.
     *
     * @param list having optional variable entries.
     * @return the constructed list without variable entries.
     */
    public <T> List<T> resolveDynamicValuesInList(final List<T> list) {
        List<T> variableFreeList = new ArrayList<>(list.size());

        for (T value : list) {
            if (value instanceof String) {
                //add new value after check if it is variable or function
                variableFreeList.add((T) replaceDynamicContentInString((String) value));
            }
        }
        return variableFreeList;
    }

    /**
     * Checks for and resolves the dynamic content in the the supplied {@code value}.
     *
     * @param value the value, optionally containing dynamic content
     * @param <V>
     * @return the original value or the value with the resolved dynamic content
     */
    private <V> V resolveDynamicContentIfRequired(V value) {
        final V adaptedValue;
        if (value instanceof String) {
            adaptedValue = (V) replaceDynamicContentInString((String) value);
        } else {
            adaptedValue = value;
        }
        return adaptedValue;
    }

    /**
     * Replaces variables and functions in array with respective values and
     * returns the new array representation.
     *
     * @param array having optional variable entries.
     * @return the constructed list without variable entries.
     */
    public <T> T[] resolveDynamicValuesInArray(final T[] array) {
        return resolveDynamicValuesInList(Arrays.asList(array)).toArray(Arrays.copyOf(array, array.length));
    }

    /**
     * Clears variables in this test context. Initially adds all global variables.
     */
    public void clear() {
        variables.clear();
        variables.putAll(globalVariables.getVariables());
    }

    /**
     * Checks if variables are present right now.
     *
     * @return boolean flag to mark existence
     */
    public boolean hasVariables() {
        return !CollectionUtils.isEmpty(variables);
    }

    /**
     * Method replacing variable declarations and place holders as well as
     * function expressions in a string
     *
     * @param str the string to parse.
     * @return resulting string without any variable place holders.
     */
    public String replaceDynamicContentInString(String str) {
        return replaceDynamicContentInString(str, false);
    }

    /**
     * Method replacing variable declarations and functions in a string, optionally
     * the variable values get surrounded with single quotes.
     *
     * @param str           the string to parse for variable place holders.
     * @param enableQuoting flag marking surrounding quotes should be added or not.
     * @return resulting string without any variable place holders.
     */
    public String replaceDynamicContentInString(final String str, boolean enableQuoting) {
        String result = null;

        if (str != null) {
            result = VariableUtils.replaceVariablesInString(str, this, enableQuoting);
            result = FunctionUtils.replaceFunctionsInString(result, this, enableQuoting);
        }

        return result;
    }

    /**
     * Checks weather the given expression is a variable or function and resolves the value
     * accordingly
     *
     * @param expression the expression to resolve
     * @return the resolved expression value
     */
    public String resolveDynamicValue(String expression) {
        if (VariableUtils.isVariableName(expression)) {
            return getVariable(expression);
        } else if (functionRegistry.isFunction(expression)) {
            return FunctionUtils.resolveFunction(expression, this);
        }
        return expression;
    }

    /**
     * Handles error creating a new CitrusRuntimeException and
     * informs test listeners.
     *
     * @param testName
     * @param packageName
     * @param message
     * @param cause
     * @return
     */
    public CitrusRuntimeException handleError(String testName, String packageName, String message, Exception cause) {
        // Create empty dummy test case for logging purpose
        TestCase dummyTest = new TestCase();
        dummyTest.setName(testName);
        dummyTest.setPackageName(packageName);

        CitrusRuntimeException exception = new CitrusRuntimeException(message, cause);

        // inform test listeners with failed test
        testListeners.onTestStart(dummyTest);
        testListeners.onTestFailure(dummyTest, exception);
        testListeners.onTestFinish(dummyTest);

        return exception;
    }

    /**
     * Setter for test variables in this context.
     *
     * @param variables
     */
    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    /**
     * Getter for test variables in this context.
     *
     * @return test variables for this test context.
     */
    public Map<String, Object> getVariables() {
        return variables;
    }

    /**
     * Copies the passed {@code globalVariables} and adds them to the test context.
     * <br/>If any of the copied global variables contain dynamic content (references to other global variables or
     * functions) then this is resolved now. As a result it is important {@link #setFunctionRegistry(FunctionRegistry)}
     * is called first before calling this method.
     *
     * @param globalVariables
     */
    public void setGlobalVariables(GlobalVariables globalVariables) {
        this.globalVariables = new GlobalVariables();
        for (Entry<String, Object> entry : globalVariables.getVariables().entrySet()) {
            final KeyValue<String, Object> adaptedEntry = resolveDynamicContent(entry.getKey(), entry.getValue());
            variables.put(adaptedEntry.getKey(), adaptedEntry.getValue());
            this.globalVariables.getVariables().put(adaptedEntry.getKey(), adaptedEntry.getValue());
        }
    }

    /**
     * Set global variables.
     *
     * @return the globalVariables
     */
    public Map<String, Object> getGlobalVariables() {
        return globalVariables.getVariables();
    }

    /**
     * Sets the messageStore property.
     *
     * @param messageStore
     */
    public void setMessageStore(MessageStore messageStore) {
        this.messageStore = messageStore;
    }

    /**
     * Gets the value of the messageStore property.
     *
     * @return the messageStore
     */
    public MessageStore getMessageStore() {
        return messageStore;
    }

    /**
     * Get the current function registry.
     *
     * @return the functionRegistry
     */
    public FunctionRegistry getFunctionRegistry() {
        return functionRegistry;
    }

    /**
     * Set the function registry.
     *
     * @param functionRegistry the functionRegistry to set
     */
    public void setFunctionRegistry(FunctionRegistry functionRegistry) {
        this.functionRegistry = functionRegistry;
    }

    /**
     * Set the message validator registry.
     *
     * @param messageValidatorRegistry the messageValidatorRegistry to set
     */
    public void setMessageValidatorRegistry(MessageValidatorRegistry messageValidatorRegistry) {
        this.messageValidatorRegistry = messageValidatorRegistry;
    }

    /**
     * Get the message validator registry.
     *
     * @return the messageValidatorRegistry
     */
    public MessageValidatorRegistry getMessageValidatorRegistry() {
        return messageValidatorRegistry;
    }

    /**
     * Get the current validation matcher registry
     *
     * @return
     */
    public ValidationMatcherRegistry getValidationMatcherRegistry() {
        return validationMatcherRegistry;
    }

    /**
     * Set the validation matcher registry
     *
     * @param validationMatcherRegistry
     */
    public void setValidationMatcherRegistry(ValidationMatcherRegistry validationMatcherRegistry) {
        this.validationMatcherRegistry = validationMatcherRegistry;
    }

    /**
     * Gets the message listeners.
     *
     * @return
     */
    public MessageListeners getMessageListeners() {
        return messageListeners;
    }

    /**
     * Set the message listeners.
     *
     * @param messageListeners
     */
    public void setMessageListeners(MessageListeners messageListeners) {
        this.messageListeners = messageListeners;
    }

    /**
     * Gets the test listeners.
     *
     * @return
     */
    public TestListeners getTestListeners() {
        return testListeners;
    }

    /**
     * Set the test listeners.
     *
     * @param testListeners
     */
    public void setTestListeners(TestListeners testListeners) {
        this.testListeners = testListeners;
    }

    /**
     * Gets the global message construction interceptors.
     *
     * @return
     */
    public GlobalMessageConstructionInterceptors getGlobalMessageConstructionInterceptors() {
        return globalMessageConstructionInterceptors;
    }

    /**
     * Sets the global messsage construction interceptors.
     *
     * @param messageConstructionInterceptors
     */
    public void setGlobalMessageConstructionInterceptors(GlobalMessageConstructionInterceptors messageConstructionInterceptors) {
        this.globalMessageConstructionInterceptors = messageConstructionInterceptors;
    }

    /**
     * Gets the endpoint factory.
     *
     * @return
     */
    public EndpointFactory getEndpointFactory() {
        return endpointFactory;
    }

    /**
     * Sets the endpoint factory.
     *
     * @param endpointFactory
     */
    public void setEndpointFactory(EndpointFactory endpointFactory) {
        this.endpointFactory = endpointFactory;
    }

    /**
     * Gets the value of the referenceResolver property.
     *
     * @return the referenceResolver
     */
    public ReferenceResolver getReferenceResolver() {
        return referenceResolver;
    }

    /**
     * Sets the referenceResolver property.
     *
     * @param referenceResolver
     */
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    /**
     * Sets the namespace context builder.
     *
     * @param namespaceContextBuilder
     */
    public void setNamespaceContextBuilder(NamespaceContextBuilder namespaceContextBuilder) {
        this.namespaceContextBuilder = namespaceContextBuilder;
    }

    /**
     * Gets the namespace context builder.
     *
     * @return
     */
    public NamespaceContextBuilder getNamespaceContextBuilder() {
        return namespaceContextBuilder;
    }

    /**
     * Gets the Spring bean application context.
     *
     * @return
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Sets the Spring bean application context.
     *
     * @param applicationContext
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Informs message listeners if present that inbound message was received.
     *
     * @param receivedMessage
     */
    public void onInboundMessage(Message receivedMessage) {
        if (messageListeners != null && !messageListeners.isEmpty()) {
            messageListeners.onInboundMessage(receivedMessage, this);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Received message:" + System.getProperty("line.separator") + (receivedMessage != null ? receivedMessage.toString() : ""));
            }
        }
    }

    /**
     * Informs message listeners if present that new outbound message is about to be sent.
     *
     * @param message
     */
    public void onOutboundMessage(Message message) {
        if (messageListeners != null && !messageListeners.isEmpty()) {
            messageListeners.onOutboundMessage(message, this);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Sent message:" + System.getProperty("line.separator") + message.toString());
            }
        }
    }

    /**
     * Registers a StopTimer in the test context, so that the associated timer can be stopped later on.
     *
     * @param timerId a unique timer id
     */
    public void registerTimer(String timerId, StopTimer timer) {
        if (timers.containsKey(timerId)) {
            throw new CitrusRuntimeException("Timer already registered with this id");
        }
        timers.put(timerId, timer);
    }

    /**
     * Stops the timer matching the supplied id
     *
     * @param timerId
     * @return true if time found and stopped, matching the supplied timerId
     */
    public boolean stopTimer(String timerId) {
        StopTimer timer = timers.get(timerId);
        if (timer != null) {
            timer.stopTimer();
            return true;
        }
        return false;
    }

    /**
     * Stops all timers
     */
    public void stopTimers() {
        for (String timerId : timers.keySet()) {
            stopTimer(timerId);
        }
    }

    /**
     * Add new exception to the context marking the test as failed. This
     * is usually used by actions to mark exceptions during forked operations.
     *
     * @param exception
     */
    public void addException(CitrusRuntimeException exception) {
        this.exceptions.add(exception);
    }

    /**
     * Gets the value of the exceptions property.
     *
     * @return the exceptions
     */
    public List<CitrusRuntimeException> getExceptions() {
        return exceptions;
    }

    /**
     * Gets exception collection state.
     *
     * @return
     */
    public boolean hasExceptions() {
        return !CollectionUtils.isEmpty(getExceptions());
    }

    /**
     * Checks test result success in combination with this context exception state.
     *
     * @param testResult
     * @return
     */
    public boolean isSuccess(TestResult testResult) {
        return !hasExceptions() &&
                Optional.ofNullable(testResult)
                        .map(TestResult::isSuccess)
                        .orElse(false);
    }
}
