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

package org.citrusframework.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.citrusframework.CitrusSettings;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.TestResult;
import org.citrusframework.container.AfterTest;
import org.citrusframework.container.BeforeTest;
import org.citrusframework.container.StopTimer;
import org.citrusframework.container.TestActionContainer;
import org.citrusframework.endpoint.EndpointFactory;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.VariableNullValueException;
import org.citrusframework.functions.FunctionRegistry;
import org.citrusframework.functions.FunctionUtils;
import org.citrusframework.log.LogModifier;
import org.citrusframework.message.DefaultMessageStore;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageDirection;
import org.citrusframework.message.MessageDirectionAware;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageProcessors;
import org.citrusframework.message.MessageStore;
import org.citrusframework.report.MessageListeners;
import org.citrusframework.report.TestActionListener;
import org.citrusframework.report.TestActionListenerAware;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.report.TestListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.TypeConverter;
import org.citrusframework.validation.MessageValidatorRegistry;
import org.citrusframework.validation.matcher.ValidationMatcherRegistry;
import org.citrusframework.variable.GlobalVariables;
import org.citrusframework.variable.SegmentVariableExtractorRegistry;
import org.citrusframework.variable.VariableExpressionIterator;
import org.citrusframework.variable.VariableUtils;
import org.citrusframework.xml.namespace.NamespaceContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class holding and managing test variables. The test context also provides utility methods
 * for replacing dynamic content(variables and functions) in message payloads and headers.
 */
public class TestContext implements ReferenceResolverAware, TestActionListenerAware {
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(TestContext.class);

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
     * List of test action listeners to be informed on test action events.
     */
    private TestActionListeners testActionListeners = new TestActionListeners();

    /**
     * List of actions to run before each test.
     */
    private List<BeforeTest> beforeTest = new ArrayList<>();

    /**
     * List of actions to run after each test.
     */
    private List<AfterTest> afterTest = new ArrayList<>();

    /**
     * List of message listeners to be informed on inbound and outbound message exchange
     */
    private MessageListeners messageListeners = new MessageListeners();

    /**
     * List of global message processors
     */
    private MessageProcessors messageProcessors = new MessageProcessors();

    /**
     * Central namespace context builder
     */
    private NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();

    /**
     * Timers registered in test context, that can be stopped
     */
    protected Map<String, StopTimer> timers = new ConcurrentHashMap<>();

    /**
     * List of exceptions that actions raised during execution of forked operations
     */
    private final List<CitrusRuntimeException> exceptions = new ArrayList<>();

    /**
     * Type converter.
     */
    private TypeConverter typeConverter = TypeConverter.lookupDefault();

    /**
     * Log modifier.
     */
    private LogModifier logModifier;

    /**
     * SegmentVariableExtractorRegistry
     */
    private SegmentVariableExtractorRegistry segmentVariableExtractorRegistry = new SegmentVariableExtractorRegistry();

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
        return typeConverter.convertIfNecessary(getVariableObject(variableExpression), type);
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

