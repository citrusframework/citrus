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

package org.citrusframework.groovy.dsl;

import org.citrusframework.Citrus;
import org.citrusframework.context.TestContext;
import groovy.lang.MissingPropertyException;
import groovy.util.DelegatingScript;

/**
 * @author Christoph Deppisch
 */
public abstract class GroovyScript extends DelegatingScript {

    private Citrus citrusFramework;
    private TestContext context;

    @Override
    public Object getProperty(String propertyName) {
        try {
            return getMetaClass().getProperty(this, propertyName);
        } catch (NullPointerException | MissingPropertyException e) {
            if (propertyName.equals("citrus")) {
                return citrusFramework;
            }

            if (propertyName.equals("context")) {
                return context;
            }

            if (context != null) {
                if (context.getReferenceResolver().isResolvable(propertyName)) {
                    return context.getReferenceResolver().resolve(propertyName);
                }

                if (context.getVariables().containsKey(propertyName)) {
                    return context.getVariable(propertyName);
                }
            }

            if (citrusFramework != null && citrusFramework.getCitrusContext().getReferenceResolver().isResolvable(propertyName)) {
                return citrusFramework.getCitrusContext().getReferenceResolver().resolve(propertyName);
            }

            throw e;
        }
    }

    public void setCitrusFramework(Citrus citrus) {
        this.citrusFramework = citrus;
    }

    public void setContext(TestContext context) {
        this.context = context;
    }
}
