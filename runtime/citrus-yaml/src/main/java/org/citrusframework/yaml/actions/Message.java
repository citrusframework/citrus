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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.citrusframework.CitrusSettings;
import org.citrusframework.yaml.SchemaProperty;
import org.citrusframework.yaml.SchemaType;
import org.citrusframework.yaml.actions.script.ScriptDefinitionType;

public class Message {
    protected Boolean headerIgnoreCase;
    protected List<Header> headers;
    protected Body body;
    protected List<Expression> expression;
    protected String dataDictionary;
    protected Boolean schemaValidation;
    protected String schema;
    protected String schemaRepository;
    protected String name;
    protected String type;

    public Boolean isHeaderIgnoreCase() {
        return headerIgnoreCase;
    }

    @SchemaProperty(advanced = true, description = "When enabled the header case is not verified.")
    public void setHeaderIgnoreCase(Boolean value) {
        this.headerIgnoreCase = value;
    }

    public List<Header> getHeaders() {
        if (headers == null) {
            headers = new ArrayList<>();
        }

        return headers;
    }

    @SchemaProperty(description = "The message headers.")
    public void setHeaders(List<Header> value) {
        this.headers = value;
    }

    public Body getBody() {
        return body;
    }

    @SchemaProperty(description = "The message body.")
    public void setBody(Body body) {
        this.body = body;
    }

    public List<Expression> getExpression() {
        if (expression == null) {
            expression = new ArrayList<>();
        }
        return this.expression;
    }

    @SchemaProperty(advanced = true, description = "List of path expressions to evaluate on the message content before processing the message.")
    public void setExpression(List<Expression> expression) {
        this.expression = expression;
    }

