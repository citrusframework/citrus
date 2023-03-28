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

package org.citrusframework.yaml.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.citrusframework.yaml.actions.script.ScriptDefinitionType;

/**
 * @author Christoph Deppisch
 */
public class Message {
    protected String headerIgnoreCase;
    protected List<Header> headers;
    protected Body body;
    protected List<Expression> expression;
    protected String dataDictionary;
    protected Boolean schemaValidation;
    protected String schema;
    protected String schemaRepository;
    protected String name;
    protected String type;

    public String getHeaderIgnoreCase() {
        return headerIgnoreCase;
    }

    public void setHeaderIgnoreCase(String value) {
        this.headerIgnoreCase = value;
    }

    public List<Header> getHeaders() {
        if (headers == null) {
            headers = new ArrayList<>();
        }

        return headers;
    }

    public void setHeaders(List<Header> value) {
        this.headers = value;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public List<Expression> getExpression() {
        if (expression == null) {
            expression = new ArrayList<>();
        }
        return this.expression;
    }

    public void setExpression(List<Expression> expression) {
        this.expression = expression;
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

    public static class Header {
        protected String data;
        protected Resource resource;

        protected String name;
        protected String value;
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

        public static class Resource {
            protected String file;
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

    public static class Body {
        protected ScriptDefinitionType builder;
        protected Body.Resource resource;
        protected String data;

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

        public static class Resource {
            protected String file;
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

    public static class Expression {
        protected String path;
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

    public static class Extract {
        protected List<Header> header;
        protected List<Expression> body;

        public List<Header> getHeader() {
            if (header == null) {
                header = new ArrayList<>();
            }
            return this.header;
        }

        public void setHeader(List<Header> header) {
            this.header = header;
        }

        public List<Expression> getBody() {
            if (body == null) {
                body = new ArrayList<>();
            }
            return this.body;
        }

        public void setBody(List<Expression> body) {
            this.body = body;
        }

        public static class Header {
            protected String name;
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

        public static class Expression {
            protected String value;
            protected String path;
            protected String variable;
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
