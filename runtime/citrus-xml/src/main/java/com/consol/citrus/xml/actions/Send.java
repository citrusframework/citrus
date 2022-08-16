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
package com.consol.citrus.xml.actions;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.TestActor;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;

@XmlRootElement(name = "send")
public class Send implements TestActionBuilder<SendMessageAction>, ReferenceResolverAware {

    private final SendMessageAction.Builder builder = new SendMessageAction.Builder();

    private String actor;
    private ReferenceResolver referenceResolver;

    @XmlElement
    public Send setDescription(String value) {
        builder.description(value);
        return this;
    }

    @XmlElement(required = true)
    public Send setMessage(Message message) {
        MessageSupport.configureMessage(builder, message);

        if (message.schema != null || message.schemaRepository != null) {
            builder.message()
                    .schemaValidation(message.isSchemaValidation())
                    .schema(message.schema)
                    .schemaRepository(message.schemaRepository);
        } else if (message.isSchemaValidation() != null && !message.isSchemaValidation()) {
            builder.message().schemaValidation(message.isSchemaValidation());
        }

        return this;
    }

    @XmlElement
    public Send setExtract(Message.Extract value) {
        MessageSupport.configureExtract(builder, value);

        return this;
    }

    @XmlAttribute
    public Send setEndpoint(String value) {
        builder.endpoint(value);
        return this;
    }

    @XmlAttribute
    public Send setActor(String value) {
        this.actor = value;
        return this;
    }

    @XmlAttribute(name = "fork")
    public Send setFork(Boolean value) {
        builder.fork(value);
        return this;
    }

    @Override
    public SendMessageAction build() {
        if (referenceResolver != null) {
            if (actor != null) {
                builder.actor(referenceResolver.resolve(actor, TestActor.class));
            }
        }

        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        builder.setReferenceResolver(referenceResolver);
        this.referenceResolver = referenceResolver;
    }

}
