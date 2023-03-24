/*
 * Copyright 2020 the original author or authors.
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

package org.citrusframework.message.builder;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.MessagePayloadBuilder;
import org.citrusframework.xml.Marshaller;
import org.citrusframework.xml.StringResult;

/**
 * @author Christoph Deppisch
 */
public class MarshallingPayloadBuilder extends DefaultPayloadBuilder {

    private final Marshaller marshaller;
    private final String marshallerName;

    /**
     * Default constructor using just model object.
     * @param model
     */
    public MarshallingPayloadBuilder(Object model) {
        super(model);

        this.marshaller = null;
        this.marshallerName = null;
    }

    /**
     * Default constructor using object marshaller and model object.
     * @param model
     * @param marshaller
     */
    public MarshallingPayloadBuilder(Object model, Marshaller marshaller) {
        super(model);

        this.marshaller = marshaller;
        this.marshallerName = null;
    }

    /**
     * Default constructor using object marshaller name and model object.
     * @param model
     * @param marshallerName
     */
    public MarshallingPayloadBuilder(Object model, String marshallerName) {
        super(model);

        this.marshallerName = marshallerName;
        this.marshaller = null;
    }

    @Override
    public Object buildPayload(TestContext context) {
        if (getPayload() == null || getPayload() instanceof String) {
            return super.buildPayload(context);
        }

        if (marshaller != null) {
            return buildPayload(marshaller, getPayload(), context);
        }

        if (marshallerName != null) {
            if (context.getReferenceResolver().isResolvable(marshallerName)) {
                Marshaller objectMapper = context.getReferenceResolver().resolve(marshallerName, Marshaller.class);
                return buildPayload(objectMapper, getPayload(), context);
            } else {
                throw new CitrusRuntimeException(String.format("Unable to find proper object marshaller for name '%s'", marshallerName));
            }
        }

        Map<String, Marshaller> marshallerMap = context.getReferenceResolver().resolveAll(Marshaller.class);
        if (marshallerMap.size() == 1) {
            return buildPayload(marshallerMap.values().iterator().next(), getPayload(), context);
        } else {
            throw new CitrusRuntimeException(String.format("Unable to auto detect object marshaller - " +
                    "found %d matching marshaller instances in reference resolver", marshallerMap.size()));
        }
    }

    private Object buildPayload(Marshaller marshaller, Object model, TestContext context) {
        final StringResult result = new StringResult();

        try {
            marshaller.marshal(model, result);
        } catch (final Exception e) {
            throw new CitrusRuntimeException("Failed to marshal object graph for message payload", e);
        }

        return context.replaceDynamicContentInString(result.toString());
    }

    public static class Builder implements MessagePayloadBuilder.Builder<MarshallingPayloadBuilder, Builder> {

        private Object model;
        private Marshaller marshaller;
        private String marshallerName;

        public static Builder marshal(Object  model) {
            Builder builder = new Builder();
            builder.model = model;
            return builder;
        }

        public static Builder marshal(Object  model, String marshaller) {
            Builder builder = new Builder();
            builder.model = model;
            builder.marshallerName = marshaller;
            return builder;
        }

        public static Builder marshal(Object  model, Marshaller marshaller) {
            Builder builder = new Builder();
            builder.model = model;
            builder.marshaller = marshaller;
            return builder;
        }

        public Builder marshaller(String marshallerName) {
            this.marshallerName = marshallerName;
            return this;
        }

        public Builder marshaller(Marshaller marshaller) {
            this.marshaller = marshaller;
            return this;
        }

        @Override
        public MarshallingPayloadBuilder build() {
            if (marshaller != null) {
                return new MarshallingPayloadBuilder(model, marshaller);
            } else if (marshallerName != null) {
                return new MarshallingPayloadBuilder(model, marshallerName);
            } else {
                return new MarshallingPayloadBuilder(model);
            }
        }

    }

}
