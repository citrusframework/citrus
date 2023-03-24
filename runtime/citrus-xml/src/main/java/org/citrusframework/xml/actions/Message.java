/*
 * Copyright 2022 the original author or authors.
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

package org.citrusframework.xml.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;

import org.citrusframework.xml.actions.script.ScriptDefinitionType;

/**
 * @author Christoph Deppisch
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "headers",
        "body",
        "expressions"
})
public class Message {
    @XmlElement(name = "headers")
    protected Headers headers;
    @XmlElement(name = "body")
    protected Body body;
    @XmlElement(name = "expression")
    protected List<Expression> expressions;
    @XmlAttribute(name = "data-dictionary")
    protected String dataDictionary;
    @XmlAttribute(name = "schema-validation")
    protected Boolean schemaValidation;
    @XmlAttribute(name = "schema")
    protected String schema;
    @XmlAttribute(name = "schema-repository")
    protected String schemaRepository;
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

    public String getDataDictionary() {
        return dataDictionary;
    }

    public void setDataDictionary(String value) {
        this.dataDictionary = value;
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
    @XmlType(name = "", propOrder = {
            "headers"
    })
    public static class Headers {
        @XmlElement(name = "header")
        protected List<Headers.Header> headers;
        @XmlAttribute(name = "ignore-case")
        protected String ignoreCase;

        public List<Headers.Header> getHeaders() {
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
        @XmlType(name = "", propOrder = {
                "data",
                "resource",
                "fragment",
        })
        public static class Header {
            @XmlElement
            protected String data;
            @XmlElement
            protected Resource resource;
            @XmlElement
            protected Fragment fragment;

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

            public void setData(String data) {
                this.data = data;
            }

            public String getData() {
                return data;
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
        protected Body.Resource resource;
        protected String data;
        protected Body.Payload payload;

        public ScriptDefinitionType getBuilder() {
            return builder;
        }

        public void setBuilder(ScriptDefinitionType value) {
            this.builder = value;
        }

        public Body.Resource getResource() {
            return resource;
        }

        public void setResource(Body.Resource value) {
            this.resource = value;
        }

        public String getData() {
            return data;
        }

        public void setData(String value) {
            this.data = value;
        }

        public Body.Payload getPayload() {
            return payload;
        }

        public void setPayload(Body.Payload value) {
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
}
