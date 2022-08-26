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
import javax.xml.bind.annotation.XmlRootElement;
import java.util.concurrent.TimeUnit;

import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.actions.SleepAction;

/**
 * @author Christoph Deppisch
 */
@XmlRootElement(name = "sleep")
public class Sleep implements TestActionBuilder<SleepAction> {

    private final SleepAction.Builder builder = new SleepAction.Builder();

    @XmlAttribute
    public Sleep setTime(String time) {
        builder.time(time);
        return this;
    }

    @XmlAttribute
    public Sleep setMilliseconds(String milliseconds) {
        builder.time(milliseconds, TimeUnit.MILLISECONDS);
        return this;
    }

    @XmlAttribute
    public Sleep setSeconds(String seconds) {
        builder.time(seconds, TimeUnit.SECONDS);
        return this;
    }

    @Override
    public SleepAction build() {
        return builder.build();
    }
}
