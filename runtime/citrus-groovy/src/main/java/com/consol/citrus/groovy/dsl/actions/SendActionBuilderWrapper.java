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

package com.consol.citrus.groovy.dsl.actions;

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.groovy.dsl.actions.model.BodySpec;
import com.consol.citrus.groovy.dsl.actions.model.HeaderSpec;
import com.consol.citrus.message.builder.DefaultPayloadBuilder;
import com.consol.citrus.message.builder.SendMessageBuilderSupport;
import groovy.lang.Closure;

/**
 * @author Christoph Deppisch
 */
public class SendActionBuilderWrapper extends SendMessageAction.SendMessageActionBuilder<SendMessageAction, SendActionBuilderWrapper.SendMessageActionBuilderSupport, SendActionBuilderWrapper> {

    public SendMessageActionBuilderSupport message(Closure<?> callable) {
        Closure<?> code = callable.rehydrate(getMessageBuilderSupport(), this, this);
        code.setResolveStrategy(Closure.DELEGATE_ONLY);
        code.call();

        return getMessageBuilderSupport();
    }

    @Override
    public SendMessageActionBuilderSupport getMessageBuilderSupport() {
        if (messageBuilderSupport == null) {
            messageBuilderSupport = new SendMessageActionBuilderSupport(this);
        }
        return super.getMessageBuilderSupport();
    }

    @Override
    protected SendMessageAction doBuild() {
        return new SendMessageAction(this);
    }

    public static class SendMessageActionBuilderSupport extends SendMessageBuilderSupport<SendMessageAction, SendActionBuilderWrapper, SendMessageActionBuilderSupport> {

        public SendMessageActionBuilderSupport(SendActionBuilderWrapper delegate) {
            super(delegate);
        }

        public SendMessageActionBuilderSupport body(Closure<?> callable) {
            BodySpec bodySpec = new BodySpec();
            Closure<?> code = callable.rehydrate(bodySpec, this, this);
            code.setResolveStrategy(Closure.DELEGATE_ONLY);

            return this.body(new DefaultPayloadBuilder(bodySpec.get(code.call())));
        }

        public SendMessageActionBuilderSupport headers(Closure<?> callable) {
            HeaderSpec headerSpec = new HeaderSpec();
            Closure<?> code = callable.rehydrate(headerSpec, this, this);
            code.setResolveStrategy(Closure.DELEGATE_ONLY);
            code.call();

            return this.headers(headerSpec.get());
        }

    }

}
