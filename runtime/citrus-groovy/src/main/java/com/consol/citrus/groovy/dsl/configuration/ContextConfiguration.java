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

package com.consol.citrus.groovy.dsl.configuration;

import java.io.IOException;

import com.consol.citrus.Citrus;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.groovy.dsl.GroovyShellUtils;
import com.consol.citrus.groovy.dsl.configuration.beans.BeansConfiguration;
import com.consol.citrus.groovy.dsl.configuration.beans.QueueConfiguration;
import com.consol.citrus.groovy.dsl.configuration.endpoints.EndpointsConfiguration;
import com.consol.citrus.util.FileUtils;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.springframework.core.io.Resource;

/**
 * @author Christoph Deppisch
 */
public class ContextConfiguration {

    private final Citrus citrus;
    private final TestContext context;
    private final String basePath;

    public ContextConfiguration(Citrus citrus, TestContext context, String basePath) {
        this.citrus = citrus;
        this.context = context;
        this.basePath = basePath;
    }

    public void load(Class<?> configClass) {
        citrus.getCitrusContext().parseConfiguration(configClass);
    }

    public void load(String pathOrType) {
        try {
            if (pathOrType.endsWith(".groovy")) {
                Resource file;
                if (pathOrType.startsWith(".")) {
                    file = FileUtils.getFileResource(basePath + pathOrType.substring(1), context);
                } else {
                    file = FileUtils.getFileResource(pathOrType, context);
                }

                GroovyShellUtils.run(new ImportCustomizer(), this, FileUtils.readToString(file), citrus, context);
            } else {
                citrus.getCitrusContext().parseConfiguration(Class.forName(pathOrType));
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new CitrusRuntimeException(String.format("Failed to load configuration from file '%s'", pathOrType), e);
        }
    }

    public void configuration(Closure<?> callable) {
        callable.setResolveStrategy(Closure.DELEGATE_FIRST);
        callable.setDelegate(this);
        callable.call();
    }

    public void beans(@DelegatesTo(BeansConfiguration.class) Closure<?> callable) {
        callable.setResolveStrategy(Closure.DELEGATE_FIRST);
        callable.setDelegate(new BeansConfiguration(citrus));
        callable.call();
    }

    public void queues(@DelegatesTo(QueueConfiguration.class) Closure<?> callable) {
        callable.setResolveStrategy(Closure.DELEGATE_FIRST);
        callable.setDelegate(new QueueConfiguration(citrus));
        callable.call();
    }

    public void endpoints(@DelegatesTo(EndpointsConfiguration.class) Closure<?> callable) {
        callable.setResolveStrategy(Closure.DELEGATE_FIRST);
        callable.setDelegate(new EndpointsConfiguration(citrus));
        callable.call();
    }
}
