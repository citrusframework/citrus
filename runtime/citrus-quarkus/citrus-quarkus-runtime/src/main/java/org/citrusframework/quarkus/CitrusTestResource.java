/*
 *  Copyright 2023 the original author or authors.
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements. See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.citrusframework.quarkus;

import java.util.Collections;
import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.citrusframework.Citrus;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.TestCaseRunnerFactory;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;

/**
 * Quarkus test resource that takes care of injecting Citrus resources
 * such as TestContext, TestCaseRunner, CitrusEndpoints and many more.
 *
 * @author Christoph Deppisch
 */
public class CitrusTestResource implements QuarkusTestResourceLifecycleManager {

    private Citrus citrus;

    private TestCaseRunner runner;

    private TestContext context;

    @Override
    public Map<String, String> start() {
        if (citrus == null) {
            citrus = CitrusInstanceManager.newInstance();
            citrus.beforeSuite("citrus-quarkus");
        }

        return Collections.emptyMap();
    }

    @Override
    public void stop() {
        if (runner != null) {
            runner.stop();
        }

        citrus.afterSuite("citrus-quarkus");

        runner = null;
        context = null;
    }

    @Override
    public void inject(Object testInstance) {
        if (runner != null) {
            runner.stop();
        }

        context = citrus.getCitrusContext().createTestContext();
        runner = TestCaseRunnerFactory.createRunner(context);
        runner.testClass(testInstance.getClass());
        runner.packageName(testInstance.getClass().getPackageName());
        runner.name(testInstance.getClass().getSimpleName());
        runner.start();

        citrus.getCitrusContext().parseConfiguration(testInstance);
        CitrusAnnotations.injectEndpoints(testInstance, context);
    }

    @Override
    public void inject(TestInjector testInjector) {
        testInjector.injectIntoFields(citrus, new TestInjector.AnnotatedAndMatchesType(CitrusFramework.class, Citrus.class));
        testInjector.injectIntoFields(runner, new TestInjector.AnnotatedAndMatchesType(CitrusResource.class, TestActionRunner.class));
        testInjector.injectIntoFields(runner, new TestInjector.AnnotatedAndMatchesType(CitrusResource.class, GherkinTestActionRunner.class));
        testInjector.injectIntoFields(runner, new TestInjector.AnnotatedAndMatchesType(CitrusResource.class, TestCaseRunner.class));
        testInjector.injectIntoFields(context, new TestInjector.AnnotatedAndMatchesType(CitrusResource.class, TestContext.class));
    }
}
