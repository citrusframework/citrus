/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.consol.citrus.xml.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.TestActor;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DelegatingPathExpressionProcessor;
import com.consol.citrus.message.MessageHeaderType;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.message.ScriptPayloadBuilder;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.validation.context.HeaderValidationContext;
import com.consol.citrus.validation.interceptor.BinaryMessageProcessor;
import com.consol.citrus.validation.interceptor.GzipMessageProcessor;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import com.consol.citrus.validation.json.JsonPathMessageValidationContext;
import com.consol.citrus.validation.script.ScriptValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.xml.actions.script.ScriptDefinitionType;
import com.consol.citrus.xml.util.PayloadElementParser;
import org.springframework.util.StringUtils;

import static com.consol.citrus.dsl.MessageSupport.MessageBodySupport.fromBody;
import static com.consol.citrus.dsl.MessageSupport.MessageHeaderSupport.fromHeaders;

@XmlRootElement(name = "receive")
public class Receive implements TestActionBuilder<ReceiveMessageAction>, ReferenceResolverAware {

    private final ReceiveMessageAction.Builder builder = new ReceiveMessageAction.Builder();

    private String actor;
    private final List<String> validators = new ArrayList<>();
    private ReferenceResolver referenceResolver;

    @XmlElement
    public Receive setDescription(String value) {
        builder.description(value);
        return this;
    }

    @XmlElement(required = true)
    public Receive setMessage(Message message) {
        if (message.type != null) {
            if (StringUtils.hasText(message.type)) {
                if (message.type.equalsIgnoreCase(MessageType.GZIP.name())) {
                    builder.process(new GzipMessageProcessor());
                }

                if (message.type.equalsIgnoreCase(MessageType.BINARY.name())) {
                    builder.process(new BinaryMessageProcessor());
                }
            }

            builder.message().type(message.type);
        }

        if (message.body != null) {
            if (message.body.data != null) {
                builder.message().body(message.body.data.trim());
            }

            if (message.body.resource != null) {
                if (message.body.resource.charset != null) {
                    builder.message().body(FileUtils.getFileResource(message.body.resource.file + FileUtils.FILE_PATH_CHARSET_PARAMETER + message.body.resource.charset));
                } else {
                    builder.message().body(FileUtils.getFileResource(message.body.resource.file));
                }
            }

            if (message.body.payload != null && !message.body.payload.getAnies().isEmpty()) {
                builder.message().body(PayloadElementParser.parseMessagePayload(message.body.payload.anies.get(0)));
            }
        }

        handleScriptPayloadBuilder(message);

        if (message.name != null) {
            builder.message().name(message.name);
        }

        Map<String, Object> pathExpressions = new HashMap<>();
        for (Message.Expression expression : message.getExpressions()) {
            String pathExpression = expression.path;
            pathExpressions.put(pathExpression, expression.value);
        }

        if (!pathExpressions.isEmpty()) {
            builder.message().process(new DelegatingPathExpressionProcessor(pathExpressions));
        }

        if (message.dataDictionary != null) {
            builder.message().dictionary(message.dataDictionary);
        }

        Headers headers = message.getHeaders();
        if (headers != null) {
            headers.getHeaders().forEach(header -> {
                Object headerValue;
                if (StringUtils.hasText(header.type)) {
                    headerValue = MessageHeaderType.createTypedValue(header.type, header.value);
                } else {
                    headerValue = header.value;
                }

                builder.message().header(header.name, headerValue);
            });
            headers.getValues().forEach(builder.message()::header);

            headers.getFragments()
                    .stream()
                    .filter(fragment -> !fragment.getAnies().isEmpty())
                    .forEach(fragment -> builder.message().header(PayloadElementParser.parseMessagePayload(fragment.anies.get(0))));

            headers.getResources().forEach(resource -> {
                if (resource.charset != null) {
                    builder.message().header(FileUtils.getFileResource(resource.file + FileUtils.FILE_PATH_CHARSET_PARAMETER + resource.charset));
                } else {
                    builder.message().header(FileUtils.getFileResource(resource.file));
                }
            });
        }

        addValidationContexts(message);

        return this;
    }

