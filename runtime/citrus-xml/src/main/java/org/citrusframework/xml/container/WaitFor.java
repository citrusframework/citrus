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

package org.citrusframework.xml.container;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.condition.ActionCondition;
import org.citrusframework.condition.Condition;
import org.citrusframework.condition.FileCondition;
import org.citrusframework.condition.HttpCondition;
import org.citrusframework.condition.MessageCondition;
import org.citrusframework.container.Wait;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.StringUtils;
import org.citrusframework.xml.TestActions;

@XmlRootElement(name = "wait-for")
public class WaitFor implements TestActionBuilder<Wait>, ReferenceResolverAware {

    private final Wait.Builder<Condition> builder = new Wait.Builder<>();

    private TestActionBuilder<?> action;
    private ReferenceResolver referenceResolver;

    @Override
    public Wait build() {
        if (action != null) {
            if (action instanceof ReferenceResolverAware) {
                ((ReferenceResolverAware) action).setReferenceResolver(referenceResolver);
            }

            ActionCondition condition = new ActionCondition();
            condition.setAction(action.build());
            builder.condition(condition);
        }

        return builder.build();
    }

    @XmlElement
    public WaitFor setDescription(String value) {
        builder.description(value);
        return this;
    }

    @XmlElement
    public WaitFor setAction(TestActions action) {
        this.action = (TestActionBuilder<?>) action.getActions().get(0);
        return this;
    }

    @XmlElement
    public WaitFor setMessage(Message message) {
        MessageCondition condition = new MessageCondition();
        condition.setMessageName(message.name);
        builder.condition(condition);
        return this;
    }

    @XmlElement
    public WaitFor setFile(File file) {
        FileCondition condition = new FileCondition();
        condition.setFilePath(file.path);
        builder.condition(condition);
        return this;
    }

    @XmlElement
    public WaitFor setHttp(Http http) {
        HttpCondition condition = new HttpCondition();
        condition.setUrl(http.url);

        String method = http.method;
        if (StringUtils.hasText(method)) {
            condition.setMethod(method);
        }

        String statusCode = http.status;
        if (StringUtils.hasText(statusCode)) {
            condition.setHttpResponseCode(statusCode);
        }

        String timeout = http.timeout;
        if (StringUtils.hasText(timeout)) {
            condition.setTimeout(timeout);
        }
        builder.condition(condition);
        return this;
    }

    @XmlAttribute
    public WaitFor setTimeout(String milliseconds) {
        builder.milliseconds(milliseconds);
        return this;
    }

    @XmlAttribute
    public WaitFor setInterval(String interval) {
        builder.interval(interval);
        return this;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class File {

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
    public static class Http {

        @XmlAttribute(name = "url")
        protected String url;
        @XmlAttribute(name = "method")
        protected String method;
        @XmlAttribute(name = "status")
        protected String status;
        @XmlAttribute(name = "timeout")
        protected String timeout;

        public String getUrl() {
            return url;
        }

        public void setUrl(String value) {
            this.url = value;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String value) {
            this.method = value;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String value) {
            this.status = value;
        }

        public String getTimeout() {
            return timeout;
        }

        public void setTimeout(String value) {
            this.timeout = value;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Message {

        @XmlAttribute(name = "name", required = true)
        protected String name;

        public String getName() {
            return name;
        }

        public void setName(String value) {
            this.name = value;
        }
    }
}
