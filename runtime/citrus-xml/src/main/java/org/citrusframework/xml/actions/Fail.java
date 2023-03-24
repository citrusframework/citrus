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

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.FailAction;

/**
 * @author Christoph Deppisch
 */
@XmlRootElement(name = "fail")
public class Fail implements TestActionBuilder<FailAction> {

    private final FailAction.Builder builder = new FailAction.Builder();

    @XmlElement
    public Fail setDescription(String value) {
        builder.description(value);
        return this;
    }

    @XmlAttribute
    public Fail setMessage(String message) {
        builder.message(message);
        return this;
    }

    @XmlElement(name = "message")
    public Fail setMessageElement(String message) {
        builder.message(message);
        return this;
    }

    @Override
    public FailAction build() {
        return builder.build();
    }
}
