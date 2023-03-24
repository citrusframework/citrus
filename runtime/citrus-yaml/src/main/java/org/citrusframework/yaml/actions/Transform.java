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

package org.citrusframework.yaml.actions;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.TransformAction;

/**
 * @author Christoph Deppisch
 */
public class Transform implements TestActionBuilder<TransformAction> {

    private final TransformAction.Builder builder = new TransformAction.Builder();

    public void setDescription(String value) {
        builder.description(value);
    }

    public void setSource(Source source) {
        if (source.file != null) {
            if (source.charset != null) {
                builder.sourceFile(source.file, source.charset);
            }

            builder.sourceFile(source.file);
        }

        builder.source(source.value);
    }

    public void setXslt(Xslt xslt) {
        if (xslt.file != null) {
            if (xslt.charset != null) {
                builder.xsltFile(xslt.file, xslt.charset);
            }

            builder.xsltFile(xslt.file);
        }

        builder.xslt(xslt.value);
    }

    public void setResult(String variable) {
        builder.result(variable);
    }

    public void setVariable(String variable) {
        builder.result(variable);
    }

    @Override
    public TransformAction build() {
        return builder.build();
    }

    public static class Source {
        protected String value;
        protected String file;
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

    public static class Xslt {
        protected String value;
        protected String file;
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