    public String getDataDictionary() {
        return dataDictionary;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData( key = "$comment", value = "group:dictionary") },
            description = "Sets a data dictionary that transforms message content before it is being processed."
    )
    public void setDataDictionary(String value) {
        this.dataDictionary = value;
    }

    public Boolean isSchemaValidation() {
        return schemaValidation;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData( key = "$comment", value = "group:schema") },
            description = "Enables the schema validation.")
    public void setSchemaValidation(Boolean value) {
        this.schemaValidation = value;
    }

    public String getSchema() {
        return schema;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData( key = "$comment", value = "group:schema") },
            description = "The reference to a schema in the bean registry that should be used for validation.")
    public void setSchema(String value) {
        this.schema = value;
    }

    public String getSchemaRepository() {
        return schemaRepository;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData( key = "$comment", value = "group:schema") },
            description = "The schema repository holding the schema.")
    public void setSchemaRepository(String value) {
        this.schemaRepository = value;
    }

    public String getName() {
        return name;
    }

    @SchemaProperty(description = "The message name. Named messages may be referenced in subsequent test steps.")
    public void setName(String value) {
        this.name = value;
    }

    public String getType() {
        return Objects.requireNonNullElse(type, CitrusSettings.DEFAULT_MESSAGE_TYPE);
    }

    @SchemaProperty(advanced = true,
            description = "The message type. Gives the test action a hint which validators are capable of performing proper message validation for this message.")
    public void setType(String value) {
        this.type = value;
    }

    @SchemaType(oneOf = { "value", "resource", "data" } )
    public static class Header {
        protected String data;
        protected Resource resource;

        protected String name;
        protected String value;
        protected String type;

        public String getName() {
            return name;
        }

        @SchemaProperty(description = "The message header name.")
        public void setName(String value) {
            this.name = value;
        }

        @SchemaProperty(description = "The message header value as inline data.")
        public void setData(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty(description = "The message header value.")
        public void setValue(String value) {
            this.value = value;
        }

        public String getType() {
            return type;
        }

        @SchemaProperty(description = "The header data loaded from a file resource.")
        public void setResource(Resource resource) {
            this.resource = resource;
        }

        public Resource getResource() {
            return resource;
        }

        @SchemaProperty(advanced = true, description = "The message header type to create typed message headers.")
        public void setType(String type) {
            this.type = type;
        }

        public static class Resource {
            protected String file;
            protected String charset;

            public String getFile() {
                return file;
            }

            @SchemaProperty(required = true, description = "The file resource path.")
            public void setFile(String value) {
                this.file = value;
            }

            public String getCharset() {
                return charset;
            }

            @SchemaProperty(advanced = true, description = "Optional file resource charset used to read the file content.")
            public void setCharset(String value) {
                this.charset = value;
            }
        }
    }

    @SchemaType(oneOf = { "data", "resource", "script" } )
    public static class Body {
        protected ScriptDefinitionType script;
        protected Body.Resource resource;
        protected String data;

        @Deprecated
        public ScriptDefinitionType getBuilder() {
            return script;
        }

        @Deprecated
        public void setBuilder(ScriptDefinitionType value) {
            this.script = value;
        }

        public ScriptDefinitionType getScript() {
            return script;
        }

        @SchemaProperty(description = "The message body set via script.")
        public void setScript(ScriptDefinitionType value) {
            this.script = value;
        }

        public Body.Resource getResource() {
            return resource;
        }

        @SchemaProperty(description = "The message body loaded from a file resource.")
        public void setResource(Body.Resource value) {
            this.resource = value;
        }

        public String getData() {
            return data;
        }

        @SchemaProperty(description = "The message body content as inline data.")
        public void setData(String value) {
            this.data = value;
        }

        public static class Resource {
            protected String file;
            protected String charset;

            public String getFile() {
                return file;
            }

            @SchemaProperty
            public void setFile(String value) {
                this.file = value;
            }

            public String getCharset() {
                return charset;
            }

            @SchemaProperty(advanced = true)
            public void setCharset(String value) {
                this.charset = value;
            }
        }
    }

    public static class Expression {
        protected String path;
        protected String value;

        public String getPath() {
            return path;
        }

        @SchemaProperty(required = true, description = "The path expression to evaluate.")
        public void setPath(String value) {
            this.path = value;
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty(required = true, description = "The expected expression result value.")
        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class Processor {
        protected Gzip gzip;
        protected Binary binary;
        protected Xpath xpath;
        protected Json json;
        protected Camel camel;

        @SchemaProperty(description = "Gzip message processor transforms content into zipped content.")
        public void setGzip(Gzip gzip) {
            this.gzip = gzip;
        }

        public Gzip getGzip() {
            return gzip;
        }

        @SchemaProperty(description = "Message processor to create binary message content.")
        public void setBinary(Binary binary) {
            this.binary = binary;
        }

        public Binary getBinary() {
            return binary;
        }

        @SchemaProperty(description = "Xml message processors.")
        public void setXpath(Xpath xpath) {
            this.xpath = xpath;
        }

        public Xpath getXpath() {
            return xpath;
        }

        @SchemaProperty(description = "Json message processors.")
        public void setJson(Json json) {
            this.json = json;
        }

        public Json getJson() {
            return json;
        }

        @SchemaProperty(description = "Camel message processors using transform capabilities of Apache Camel with supported data formats.")
        public void setCamel(Camel camel) {
            this.camel = camel;
        }

        public Camel getCamel() {
            return camel;
        }

        public static class Gzip {
            protected String encoding;

            public String getEncoding() {
                return encoding;
            }

            @SchemaProperty(description = "Sets a custom file encoding.", advanced = true)
            public void setEncoding(String value) {
                this.encoding = value;
            }
        }

        public static class Binary {
            protected String encoding;

            public String getEncoding() {
                return encoding;
            }

            @SchemaProperty(description = "Sets a custom file encoding.", advanced = true)
            public void setEncoding(String value) {
                this.encoding = value;
            }
        }

        public static class Json {
            protected List<Expression> expressions;

            @SchemaProperty(description = "List of expressions to evaluate")
            public void setExpressions(List<Expression> expressions) {
                this.expressions = expressions;
            }

            public List<Expression> getExpressions() {
                if (expressions == null) {
                    expressions = new ArrayList<>();
                }

                return expressions;
            }
        }

        public static class Xpath {
            protected List<Expression> expressions;
            protected List<Namespace> namespaces;

            @SchemaProperty(description = "List of expressions to evaluate")
            public void setExpressions(List<Expression> expressions) {
                this.expressions = expressions;
            }

            public List<Expression> getExpressions() {
                if (expressions == null) {
                    expressions = new ArrayList<>();
                }

                return expressions;
            }

            @SchemaProperty(description = "List of namespaces that should be used with the expression evaluation.")
            public void setNamespaces(List<Namespace> namespaces) {
                this.namespaces = namespaces;
            }

            public List<Namespace> getNamespaces() {
                if (namespaces == null) {
                    namespaces = new ArrayList<>();
                }

                return namespaces;
            }

            public static class Namespace {
                protected String prefix;
                protected String uri;

                public String getPrefix() {
                    return prefix;
                }

                @SchemaProperty(required = true, description = "The namespace prefix.")
                public void setPrefix(String value) {
                    this.prefix = value;
                }

                public String getUri() {
                    return uri;
                }

                @SchemaProperty(required = true, description = "The namespace uri.")
                public void setUri(String value) {
                    this.uri = value;
                }
            }
        }

        public static class Expression {
            protected String path;
            protected String value;

            public String getPath() {
                return path;
            }

            @SchemaProperty(description = "The path expression to evaluate.")
            public void setPath(String value) {
                this.path = value;
            }

            public String getValue() {
                return value;
            }

            @SchemaProperty(required = true, description = "The path expression value.")
            public void setValue(String value) {
                this.value = value;
            }

        }

        public static class Camel {
            protected String camelContext;
            protected String processor;
            protected Map<String, Object> transform;
            protected Map<String, Object> marshal;
            protected Map<String, Object> unmarshal;
            protected ConvertBodyTo convertBodyTo;

            public String getCamelContext() {
                return camelContext;
            }

            @SchemaProperty(description = "Camel context used with this processor.")
            public void setCamelContext(String camelContext) {
                this.camelContext = camelContext;
            }

            public String getProcessor() {
                return processor;
            }

            @SchemaProperty(description = "Camel message processor referenced by its bean name.")
            public void setProcessor(String processor) {
                this.processor = processor;
            }

            public Map<String, Object> getTransform() {
                return transform;
            }

            @SchemaProperty(description = "Transform message content with a Camel expression.")
            public void setTransform(Map<String, Object> transform) {
                this.transform = transform;
            }

            public Map<String, Object> getMarshal() {
                return marshal;
            }

            @SchemaProperty(description = "Marshal message content with a Camel data format.")
            public void setMarshal(Map<String, Object> node) {
                this.marshal = node;
            }

            public Map<String, Object> getUnmarshal() {
                return unmarshal;
            }

            @SchemaProperty(description = "Unmarshal message content with a Camel data format.")
            public void setUnmarshal(Map<String, Object> unmarshal) {
                this.unmarshal = unmarshal;
            }

            public ConvertBodyTo getConvertBodyTo() {
                return convertBodyTo;
            }

            @SchemaProperty(description = "Converts message body to given type.")
            public void setConvertBodyTo(ConvertBodyTo convertBodyTo) {
                this.convertBodyTo = convertBodyTo;
            }

            public static class ConvertBodyTo {
                protected String type;

                public String getType() {
                    return type;
                }

                @SchemaProperty(required = true, description = "Fully qualified class name of the target type.")
                public void setType(String type) {
                    this.type = type;
                }
            }

            public static class DataFormat {

            }
        }
    }

    public static class Extract {
        protected List<Header> header;
        protected List<Expression> body;

        public List<Header> getHeader() {
            if (header == null) {
                header = new ArrayList<>();
            }
            return this.header;
        }

        @SchemaProperty(description = "Extract elements from the message header into test variables.")
        public void setHeader(List<Header> header) {
            this.header = header;
        }

        public List<Expression> getBody() {
            if (body == null) {
                body = new ArrayList<>();
            }
            return this.body;
        }

        @SchemaProperty(description = "Extract elements from the message body into test variables.")
        public void setBody(List<Expression> body) {
            this.body = body;
        }

        public static class Header {
            protected String name;
            protected String variable;

            public String getName() {
                return name;
            }

            @SchemaProperty(required = true, description = "The name of the message header.")
            public void setName(String value) {
                this.name = value;
            }

            public String getVariable() {
                return variable;
            }

            @SchemaProperty(required = true, description = "The test variable name.")
            public void setVariable(String value) {
                this.variable = value;
            }
        }

        public static class Expression {
            protected String path;
            protected String variable;
            protected String resultType;

            public String getPath() {
                return path;
            }

            @SchemaProperty(description = "The path expression to evaluate.")
            public void setPath(String value) {
                this.path = value;
            }

            public String getVariable() {
                return variable;
            }

            @SchemaProperty(required = true, description = "The test variable name.")
            public void setVariable(String value) {
                this.variable = value;
            }

            public String getResultType() {
                return resultType;
            }

            @SchemaProperty(advanced = true, description = "The expression result type. " +
                    "By default the path expression evaluate to String values." +
                    "With this setting you can force another expression result type.")
            public void setResultType(String value) {
                this.resultType = value;
            }
        }
    }
}
