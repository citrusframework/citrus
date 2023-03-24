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

package org.citrusframework.groovy.dsl.test;

import org.citrusframework.context.TestContext;
import groovy.lang.GroovyObjectSupport;

/**
 * @author Christoph Deppisch
 */
public class VariablesConfiguration extends GroovyObjectSupport {

    private final TestContext context;

    public VariablesConfiguration(TestContext context) {
        this.context = context;
    }

    public void propertyMissing(String name, Object value) {
        if (value instanceof String) {
            context.setVariable(name, context.replaceDynamicContentInString((String) value));
        } else {
            context.setVariable(name, value);
        }
    }
}
