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

package com.consol.citrus.junit.jupiter;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusInstanceManager;
import com.consol.citrus.annotations.CitrusAnnotations;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.junit.jupiter.spring.CitrusSpringExtension;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

/**
 * JUnit5 extension supports Citrus Xml test extension that also allows to load test cases from external Spring configuration files. In addition to that Citrus annotation based resource injection
 * and lifecycle management such as before/after suite is supported.
 *
 * Extension resolves method parameter of type {@link TestContext} and injects endpoints and resources coming from Citrus Spring application context that is automatically loaded at suite start up.
 * After suite automatically includes Citrus report generation.
 *
 * @author Christoph Deppisch
 * @deprecated in favor of using {@link CitrusSpringExtension}
 */
@Deprecated
public class CitrusBaseExtension implements BeforeAllCallback, TestInstancePostProcessor {

    /** Test suite name */
    private static final String SUITE_NAME = "citrus-junit5-suite";

    private static boolean beforeSuite = true;
    private static boolean afterSuite = true;

    /**
     * {@link ExtensionContext.Namespace} in which Citrus related objects are stored keyed by test class.
     */
    public static final ExtensionContext.Namespace NAMESPACE = CitrusExtension.NAMESPACE;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        CitrusExtensionHelper.setCitrus(getCitrus(), extensionContext);

        if (beforeSuite) {
            beforeSuite = false;
            CitrusExtensionHelper.getCitrus(extensionContext).beforeSuite(SUITE_NAME);
        }

        if (afterSuite) {
            afterSuite = false;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> CitrusExtensionHelper.getCitrus(extensionContext).afterSuite(SUITE_NAME)));
        }
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) throws Exception {
        CitrusAnnotations.injectCitrusFramework(testInstance, CitrusExtensionHelper.getCitrus(extensionContext));
    }

    /**
     * Initialize and get Citrus instance.
     * @return
     */
    protected Citrus getCitrus() {
        return CitrusInstanceManager.getOrDefault();
    }
}
