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

package org.citrusframework.xml.actions;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.TransformAction;

/**
 * @author Christoph Deppisch
 */
@XmlRootElement(name = "transform")
public class Transform implements TestActionBuilder<TransformAction> {

    private final TransformAction.Builder builder = new TransformAction.Builder();

    @XmlElement
    public Transform setDescription(String value) {
        builder.description(value);
        return this;
    }

    @XmlElement(required = true)
    public Transform setSource(Source source) {
        if (source.file != null) {
            if (source.charset != null) {
                builder.sourceFile(source.file, source.charset);
            }

            builder.sourceFile(source.file);
        }

        builder.source(source.value);

        return this;
    }

    @XmlElement(name = "xslt", required = true)
    public Transform setXslt(Xslt xslt) {
        if (xslt.file != null) {
            if (xslt.charset != null) {
                builder.xsltFile(xslt.file, xslt.charset);
            }

            builder.xsltFile(xslt.file);
        }

        builder.xslt(xslt.value);

        return this;
    }

    @XmlAttribute
    public Transform setResult(String variable) {
        builder.result(variable);
        return this;
    }

    @XmlAttribute
    public Transform setVariable(String variable) {
        builder.result(variable);
        return this;
    }

    @Override
    public TransformAction build() {
        return builder.build();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Source {
        @XmlValue
        protected String value;
        @XmlAttribute(name = "file")
        protected String file;
        @XmlAttribute(name = "charset")
        protected String charset;

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getCharset() {
            return charset;
        }

        public void setCharset(String charset) {
            this.charset = charset;
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
    public static class Xslt {
        @XmlValue
        protected String value;
        @XmlAttribute(name = "file")
        protected String file;
        @XmlAttribute(name = "charset")
        protected String charset;

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getCharset() {
            return charset;
        }

        public void setCharset(String charset) {
            this.charset = charset;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
