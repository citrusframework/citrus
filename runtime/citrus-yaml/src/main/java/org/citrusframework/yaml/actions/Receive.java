/*
 * Copyright the original author or authors.
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
package org.citrusframework.yaml.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.StringUtils;
import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.MessageValidationContext;
import org.citrusframework.validation.json.JsonPathMessageValidationContext;
import org.citrusframework.validation.script.DefaultScriptValidationContext;
import org.citrusframework.validation.script.ScriptValidationContextBuilder;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.citrusframework.yaml.SchemaProperty;
import org.citrusframework.yaml.actions.script.ScriptDefinitionType;

public class Receive implements TestActionBuilder<ReceiveMessageAction>, ReferenceResolverAware {

    private final ReceiveMessageAction.ReceiveMessageActionBuilder<?, ?, ?> builder;

    private String actor;
    private ReferenceResolver referenceResolver;
    private final List<String> validators = new ArrayList<>();

    private MessageValidationContext.Builder<?, ?> messageValidationContext;

    protected Message message;
    protected List<Ignore> ignore;
    protected List<Validate> validate;
    protected List<Namespace> namespace;

    public Receive() {
        this(new ReceiveMessageAction.Builder());
    }

    public Receive(ReceiveMessageAction.ReceiveMessageActionBuilder<?, ?, ?> builder) {
        this.builder = builder;
    }

    @SchemaProperty(advanced = true, description = "Test action description printed when the action is executed.")
    public void setDescription(String value) {
        builder.description(value);
    }

    public List<Ignore> getIgnore() {
        if (ignore == null) {
            ignore = new ArrayList<>();
        }
        return this.ignore;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData( key = "$comment", value = "group:ignore") },
            description = "List of expressions to identify message elements that should be ignored during validation.")
    public void setIgnore(List<Ignore> ignore) {
        this.ignore = ignore;
    }

    public List<Validate> getValidate() {
        if (validate == null) {
            validate = new ArrayList<>();
        }
        return this.validate;
    }

    @SchemaProperty(description = "Validation expressions evaluated on the message.")
    public void setValidate(List<Validate> validate) {
        this.validate = validate;
    }

    public List<Namespace> getNamespace() {
        if (namespace == null) {
            namespace = new ArrayList<>();
        }
        return this.namespace;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData( key = "$comment", value = "group:xml") },
            description = "Optional XML namespace to validate.")
    public void setNamespace(List<Namespace> namespace) {
        this.namespace = namespace;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData( key = "$comment", value = "group:validator") },
            description = "Explicit message validator.")
    public void setValidator(String validator) {
        if (StringUtils.hasText(validator)) {
            validators.add(validator);
        }
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData( key = "$comment", value = "group:validator") },
            description = "List of message validators used to validate the message.")
    public void setValidators(String messageValidatorExpression) {
        if (StringUtils.hasText(messageValidatorExpression)) {
            Stream.of(messageValidatorExpression.split(","))
                    .map(String::trim)
                    .forEach(validators::add);
        }
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData( key = "$comment", value = "group:validator") },
            description = "Explicit message header validator.")
    public void setHeaderValidator(String headerValidator) {
        if (StringUtils.hasText(headerValidator)) {
            builder.getHeaderValidationContext().validator(headerValidator);
        }
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData( key = "$comment", value = "group:validator") },
            description = "List of message header validators used to validate the message.")
    public void setHeaderValidators(String headerValidatorExpression) {
        if (StringUtils.hasText(headerValidatorExpression)) {
            Stream.of(headerValidatorExpression.split(","))
                    .map(String::trim)
                    .forEach(builder.getHeaderValidationContext()::validator);
        }
    }

    @SchemaProperty(required = true, description = "The expected message used to validate.")
    public void setMessage(Message message) {
        this.message = message;
        MessageSupport.configureMessage(builder, message);
    }

    private void addScriptValidationContext() {
        ScriptValidationContextBuilder<?, ?> context = null;

        for (Validate validateElement : getValidate()) {
            ScriptDefinitionType scriptElement = validateElement.getScript();

            // check for nested validate script child node
            if (scriptElement != null) {
                if (context != null) {
                    throw new CitrusRuntimeException("Found multiple validation script definitions - " +
                            "only supporting a single validation script for message validation");
                }

                String type = scriptElement.getType();
                context = new DefaultScriptValidationContext.Builder().scriptType(type);

                String filePath = scriptElement.getFile();
                if (StringUtils.hasText(filePath)) {
                    context.scriptResource(filePath);
                    if (scriptElement.getCharset() != null) {
                        context.scriptResourceCharset(scriptElement.getCharset());
                    }
                } else if (scriptElement.getValue() != null) {
                    context.script(scriptElement.getValue().trim());
                }
                builder.validate(context);
            }
        }
    }

    private void addJsonValidationContext() {
        //check for validate elements, these elements can either have script, jsonPath or namespace validation information
        //for now we only handle jsonPath validation
        Map<String, Object> validateJsonPathExpressions = new HashMap<>();
        for (Validate validateElement : getValidate()) {
            extractJsonPathValidateExpressions(validateElement, validateJsonPathExpressions);
        }

        if (!validateJsonPathExpressions.isEmpty()) {
            JsonPathMessageValidationContext.Builder jsonPathContext = new JsonPathMessageValidationContext.Builder();
            jsonPathContext.expressions(validateJsonPathExpressions);
            builder.validate(jsonPathContext);
        }
    }

    private void extractJsonPathValidateExpressions(Validate validateElement, Map<String, Object> validateJsonPathExpressions) {
        //check for jsonPath validation - old style with direct attribute
        String pathExpression = validateElement.path;
        if (JsonPathMessageValidationContext.isJsonPathExpression(pathExpression)) {
            validateJsonPathExpressions.put(pathExpression, validateElement.value);
        }

        //check for jsonPath validation elements - new style preferred
        for (Validate.JsonPath jsonPathElement : validateElement.getJsonPath()) {
            String expression = jsonPathElement.expression;
            if (StringUtils.hasText(expression)) {
                validateJsonPathExpressions.put(expression, jsonPathElement.value);
            }
        }
    }

    private void addMessageValidationContext() {
        getIgnore().stream()
                .map(Ignore::getPath)
                .forEach(ignorePath -> getMessageValidationContext().ignore(ignorePath));

        if (message != null) {
            if (message.schema != null || message.schemaRepository != null) {
                getMessageValidationContext()
                        .schemaValidation(message.isSchemaValidation())
                        .schema(message.schema)
                        .schemaRepository(message.schemaRepository);
            } else if (message.isSchemaValidation() != null && !message.isSchemaValidation()) {
                getMessageValidationContext().schemaValidation(message.isSchemaValidation());
            }
        }
    }

    private MessageValidationContext.Builder<?, ?> getMessageValidationContext() {
        if (messageValidationContext == null) {
            messageValidationContext = new DefaultMessageValidationContext.Builder();
        }

        return messageValidationContext;
    }

    private void addXmlValidationContext() {
        //check for validate elements, these elements can either have script, xpath or namespace validation information
        //for now we only handle namespace validation
        Map<String, String> validateNamespaces = new HashMap<>();
        for (Validate validateElement : getValidate()) {
            //check for namespace validation elements
            for (Validate.Namespace namespaceElement : validateElement.getNamespace()) {
                validateNamespaces.put(namespaceElement.prefix, namespaceElement.value);
            }
        }
        if (!validateNamespaces.isEmpty()) {
            getXmlValidationContext().namespaces(validateNamespaces);
        }

        //Catch namespace declarations for namespace context
        Map<String, String> namespaces = new HashMap<>();
        for (Namespace namespace : getNamespace()) {
            namespaces.put(namespace.prefix, namespace.value);
        }
        if (!namespaces.isEmpty()) {
            getXmlValidationContext().namespaceContext(namespaces);
        }

        //check for validate elements, these elements can either have script, xpath or namespace validation information
        //for now we only handle xpath validation
        Map<String, Object> validateXpathExpressions = new HashMap<>();
        for (Validate validateElement : getValidate()) {
            extractXPathValidateExpressions(validateElement, validateXpathExpressions);
        }

        if (!validateXpathExpressions.isEmpty()) {
            messageValidationContext = getXmlValidationContext().xpath().expressions(validateXpathExpressions);
        }
    }

    private XmlMessageValidationContext.Builder getXmlValidationContext() {
        if (messageValidationContext == null) {
            XmlMessageValidationContext.Builder builder = new XmlMessageValidationContext.Builder();
            messageValidationContext = builder;
            return builder;
        } else if (messageValidationContext instanceof XmlMessageValidationContext.Builder xmlValidationContext) {
            return xmlValidationContext;
        } else {
            XmlMessageValidationContext.Builder builder = XmlMessageValidationContext.Builder.adapt(messageValidationContext);
            messageValidationContext = builder;
            return builder;
        }
    }

    private void extractXPathValidateExpressions(Validate validateElement, Map<String, Object> validateXpathExpressions) {
        //check for xpath validation - old style with direct attribute
        String pathExpression = validateElement.path;
        if (StringUtils.hasText(pathExpression) && !JsonPathMessageValidationContext.isJsonPathExpression(pathExpression)) {
            //construct pathExpression with explicit result-type, like boolean:/TestMessage/Value
            if (validateElement.resultType != null) {
                pathExpression = validateElement.resultType + ":" + pathExpression;
            }

            validateXpathExpressions.put(pathExpression, validateElement.value);
        }

        //check for xpath validation elements - new style preferred
        for (Validate.Xpath xpathElement : validateElement.getXpath()) {
            String expression = xpathElement.expression;
            if (StringUtils.hasText(expression)) {
                //construct expression with explicit result-type, like boolean:/TestMessage/Value
                if (xpathElement.resultType != null) {
                    expression = xpathElement.resultType + ":" + expression;
                }

                validateXpathExpressions.put(expression, xpathElement.value);
            }
        }
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData( key = "$comment", value = "group:extract") },
            description = "Extract message content to test variables after the message validation.")
    public void setExtract(Message.Extract value) {
        MessageSupport.configureExtract(builder, value);
    }

    @SchemaProperty(required = true, description = "The message endpoint name or URI used to receive the message.")
    public void setEndpoint(String value) {
        builder.endpoint(value);
    }

    @SchemaProperty(advanced = true)
    public void setActor(String value) {
        this.actor = value;
    }

    @SchemaProperty(description = "Receive timeout.")
    public void setTimeout(Integer value) {
        builder.timeout(value);
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData( key = "$comment", value = "group:selector") },
            description = "Message select expression to selectively receive a message.")
    public void setSelect(String value) {
        builder.selector(value);
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData( key = "$comment", value = "group:selector") },
            description = "Message selector used to selectively receive a message.")
    public void setSelector(Selector selector) {
        if (selector.value != null) {
            builder.selector(selector.value);
        }

        if (selector.element != null) {
            Map<String, String> selectorElements = new HashMap<>();
            for (Selector.Element element : selector.element) {
                selectorElements.put(element.name, element.value);
            }

            builder.selector(selectorElements);
        }
    }

    @Override
    public ReceiveMessageAction build() {
        if (referenceResolver != null) {
            if (actor != null) {
                builder.actor(referenceResolver.resolve(actor, TestActor.class));
            }
        }

        validators.forEach(builder::validator);

        addXmlValidationContext();
        addJsonValidationContext();
        addMessageValidationContext();
        addScriptValidationContext();

        if (messageValidationContext != null) {
            builder.validate(messageValidationContext);
        }

        return doBuild();
    }

    /**
     * Subclasses may add additional building logic here.
     */
    protected ReceiveMessageAction doBuild() {
        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        builder.setReferenceResolver(referenceResolver);
        this.referenceResolver = referenceResolver;
    }

    public static class Selector {
        protected List<Element> element;
        protected String value;

        public List<Element> getElement() {
            if (element == null) {
                element = new ArrayList<>();
            }
            return this.element;
        }

        @SchemaProperty(advanced = true, description = "List of elements to build the selector expression.")
        public void setElement(List<Element> element) {
            this.element = element;
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty(description = "Selector expression value.")
        public void setValue(String value) {
            this.value = value;
        }

        public static class Element {
            protected String name;
            protected String value;

            public String getName() {
                return name;
            }

            @SchemaProperty(required = true, description = "Select element key name.")
            public void setName(String value) {
                this.name = value;
            }

            public String getValue() {
                return value;
            }

            @SchemaProperty(required = true, description = "Select expression value.")
            public void setValue(String value) {
                this.value = value;
            }
        }
    }

    public static class Ignore {
        protected String path;

        public String getPath() {
            return path;
        }

        @SchemaProperty(required = true, description = "Path expression identifies the message element to ignore.")
        public void setPath(String value) {
            this.path = value;
        }
    }

    public static class Namespace {
        protected String prefix;
        protected String value;

        public String getPrefix() {
            return prefix;
        }

        @SchemaProperty(required = true, description = "Expected namespace prefix.")
        public void setPrefix(String value) {
            this.prefix = value;
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty(required = true, description = "Expected namespace value.")
        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class Validate {
        protected ScriptDefinitionType script;
        protected List<Xpath> xpath;
        protected List<JsonPath> jsonPath;
        protected List<Namespace> namespace;
        protected String path;
        protected String value;
        protected String resultType;

        public ScriptDefinitionType getScript() {
            return script;
        }

        @SchemaProperty(
                metadata = { @SchemaProperty.MetaData( key = "$comment", value = "group:script") },
                description = "Script that builds the expected validate value.")
        public void setScript(ScriptDefinitionType value) {
            this.script = value;
        }

        public List<Xpath> getXpath() {
            if (xpath == null) {
                xpath = new ArrayList<>();
            }
            return this.xpath;
        }

        @SchemaProperty(
                metadata = { @SchemaProperty.MetaData( key = "$comment", value = "group:xml") },
                description = "Xpath validation expression.")
        public void setXpath(List<Xpath> xpath) {
            this.xpath = xpath;
        }

        public List<JsonPath> getJsonPath() {
            if (jsonPath == null) {
                jsonPath = new ArrayList<>();
            }
            return this.jsonPath;
        }

        @SchemaProperty(
                metadata = { @SchemaProperty.MetaData( key = "$comment", value = "group:jsonPath") },
                description = "JsonPath validation expression.")
        public void setJsonPath(List<JsonPath> jsonPath) {
            this.jsonPath = jsonPath;
        }

        public List<Namespace> getNamespace() {
            if (namespace == null) {
                namespace = new ArrayList<>();
            }
            return this.namespace;
        }

        @SchemaProperty(
                metadata = { @SchemaProperty.MetaData( key = "$comment", value = "group:xml") },
                description = "List of expected namespaces.")
        public void setNamespace(List<Namespace> namespace) {
            this.namespace = namespace;
        }

        public String getPath() {
            return path;
        }

        @SchemaProperty(description = "Message validation expression.")
        public void setPath(String value) {
            this.path = value;
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty(description = "Expected message validation expression value.")
        public void setValue(String value) {
            this.value = value;
        }

        public String getResultType() {
            return resultType;
        }

        @SchemaProperty(advanced = true, description = "Expected expression result type.")
        public void setResultType(String value) {
            this.resultType = value;
        }

        public static class JsonPath {
            protected String expression;
            protected String value;

            public String getExpression() {
                return expression;
            }

            @SchemaProperty(required = true, description = "The Json path expression to evaluate.")
            public void setExpression(String value) {
                this.expression = value;
            }

            public String getValue() {
                return value;
            }

            @SchemaProperty(required = true, description = "The expected result.")
            public void setValue(String value) {
                this.value = value;
            }
        }

        public static class Namespace {
            protected String prefix;
            protected String value;

            public String getPrefix() {
                return prefix;
            }

            @SchemaProperty(required = true, description = "The expected namespace prefix.")
            public void setPrefix(String value) {
                this.prefix = value;
            }

            public String getValue() {
                return value;
            }

            @SchemaProperty(required = true, description = "The expected namespace value.")
            public void setValue(String value) {
                this.value = value;
            }
        }

        public static class Xpath {
            protected String expression;
            protected String value;
            protected String resultType;

            public String getExpression() {
                return expression;
            }

            @SchemaProperty(required = true, description = "The Xpath expression to evaluate.")
            public void setExpression(String value) {
                this.expression = value;
            }

            public String getValue() {
                return value;
            }

            @SchemaProperty(required = true, description = "The expected result.")
            public void setValue(String value) {
                this.value = value;
            }

            public String getResultType() {
                return resultType;
            }

            @SchemaProperty(advanced = true, description = "The expected result type.")
            public void setResultType(String value) {
                this.resultType = value;
            }
        }
    }
}
