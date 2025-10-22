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
package org.citrusframework.xml.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
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
import org.citrusframework.xml.actions.script.ScriptDefinitionType;

@XmlRootElement(name = "receive")
public class Receive implements TestActionBuilder<ReceiveMessageAction>, ReferenceResolverAware {

    private final ReceiveMessageAction.ReceiveMessageActionBuilder<?, ?, ?> builder;

    private String actor;
    private ReferenceResolver referenceResolver;
    private final List<String> validators = new ArrayList<>();

    private MessageValidationContext.Builder<?, ?> messageValidationContext;

    protected Message message;
    protected List<Ignore> ignores;
    protected List<Validate> validates;
    protected List<Namespace> namespaces;

    public Receive() {
        this(new ReceiveMessageAction.Builder());
    }

    public Receive(ReceiveMessageAction.ReceiveMessageActionBuilder<?, ?, ?> builder) {
        this.builder = builder;
    }

    @XmlElement
    public void setDescription(String value) {
        builder.description(value);
    }

    public List<Ignore> getIgnores() {
        if (ignores == null) {
            ignores = new ArrayList<>();
        }
        return this.ignores;
    }

    @XmlElement(name = "ignore")
    public void setIgnores(List<Ignore> ignores) {
        this.ignores = ignores;
    }

    public List<Validate> getValidates() {
        if (validates == null) {
            validates = new ArrayList<>();
        }
        return this.validates;
    }

    @XmlElement(name = "validate")
    public void setValidates(List<Validate> validates) {
        this.validates = validates;
    }

    public List<Namespace> getNamespaces() {
        if (namespaces == null) {
            namespaces = new ArrayList<>();
        }
        return this.namespaces;
    }

    @XmlElement(name = "namespace")
    public void setNamespaces(List<Namespace> namespaces) {
        this.namespaces = namespaces;
    }

    @XmlAttribute
    public void setValidator(String validator) {
        if (StringUtils.hasText(validator)) {
            validators.add(validator);
        }

    }

    @XmlAttribute
    public void setValidators(String messageValidatorExpression) {
        if (StringUtils.hasText(messageValidatorExpression)) {
            Stream.of(messageValidatorExpression.split(","))
                    .map(String::trim)
                    .forEach(validators::add);
        }
    }

    @XmlAttribute(name = "header-validator")
    public void setHeaderValidator(String headerValidator) {
        if (StringUtils.hasText(headerValidator)) {
            builder.getHeaderValidationContext().validator(headerValidator);
        }
    }

    @XmlAttribute(name = "header-validators")
    public void setHeaderValidators(String headerValidatorExpression) {
        if (StringUtils.hasText(headerValidatorExpression)) {
            Stream.of(headerValidatorExpression.split(","))
                    .map(String::trim)
                    .forEach(builder.getHeaderValidationContext()::validator);
        }
    }

    @XmlElement(required = true)
    public void setMessage(Message message) {
        this.message = message;
        MessageSupport.configureMessage(builder, message);
    }

