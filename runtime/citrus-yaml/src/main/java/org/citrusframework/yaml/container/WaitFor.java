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

package org.citrusframework.yaml.container;

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
import org.citrusframework.yaml.TestActions;

/**
 * @author Christoph Deppisch
 */
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

    public void setDescription(String value) {
        builder.description(value);
    }

    public void setAction(TestActions action) {
        this.action = action.get();
    }

    public void setMessage(Message message) {
        MessageCondition condition = new MessageCondition();
        condition.setMessageName(message.name);
        builder.condition(condition);
    }

    public void setFile(File file) {
        FileCondition condition = new FileCondition();
        condition.setFilePath(file.path);
        builder.condition(condition);
    }

    public void setHttp(Http http) {
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
    }

    public void setTimeout(String milliseconds) {
        builder.milliseconds(milliseconds);
    }

    public void setInterval(String interval) {
        builder.interval(interval);
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    public static class File {

        protected String path;

        public String getPath() {
            return path;
        }

        public void setPath(String value) {
            this.path = value;
        }

    }

    public static class Http {

        protected String url;
        protected String method;
        protected String status;
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

    public static class Message {

        protected String name;

        public String getName() {
            return name;
        }

        public void setName(String value) {
            this.name = value;
        }
    }
}
