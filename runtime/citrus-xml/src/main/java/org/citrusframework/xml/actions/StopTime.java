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
import org.citrusframework.actions.StopTimeAction;

/**
 * @author Christoph Deppisch
 */
@XmlRootElement(name = "stop-time")
public class StopTime implements TestActionBuilder<StopTimeAction> {

    private final StopTimeAction.Builder builder = new StopTimeAction.Builder();

    @XmlAttribute
    public StopTime setId(String id) {
        builder.id(id);
        return this;
    }

    @XmlAttribute
    public StopTime setSuffix(String suffix) {
        builder.suffix(suffix);
        return this;
    }

    @XmlElement
    public StopTime setDescription(String value) {
        builder.description(value);
        return this;
    }

    @Override
    public StopTimeAction build() {
        return builder.build();
    }
}