        if (variableName.startsWith(CitrusSettings.VARIABLE_ESCAPE) && variableName.endsWith(CitrusSettings.VARIABLE_ESCAPE)) {
            return CitrusSettings.VARIABLE_PREFIX + VariableUtils.cutOffVariablesEscaping(variableName) + CitrusSettings.VARIABLE_SUFFIX;
        } else if (variables.containsKey(variableName)) {
            return variables.get(variableName);
        } else {
            return VariableExpressionIterator.getLastExpressionValue(variableName, this, segmentVariableExtractorRegistry.getSegmentValueExtractors());
        }

    }

    /**
     * Creates a new variable in this test context with the respective value. In case variable already exists
     * variable is overwritten.
     *
     * @param variableName the name of the new variable
     * @param value        the new variable value
     * @throws CitrusRuntimeException
     */
    public void setVariable(final String variableName, Object value) {
        if (variableName == null || variableName.isBlank() || VariableUtils.cutOffVariablesPrefix(variableName).isEmpty()) {
            throw new CitrusRuntimeException("Can not create variable '" + variableName + "', please define proper variable name");
        }

        if (value == null) {
            throw new VariableNullValueException("Trying to set variable: " + VariableUtils.cutOffVariablesPrefix(variableName) + ", but variable value is null");
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Setting variable: %s with value: '%s'", VariableUtils.cutOffVariablesPrefix(variableName), value));
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
            final String adaptedKey = resolveDynamicContentIfRequired(entry.getKey());
            final T adaptedValue = resolveDynamicContentIfRequired(entry.getValue());
            target.put(adaptedKey, adaptedValue);
        }
        return target;
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
        return variables != null && !variables.isEmpty();
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
    public RuntimeException handleError(String testName, String packageName, String message, Exception cause) {
        // Create empty dummy test case for logging purpose
        TestCase dummyTest = new EmptyTestCase(testName, packageName);

        CitrusRuntimeException exception = new CitrusRuntimeException(message, cause);

        // inform test listeners with failed test
        testListeners.onTestStart(dummyTest);
        testListeners.onTestFailure(dummyTest, exception);
        testListeners.onTestFinish(dummyTest);

        return new RuntimeException(cause);
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
        GlobalVariables.Builder builder = new GlobalVariables.Builder();
        for (Entry<String, Object> entry : globalVariables.getVariables().entrySet()) {
            final String adaptedKey = resolveDynamicContentIfRequired(entry.getKey());
            final Object adaptedValue = resolveDynamicContentIfRequired(entry.getValue());
            variables.put(adaptedKey, adaptedValue);
            builder.variable(adaptedKey, adaptedValue);
        }

        this.globalVariables = builder.build();
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
     * Obtains the testActionListeners.
     * @return
     */
    public TestActionListeners getTestActionListeners() {
        return testActionListeners;
    }

    /**
     * Specifies the testActionListeners.
     * @param testActionListeners
     */
    public void setTestActionListeners(TestActionListeners testActionListeners) {
        this.testActionListeners = testActionListeners;
    }

    @Override
    public void addTestActionListener(TestActionListener listener) {
        this.testActionListeners.addTestActionListener(listener);
    }

    /**
     * Obtains the beforeTest.
     * @return
     */
    public List<BeforeTest> getBeforeTest() {
        return beforeTest;
    }

    /**
     * Specifies the beforeTest.
     * @param beforeTest
     */
    public void setBeforeTest(List<BeforeTest> beforeTest) {
        this.beforeTest = beforeTest;
    }

    /**
     * Obtains the afterTest.
     * @return
     */
    public List<AfterTest> getAfterTest() {
        return afterTest;
    }

    /**
     * Specifies the afterTest.
     * @param afterTest
     */
    public void setAfterTest(List<AfterTest> afterTest) {
        this.afterTest = afterTest;
    }

    /**
     * Obtains the segmentVariableExtractorRegistry
     * @return
     */
    public SegmentVariableExtractorRegistry getSegmentVariableExtractorRegistry() {
        return segmentVariableExtractorRegistry;
    }

    /**
     * Specifies the segmentVariableExtractorRegistry
     * @param segmentVariableExtractorRegistry
     */
    public void setSegmentVariableExtractorRegistry(SegmentVariableExtractorRegistry segmentVariableExtractorRegistry) {
        this.segmentVariableExtractorRegistry = segmentVariableExtractorRegistry;
    }

    /**
     * Gets the global message processors for given direction.
     * @return
     */
    public List<MessageProcessor> getMessageProcessors(MessageDirection direction) {
        return messageProcessors.getMessageProcessors().stream()
                .filter(processor -> {
                    MessageDirection processorDirection = MessageDirection.UNBOUND;

                    if (processor instanceof MessageDirectionAware) {
                        processorDirection = ((MessageDirectionAware) processor).getDirection();
                    }

                    return processorDirection.equals(direction)
                            || processorDirection.equals(MessageDirection.UNBOUND);
                })
                .collect(Collectors.toList());
    }

    /**
     * Gets the global message processors.
     * @return
     */
    public MessageProcessors getMessageProcessors() {
        return messageProcessors;
    }

    /**
     * Sets the global message processors.
     *
     * @param messageProcessors
     */
    public void setMessageProcessors(MessageProcessors messageProcessors) {
        this.messageProcessors = messageProcessors;
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

    @Override
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
     * Obtains the typeConverter.
     * @return
     */
    public TypeConverter getTypeConverter() {
        return typeConverter;
    }

    /**
     * Specifies the typeConverter.
     * @param typeConverter
     */
    public void setTypeConverter(TypeConverter typeConverter) {
        this.typeConverter = typeConverter;
    }

    /**
     * Gets the logModifier.
     * @return
     */
    public LogModifier getLogModifier() {
        return logModifier;
    }

    /**
     * Sets the logModifier.
     * @param logModifier
     */
    public void setLogModifier(LogModifier logModifier) {
        this.logModifier = logModifier;
    }

    /**
     * Informs message listeners if present that inbound message was received.
     *
     * @param receivedMessage
     */
    public void onInboundMessage(Message receivedMessage) {
        logMessage("Receive", receivedMessage, MessageDirection.INBOUND);
    }

    /**
     * Informs message listeners if present that new outbound message is about to be sent.
     *
     * @param message
     */
    public void onOutboundMessage(Message message) {
        logMessage("Send", message, MessageDirection.OUTBOUND);
    }

    /**
     * Informs message listeners if present that new outbound message is about to be sent.
     *
     * @param message
     */
    private void logMessage(String operation, Message message, MessageDirection direction) {
        if (messageListeners != null && !messageListeners.isEmpty()) {
            if (MessageDirection.OUTBOUND.equals(direction)) {
                messageListeners.onOutboundMessage(message, this);
            } else if (MessageDirection.INBOUND.equals(direction)) {
                messageListeners.onInboundMessage(message, this);
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug(String.format("%s message:%n%s", operation, Optional.ofNullable(message).map(Message::toString).orElse("")));
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
        return !getExceptions().isEmpty();
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

    /**
     * Empty test case implementation used as test result when tests fail before execution.
     */
    private static class EmptyTestCase implements TestCase {
        private final String testName;
        private final String packageName;

        public EmptyTestCase(String testName, String packageName) {
            this.testName = testName;
            this.packageName = packageName;
        }

        @Override
        public String getName() {
            return testName;
        }

        @Override
        public String getPackageName() {
            return packageName;
        }

        @Override
        public Map<String, Object> getVariableDefinitions() {
            return Collections.emptyMap();
        }

        @Override
        public TestActionContainer setActions(List<TestAction> actions) {
            return this;
        }

        @Override
        public List<TestAction> getActions() {
            return Collections.emptyList();
        }

        @Override
        public List<TestActionBuilder<?>> getActionBuilders() {
            return Collections.emptyList();
        }

        @Override
        public long getActionCount() {
            return 0;
        }

        @Override
        public TestActionContainer addTestActions(TestAction... action) {
            return this;
        }

        @Override
        public TestActionContainer addTestAction(TestAction action) {
            return this;
        }

        @Override
        public int getActionIndex(TestAction action) {
            return 0;
        }

        @Override
        public TestAction getActiveAction() {
            return null;
        }

        @Override
        public List<TestAction> getExecutedActions() {
            return Collections.emptyList();
        }

        @Override
        public TestAction getTestAction(int index) {
            return null;
        }

        @Override
        public String getDescription() {
            return "Empty test";
        }

        @Override
        public TestAction setDescription(String description) {
            return this;
        }

        @Override
        public boolean isDisabled(TestContext context) {
            return false;
        }

        @Override
        public TestActor getActor() {
            return null;
        }

        @Override
        public void setName(String name) {
            // do nothing
        }

        @Override
        public void execute(TestContext context) {
            // do nothing
        }

        @Override
        public void start(TestContext context) {
            // do nothing
        }

        @Override
        public void executeAction(TestAction action, TestContext context) {
            // do nothing
        }

        @Override
        public void finish(TestContext context) {
            // do nothing
        }

        @Override
        public void setTestResult(TestResult testResult) {
            // do nothing
        }

        @Override
        public TestResult getTestResult() {
            return null;
        }

        @Override
        public void setIncremental(boolean incremental) {
            // do nothing
        }

        @Override
        public void addFinalAction(TestActionBuilder<?> action) {
            // do nothing
        }

        @Override
        public void setPackageName(String packageName) {
            // do nothing
        }

        @Override
        public void setActiveAction(TestAction action) {
            // do nothing
        }

        @Override
        public void setExecutedAction(TestAction action) {
            // do nothing
        }

        @Override
        public void setTestClass(Class<?> type) {
            // do nothing
        }

        @Override
        public TestCaseMetaInfo getMetaInfo() {
            return new TestCaseMetaInfo();
        }

        @Override
        public Class<?> getTestClass() {
            return this.getClass();
        }
    }

}