    private void addValidationContexts(Message message) {
        HeaderValidationContext headerValidationContext = new HeaderValidationContext();

        String headerValidator = message.headerValidator;
        if (StringUtils.hasText(headerValidator)) {
            headerValidationContext.addHeaderValidator(headerValidator);
        }

        String headerValidatorExpression = message.headerValidators;
        if (StringUtils.hasText(headerValidatorExpression)) {
            Stream.of(headerValidatorExpression.split(","))
                    .map(String::trim)
                    .forEach(headerValidationContext::addHeaderValidator);
        }
        builder.message().validate(headerValidationContext);

        addXmlValidationContext(message);
        addJsonValidationContext(message);
        addScriptValidationContext(message);

        if (StringUtils.hasText(message.validator)) {
            validators.add(message.validator);
        }

        String messageValidatorExpression = message.validators;
        if (StringUtils.hasText(messageValidatorExpression)) {
            Stream.of(messageValidatorExpression.split(","))
                    .map(String::trim)
                    .forEach(validators::add);
        }

        validators.forEach(builder::validator);
    }

    private void addScriptValidationContext(Message message) {
        ScriptValidationContext.Builder context = null;

        for (Message.Validate validateElement : message.getValidates()) {
            ScriptDefinitionType scriptElement = validateElement.getScript();

            // check for nested validate script child node
            if (scriptElement != null) {
                if (context != null) {
                    throw new CitrusRuntimeException("Found multiple validation script definitions - " +
                            "only supporting a single validation script for message validation");
                }

                String type = scriptElement.getType();
                context = new ScriptValidationContext.Builder().scriptType(type);

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

    private void addJsonValidationContext(Message message) {
        JsonMessageValidationContext.Builder context = new JsonMessageValidationContext.Builder();

        if (message.schema != null || message.schemaRepository != null) {
            context
                    .schemaValidation(message.isSchemaValidation())
                    .schema(message.schema)
                    .schemaRepository(message.schemaRepository);
        } else if (message.isSchemaValidation() != null && !message.isSchemaValidation()) {
            context.schemaValidation(message.isSchemaValidation());
        }

        Set<String> ignoreExpressions = new HashSet<>();
        for (Message.Ignore ignoreValue : message.getIgnores()) {
            ignoreExpressions.add(ignoreValue.path);
        }
        ignoreExpressions.forEach(context::ignore);
        builder.validate(context);

        JsonPathMessageValidationContext.Builder jsonPathContext = new JsonPathMessageValidationContext.Builder();

        //check for validate elements, these elements can either have script, jsonPath or namespace validation information
        //for now we only handle jsonPath validation
        Map<String, Object> validateJsonPathExpressions = new HashMap<>();
        for (Message.Validate validateElement : message.getValidates()) {
            extractJsonPathValidateExpressions(validateElement, validateJsonPathExpressions);
        }

        if (!validateJsonPathExpressions.isEmpty()) {
            jsonPathContext.expressions(validateJsonPathExpressions);
            builder.validate(jsonPathContext);
        }
    }

    private void extractJsonPathValidateExpressions(Message.Validate validateElement, Map<String, Object> validateJsonPathExpressions) {
        //check for jsonPath validation - old style with direct attribute
        String pathExpression = validateElement.path;
        if (JsonPathMessageValidationContext.isJsonPathExpression(pathExpression)) {
            validateJsonPathExpressions.put(pathExpression, validateElement.value);
        }

        //check for jsonPath validation elements - new style preferred
        for (Message.Validate.JsonPath jsonPathElement : validateElement.getJsonPaths()) {
            String expression = jsonPathElement.expression;
            if (StringUtils.hasText(expression)) {
                validateJsonPathExpressions.put(expression, jsonPathElement.value);
            }
        }
    }

    private void addXmlValidationContext(Message message) {
        XmlMessageValidationContext.Builder context = new XmlMessageValidationContext.Builder();

        if (message.schema != null || message.schemaRepository != null) {
            context.schemaValidation(message.isSchemaValidation())
                .schema(message.schema)
                .schemaRepository(message.schemaRepository);
        } else if (message.isSchemaValidation() != null && !message.isSchemaValidation()) {
            context.schemaValidation(message.isSchemaValidation());
        }

        Set<String> ignoreExpressions = new HashSet<>();
        for (Message.Ignore ignore : message.getIgnores()) {
            ignoreExpressions.add(ignore.path);
        }
        ignoreExpressions.forEach(context::ignore);

        //check for validate elements, these elements can either have script, xpath or namespace validation information
        //for now we only handle namespace validation
        Map<String, String> validateNamespaces = new HashMap<>();
        for (Message.Validate validateElement : message.getValidates()) {
            //check for namespace validation elements
            for (Message.Validate.Namespace namespaceElement : validateElement.getNamespaces()) {
                validateNamespaces.put(namespaceElement.prefix, namespaceElement.value);
            }
        }
        context.namespaces(validateNamespaces);

        //Catch namespace declarations for namespace context
        Map<String, String> namespaces = new HashMap<>();
        for (Message.Namespace namespace : message.getNamespaces()) {
            namespaces.put(namespace.prefix, namespace.value);
        }
        context.setNamespaces(namespaces);

        //check for validate elements, these elements can either have script, xpath or namespace validation information
        //for now we only handle xpath validation
        Map<String, Object> validateXpathExpressions = new HashMap<>();
        for (Message.Validate validateElement : message.getValidates()) {
            extractXPathValidateExpressions(validateElement, validateXpathExpressions);
        }

        if (!validateXpathExpressions.isEmpty()) {
            builder.validate(context.xpath().expressions(validateXpathExpressions));
        } else {
            builder.validate(context);
        }
    }

    private void extractXPathValidateExpressions(Message.Validate validateElement, Map<String, Object> validateXpathExpressions) {
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
        for (Message.Validate.Xpath xpathElement : validateElement.getXpaths()) {
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

    private void handleScriptPayloadBuilder(Message message) {
        if (message.body != null && message.body.builder != null) {
            String scriptType = Optional.ofNullable(message.body.builder.getType()).orElse("groovy");

            Optional<ScriptPayloadBuilder> scriptPayloadBuilder = ScriptPayloadBuilder.lookup(scriptType);

            if (scriptPayloadBuilder.isPresent()) {
                if (message.body.builder.getValue() != null) {
                    scriptPayloadBuilder.get().setScript(message.body.builder.getValue().trim());
                }

                if (message.body.builder.getFile() != null) {
                    if (message.body.builder.getCharset() != null) {
                        scriptPayloadBuilder.get().setFile(FileUtils.getFileResource(message.body.builder.getFile() + FileUtils.FILE_PATH_CHARSET_PARAMETER + message.body.builder.getCharset()));
                    } else {
                        scriptPayloadBuilder.get().setFile(FileUtils.getFileResource(message.body.builder.getFile()));
                    }
                }

                builder.message().body(scriptPayloadBuilder.get());
            } else {
                throw new CitrusRuntimeException(String.format("Failed to resolve script payload builder for type '%s'", scriptType));
            }
        }
    }

    @XmlElement
    public Receive setExtract(Extract value) {
        if (!value.getHeaders().isEmpty()) {
            Map<String, Object> expressions = new LinkedHashMap<>();
            for (Extract.Header extract : value.getHeaders()) {
                expressions.put(extract.name, extract.variable);
            }
            builder.message().extract(fromHeaders()
                                        .expressions(expressions));
        }

        if (!value.getBodyExpressions().isEmpty()) {
            Map<String, Object> expressions = new LinkedHashMap<>();
            for (Extract.Expression extract : value.getBodyExpressions()) {
                String pathExpression = extract.path;

                //construct pathExpression with explicit result-type, like boolean:/TestMessage/Value
                if (extract.resultType != null) {
                    pathExpression = extract.resultType + ":" + pathExpression;
                }

                expressions.put(pathExpression, extract.variable);
            }
            builder.message().extract(fromBody()
                                        .expressions(expressions));
        }

        return this;
    }

    @XmlAttribute
    public Receive setEndpoint(String value) {
        builder.endpoint(value);
        return this;
    }

    @XmlAttribute
    public Receive setActor(String value) {
        this.actor = value;
        return this;
    }

    @XmlAttribute
    public Receive setTimeout(Integer value) {
        builder.timeout(value);
        return this;
    }

    @XmlAttribute
    public Receive setSelect(String value) {
        builder.selector(value);
        return this;
    }

    @XmlElement
    public Receive setSelector(Selector selector) {
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

        return this;
    }

    @Override
    public ReceiveMessageAction build() {
        if (referenceResolver != null) {
            if (actor != null) {
                builder.actor(referenceResolver.resolve(actor, TestActor.class));
            }
        }

        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        builder.setReferenceResolver(referenceResolver);
        this.referenceResolver = referenceResolver;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "headers",
            "bodyExpressions"
    })
    public static class Extract {
        @XmlElement(name = "header")
        protected List<Header> headers;
        @XmlElement(name = "body")
        protected List<Expression> bodyExpressions;

        public List<Header> getHeaders() {
            if (headers == null) {
                headers = new ArrayList<>();
            }
            return this.headers;
        }

        public List<Expression> getBodyExpressions() {
            if (bodyExpressions == null) {
                bodyExpressions = new ArrayList<>();
            }
            return this.bodyExpressions;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Header {
            @XmlAttribute(name = "name", required = true)
            protected String name;
            @XmlAttribute(name = "variable", required = true)
            protected String variable;

            public String getName() {
                return name;
            }

            public void setName(String value) {
                this.name = value;
            }

            public String getVariable() {
                return variable;
            }

            public void setVariable(String value) {
                this.variable = value;
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "value"
        })
        public static class Expression {
            @XmlValue
            protected String value;
            @XmlAttribute(name = "path", required = true)
            protected String path;
            @XmlAttribute(name = "variable", required = true)
            protected String variable;
            @XmlAttribute(name = "result-type")
            protected String resultType;

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }

            public String getPath() {
                return path;
            }

            public void setPath(String value) {
                this.path = value;
            }

            public String getVariable() {
                return variable;
            }

            public void setVariable(String value) {
                this.variable = value;
            }

            public String getResultType() {
                return resultType;
            }

            public void setResultType(String value) {
                this.resultType = value;
            }
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "values",
            "resources",
            "fragments",
            "headers"
    })
    public static class Headers {
        @XmlElement(name = "value")
        protected List<String> values;
        @XmlElement(name = "resource")
        protected List<Resource> resources;
        @XmlElement(name = "fragment")
        protected List<Fragment> fragments;
        @XmlElement(name = "header")
        protected List<Header> headers;
        @XmlAttribute(name = "ignore-case")
        protected String ignoreCase;

        public List<String> getValues() {
            if (values == null) {
                values = new ArrayList<>();
            }
            return this.values;
        }

        public List<Resource> getResources() {
            if (resources == null) {
                resources = new ArrayList<>();
            }
            return this.resources;
        }

        public List<Fragment> getFragments() {
            if (fragments == null) {
                fragments = new ArrayList<>();
            }
            return this.fragments;
        }

        public List<Header> getHeaders() {
            if (headers == null) {
                headers = new ArrayList<>();
            }
            return this.headers;
        }

        public String getIgnoreCase() {
            return ignoreCase;
        }

        public void setIgnoreCase(String value) {
            this.ignoreCase = value;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Header {
            @XmlAttribute(name = "name", required = true)
            protected String name;
            @XmlAttribute(name = "value", required = true)
            protected String value;
            @XmlAttribute(name = "type")
            protected String type;

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

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "anies"
        })
        public static class Fragment {
            @XmlAnyElement
            protected List<org.w3c.dom.Element> anies;

            public List<org.w3c.dom.Element> getAnies() {
                if (anies == null) {
                    anies = new ArrayList<>();
                }
                return this.anies;
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Resource {
            @XmlAttribute(name = "file", required = true)
            protected String file;
            @XmlAttribute(name = "charset")
            protected String charset;

            public String getFile() {
                return file;
            }

            public void setFile(String value) {
                this.file = value;
            }

            public String getCharset() {
                return charset;
            }

            public void setCharset(String value) {
                this.charset = value;
            }
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "builder",
            "resource",
            "data",
            "payload"
    })
    public static class Body {
        protected ScriptDefinitionType builder;
        protected Resource resource;
        protected String data;
        protected Payload payload;

        public ScriptDefinitionType getBuilder() {
            return builder;
        }

        public void setBuilder(ScriptDefinitionType value) {
            this.builder = value;
        }

        public Resource getResource() {
            return resource;
        }

        public void setResource(Resource value) {
            this.resource = value;
        }

        public String getData() {
            return data;
        }

        public void setData(String value) {
            this.data = value;
        }

        public Payload getPayload() {
            return payload;
        }

        public void setPayload(Payload value) {
            this.payload = value;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "anies"
        })
        public static class Payload {
            @XmlAnyElement
            protected List<org.w3c.dom.Element> anies;

            public List<org.w3c.dom.Element> getAnies() {
                if (anies == null) {
                    anies = new ArrayList<>();
                }
                return this.anies;
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Resource {
            @XmlAttribute(name = "file", required = true)
            protected String file;
            @XmlAttribute(name = "charset")
            protected String charset;

            public String getFile() {
                return file;
            }

            public void setFile(String value) {
                this.file = value;
            }

            public String getCharset() {
                return charset;
            }

            public void setCharset(String value) {
                this.charset = value;
            }
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "headers",
            "body",
            "expressions",
            "ignores",
            "validates",
            "namespaces"
    })
    public static class Message {
        protected Headers headers;
        protected Body body;
        @XmlElement(name = "expression")
        protected List<Expression> expressions;
        @XmlElement(name = "ignore")
        protected List<Ignore> ignores;
        @XmlElement(name = "validate")
        protected List<Validate> validates;
        @XmlElement(name = "namespace")
        protected List<Namespace> namespaces;
        @XmlAttribute(name = "schema-validation")
        protected Boolean schemaValidation;
        @XmlAttribute(name = "schema")
        protected String schema;
        @XmlAttribute(name = "schema-repository")
        protected String schemaRepository;
        @XmlAttribute(name = "validator")
        protected String validator;
        @XmlAttribute(name = "validators")
        protected String validators;
        @XmlAttribute(name = "header-validator")
        protected String headerValidator;
        @XmlAttribute(name = "header-validators")
        protected String headerValidators;
        @XmlAttribute(name = "data-dictionary")
        protected String dataDictionary;
        @XmlAttribute(name = "name")
        protected String name;
        @XmlAttribute(name = "type")
        protected String type;

        public Headers getHeaders() {
            return headers;
        }

        public void setHeaders(Headers value) {
            this.headers = value;
        }

        public Body getBody() {
            return body;
        }

        public void setBody(Body body) {
            this.body = body;
        }

        public List<Expression> getExpressions() {
            if (expressions == null) {
                expressions = new ArrayList<>();
            }
            return this.expressions;
        }

        public List<Ignore> getIgnores() {
            if (ignores == null) {
                ignores = new ArrayList<>();
            }
            return this.ignores;
        }

        public List<Validate> getValidates() {
            if (validates == null) {
                validates = new ArrayList<>();
            }
            return this.validates;
        }

        public List<Namespace> getNamespaces() {
            if (namespaces == null) {
                namespaces = new ArrayList<>();
            }
            return this.namespaces;
        }

        public Boolean isSchemaValidation() {
            return schemaValidation;
        }

        public void setSchemaValidation(Boolean value) {
            this.schemaValidation = value;
        }

        public String getSchema() {
            return schema;
        }

        public void setSchema(String value) {
            this.schema = value;
        }

        public String getSchemaRepository() {
            return schemaRepository;
        }

        public void setSchemaRepository(String value) {
            this.schemaRepository = value;
        }

        public String getValidator() {
            return validator;
        }

        public void setValidator(String value) {
            this.validator = value;
        }

        public String getValidators() {
            return validators;
        }

        public void setValidators(String value) {
            this.validators = value;
        }

        public String getHeaderValidator() {
            return headerValidator;
        }

        public void setHeaderValidator(String value) {
            this.headerValidator = value;
        }

        public String getHeaderValidators() {
            return headerValidators;
        }

        public void setHeaderValidators(String value) {
            this.headerValidators = value;
        }

        public String getDataDictionary() {
            return dataDictionary;
        }

        public void setDataDictionary(String value) {
            this.dataDictionary = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String value) {
            this.name = value;
        }

        public String getType() {
            return Objects.requireNonNullElse(type, "xml");
        }

        public void setType(String value) {
            this.type = value;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Expression {
            @XmlAttribute(name = "path", required = true)
            protected String path;
            @XmlAttribute(name = "value", required = true)
            protected String value;

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
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Ignore {
            @XmlAttribute(name = "path", required = true)
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
            @XmlAttribute(name = "prefix", required = true)
            protected String prefix;
            @XmlAttribute(name = "value", required = true)
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
            @XmlAttribute(name = "path")
            protected String path;
            @XmlAttribute(name = "value")
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

            public List<JsonPath> getJsonPaths() {
                if (jsonPaths == null) {
                    jsonPaths = new ArrayList<>();
                }
                return this.jsonPaths;
            }

            public List<Namespace> getNamespaces() {
                if (namespaces == null) {
                    namespaces = new ArrayList<>();
                }
                return this.namespaces;
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
                @XmlAttribute(name = "expression", required = true)
                protected String expression;
                @XmlAttribute(name = "value", required = true)
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
                @XmlAttribute(name = "prefix", required = true)
                protected String prefix;
                @XmlAttribute(name = "value", required = true)
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
                @XmlAttribute(name = "expression", required = true)
                protected String expression;
                @XmlAttribute(name = "value", required = true)
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
            @XmlAttribute(name = "name", required = true)
            protected String name;
            @XmlAttribute(name = "value", required = true)
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
}
