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

package org.citrusframework.groovy.dsl.actions;

import groovy.lang.Closure;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.groovy.dsl.actions.model.BodySpec;
import org.citrusframework.groovy.dsl.actions.model.HeaderSpec;
import org.citrusframework.groovy.dsl.actions.model.JsonBodySpec;
import org.citrusframework.groovy.dsl.actions.model.XmlBodySpec;
import org.citrusframework.message.builder.DefaultPayloadBuilder;
import org.citrusframework.message.builder.ReceiveMessageBuilderSupport;
import org.citrusframework.util.FileUtils;

/**
 * @author Christoph Deppisch
 */
public class ReceiveActionBuilderWrapper extends ReceiveMessageAction.ReceiveMessageActionBuilder<ReceiveMessageAction, ReceiveActionBuilderWrapper.ReceiveMessageActionBuilderSupport, ReceiveActionBuilderWrapper> {

    public ReceiveMessageActionBuilderSupport message(Closure<?> callable) {
        Closure<?> code = callable.rehydrate(getMessageBuilderSupport(), this, this);
        code.setResolveStrategy(Closure.DELEGATE_ONLY);
        code.call();

        return getMessageBuilderSupport();
    }

    @Override
    public ReceiveMessageActionBuilderSupport getMessageBuilderSupport() {
        if (messageBuilderSupport == null) {
            messageBuilderSupport = new ReceiveMessageActionBuilderSupport(this);
        }
        return super.getMessageBuilderSupport();
    }

    @Override
    protected ReceiveMessageAction doBuild() {
        return new ReceiveMessageAction(this);
    }

    public static class ReceiveMessageActionBuilderSupport extends ReceiveMessageBuilderSupport<ReceiveMessageAction, ReceiveActionBuilderWrapper, ReceiveMessageActionBuilderSupport> {

        public ReceiveMessageActionBuilderSupport(ReceiveActionBuilderWrapper delegate) {
            super(delegate);
        }

        public ReceiveMessageActionBuilderSupport body(Closure<?> callable) {
            BodySpec bodySpec = new BodySpec();
            Closure<?> code = callable.rehydrate(bodySpec, this, this);
            code.setResolveStrategy(Closure.DELEGATE_ONLY);

            return this.body(new DefaultPayloadBuilder(bodySpec.get(code.call())));
        }

        @Override
        public ReceiveMessageActionBuilderSupport body(String payload) {
            return super.body(payload.trim());
        }

        public GroovyMessageBuilderSupport body() {
            return new GroovyMessageBuilderSupport();
        }

        public ReceiveMessageActionBuilderSupport headers(Closure<?> callable) {
            HeaderSpec headerSpec = new HeaderSpec();
            Closure<?> code = callable.rehydrate(headerSpec, this, this);
            code.setResolveStrategy(Closure.DELEGATE_ONLY);
            code.call();

            return this.headers(headerSpec.get());
        }

        public class GroovyMessageBuilderSupport {

            public ReceiveMessageActionBuilderSupport resource(String filePath) {
                return ReceiveMessageActionBuilderSupport.this.body(FileUtils.getFileResource(filePath));
            }

            public ReceiveMessageActionBuilderSupport json(Closure<?> callable) {
                JsonBodySpec bodySpec = new JsonBodySpec();
                Closure<?> code = callable.rehydrate(bodySpec, this, this);
                code.setResolveStrategy(Closure.DELEGATE_ONLY);

                return ReceiveMessageActionBuilderSupport.this.body(new DefaultPayloadBuilder(bodySpec.get(code.call())));
            }

            public ReceiveMessageActionBuilderSupport xml(Closure<?> callable) {
                XmlBodySpec bodySpec = new XmlBodySpec();
                Closure<?> code = callable.rehydrate(bodySpec, this, this);
                code.setResolveStrategy(Closure.DELEGATE_ONLY);

                return ReceiveMessageActionBuilderSupport.this.body(new DefaultPayloadBuilder(bodySpec.get(code.call())));
            }
        }
    }
}