    private void addScriptValidationContext() {
        ScriptValidationContextBuilder<?, ?> context = null;

        for (Validate validateElement : getValidates()) {
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
        for (Validate validateElement : getValidates()) {
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
        for (Validate.JsonPath jsonPathElement : validateElement.getJsonPaths()) {
            String expression = jsonPathElement.expression;
            if (StringUtils.hasText(expression)) {
                validateJsonPathExpressions.put(expression, jsonPathElement.value);
            }
        }
    }

    private void addMessageValidationContext() {
        getIgnores().stream()
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
        for (Validate validateElement : getValidates()) {
            //check for namespace validation elements
            for (Validate.Namespace namespaceElement : validateElement.getNamespaces()) {
                validateNamespaces.put(namespaceElement.prefix, namespaceElement.value);
            }
        }
        if (!validateNamespaces.isEmpty()) {
            getXmlValidationContext().namespaces(validateNamespaces);
        }

        //Catch namespace declarations for namespace context
        Map<String, String> namespaces = new HashMap<>();
        for (Namespace namespace : getNamespaces()) {
            namespaces.put(namespace.prefix, namespace.value);
        }
        if (!namespaces.isEmpty()) {
            getXmlValidationContext().namespaceContext(namespaces);
        }

        //check for validate elements, these elements can either have script, xpath or namespace validation information
        //for now we only handle xpath validation
        Map<String, Object> validateXpathExpressions = new HashMap<>();
        for (Validate validateElement : getValidates()) {
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
        for (Validate.Xpath xpathElement : validateElement.getXpaths()) {
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

    @XmlElement
    public void setExtract(Message.Extract value) {
        MessageSupport.configureExtract(builder, value);
    }

    @XmlAttribute
    public void setEndpoint(String value) {
        builder.endpoint(value);
    }

    @XmlAttribute
    public void setActor(String value) {
        this.actor = value;
    }

    @XmlAttribute
    public void setTimeout(Integer value) {
        builder.timeout(value);
    }

    @XmlAttribute
    public void setSelect(String value) {
        builder.selector(value);
    }

    @XmlElement
    public void setSelector(Selector selector) {
        if (selector.selectorValue != null) {
            builder.selector(selector.selectorValue);
        }

        if (selector.elements != null) {
            Map<String, String> selectorElements = new HashMap<>();
            for (Selector.Element element : selector.elements) {
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
     * @return
     */
    protected ReceiveMessageAction doBuild() {
        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        builder.setReferenceResolver(referenceResolver);
        this.referenceResolver = referenceResolver;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "elements",
            "selectorValue"
    })
    public static class Selector {
        @XmlElement(name = "element")
        protected List<Element> elements;
        @XmlElement(name = "value")
        protected String selectorValue;

        public List<Element> getElements() {
            if (elements == null) {
                elements = new ArrayList<>();
            }
            return this.elements;
        }

        public String getSelectorValue() {
            return selectorValue;
        }

        public void setSelectorValue(String value) {
            this.selectorValue = value;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Element {
            @XmlAttribute(required = true)
            protected String name;
            @XmlAttribute(required = true)
            protected String value;

            public String getName() {
                return name;
            }

            public void setName(String value) {
                this.name = value;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Ignore {
        @XmlAttribute(required = true)
        protected String path;

        public String getPath() {
            return path;
        }

        public void setPath(String value) {
            this.path = value;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Namespace {
        @XmlAttribute(required = true)
        protected String prefix;
        @XmlAttribute(required = true)
        protected String value;

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String value) {
            this.prefix = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "script",
            "xpaths",
            "jsonPaths",
            "namespaces"
    })
    public static class Validate {
        protected ScriptDefinitionType script;
        @XmlElement(name = "xpath")
        protected List<Xpath> xpaths;
        @XmlElement(name = "json-path")
        protected List<JsonPath> jsonPaths;
        @XmlElement(name = "namespace")
        protected List<Namespace> namespaces;
        @XmlAttribute
        protected String path;
        @XmlAttribute
        protected String value;
        @XmlAttribute(name = "result-type")
        protected String resultType;

        public ScriptDefinitionType getScript() {
            return script;
        }

        public void setScript(ScriptDefinitionType value) {
            this.script = value;
        }

        public List<Xpath> getXpaths() {
            if (xpaths == null) {
                xpaths = new ArrayList<>();
            }
            return this.xpaths;
        }

        public void setXpaths(List<Xpath> xpaths) {
            this.xpaths = xpaths;
        }

        public List<JsonPath> getJsonPaths() {
            if (jsonPaths == null) {
                jsonPaths = new ArrayList<>();
            }
            return this.jsonPaths;
        }

        public void setJsonPaths(List<JsonPath> jsonPaths) {
            this.jsonPaths = jsonPaths;
        }

        public List<Namespace> getNamespaces() {
            if (namespaces == null) {
                namespaces = new ArrayList<>();
            }
            return this.namespaces;
        }

        public void setNamespaces(List<Namespace> namespaces) {
            this.namespaces = namespaces;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String value) {
            this.path = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getResultType() {
            return resultType;
        }

        public void setResultType(String value) {
            this.resultType = value;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class JsonPath {
            @XmlAttribute(required = true)
            protected String expression;
            @XmlAttribute(required = true)
            protected String value;

            public String getExpression() {
                return expression;
            }

            public void setExpression(String value) {
                this.expression = value;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Namespace {
            @XmlAttribute(required = true)
            protected String prefix;
            @XmlAttribute(required = true)
            protected String value;

            public String getPrefix() {
                return prefix;
            }

            public void setPrefix(String value) {
                this.prefix = value;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Xpath {
            @XmlAttribute(required = true)
            protected String expression;
            @XmlAttribute(required = true)
            protected String value;
            @XmlAttribute(name = "result-type")
            protected String resultType;

            public String getExpression() {
                return expression;
            }

            public void setExpression(String value) {
                this.expression = value;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }

            public String getResultType() {
                return resultType;
            }

            public void setResultType(String value) {
                this.resultType = value;
            }
        }
    }
}
