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

package org.citrusframework;

/**
 * @author Christoph Deppisch
 */
public class CitrusSpringSettings {

    /**
     * Prevent instantiation of utility class.
     */
    private CitrusSpringSettings() {
        // prevent instantiation
    }

    /** Default application context name */
    public static final String DEFAULT_APPLICATION_CONTEXT_PROPERTY = "citrus.spring.application.context";
    public static final String DEFAULT_APPLICATION_CONTEXT_ENV = "CITRUS_SPRING_APPLICATION_CONTEXT";
    public static final String DEFAULT_APPLICATION_CONTEXT = System.getProperty(DEFAULT_APPLICATION_CONTEXT_PROPERTY, System.getenv(DEFAULT_APPLICATION_CONTEXT_ENV) != null ?
            System.getenv(DEFAULT_APPLICATION_CONTEXT_ENV) : "classpath*:citrus-context.xml");

    /** Default application context class */
    public static final String DEFAULT_APPLICATION_CONTEXT_CLASS_PROPERTY = "citrus.spring.java.config";
    public static final String DEFAULT_APPLICATION_CONTEXT_CLASS_ENV = "CITRUS_SPRING_JAVA_CONFIG";
    public static final String DEFAULT_APPLICATION_CONTEXT_CLASS = System.getProperty(DEFAULT_APPLICATION_CONTEXT_CLASS_PROPERTY,
            System.getenv(DEFAULT_APPLICATION_CONTEXT_CLASS_ENV) != null ? System.getenv(DEFAULT_APPLICATION_CONTEXT_CLASS_ENV) : CitrusSettings.DEFAULT_CONFIG_CLASS);

}
