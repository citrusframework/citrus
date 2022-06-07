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

package com.consol.citrus.groovy;

import java.io.File;
import java.io.IOException;

import com.consol.citrus.Citrus;
import com.consol.citrus.DefaultTestCaseRunner;
import com.consol.citrus.TestCase;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.annotations.CitrusFramework;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.common.TestLoader;
import com.consol.citrus.common.TestSourceAware;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.groovy.dsl.GroovyShellUtils;
import com.consol.citrus.groovy.dsl.test.TestCaseScript;
import com.consol.citrus.util.FileUtils;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 */
public class GroovyTestLoader implements TestLoader, TestSourceAware {

    @CitrusFramework
    private Citrus citrus;

    @CitrusResource
    private TestContext context;

    @CitrusResource
    private TestCaseRunner runner;
    private TestCase testCase;

    private Class<?> testClass;
    private String testName;
    private String packageName;

    private String source;

    public TestCase load() {
        if (testCase != null) {
            return testCase;
        }

        boolean shouldFinish = false;

        if (runner == null) {
            if (context == null) {
                if (citrus == null) {
                    throw new CitrusRuntimeException("Missing Citrus framework instance for loading Groovy test");
                }

                context = citrus.getCitrusContext().createTestContext();
            }

            runner = new DefaultTestCaseRunner(context);

            runner.start();
            shouldFinish = true;
        }

        if (testClass == null) {
            testClass = runner.getTestCase().getTestClass();
        } else {
            runner.testClass(testClass);
        }

        if (testName == null) {
            testName = runner.getTestCase().getName();
        } else {
            runner.name(testName);
        }

        if (packageName == null) {
            packageName = runner.getTestCase().getPackageName();
        } else {
            runner.packageName(packageName);
        }

        try {
            Resource scriptSource = FileUtils.getFileResource(this.getSource(), context);
            ImportCustomizer ic = new ImportCustomizer();

            String basePath = scriptSource.getFile().getParent();
            if (scriptSource instanceof ClassPathResource) {
                basePath = FileUtils.getBasePath(((ClassPathResource) scriptSource).getPath());
            }

            GroovyShellUtils.run(ic, new TestCaseScript(citrus, runner, context, basePath), FileUtils.readToString(scriptSource), citrus, context);
            testCase = runner.getTestCase();
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to load Groovy test source", e);
        } finally {
            if (shouldFinish) {
                runner.stop();
            }
        }

        return testCase;
    }

    public String getSource() {
        if (StringUtils.hasText(this.source)) {
            return this.source;
        } else {
            String path = packageName.replace('.', File.separatorChar);
            String fileName = testName.endsWith(".groovy") ? testName : testName + ".groovy";
            return "classpath:" + path + File.separator + fileName;
        }
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public void setTestClass(Class<?> testClass) {
        this.testClass = testClass;
    }

    @Override
    public void setTestName(String testName) {
        this.testName = testName;
    }

    @Override
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
