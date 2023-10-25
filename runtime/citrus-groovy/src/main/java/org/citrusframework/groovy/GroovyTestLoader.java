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

package org.citrusframework.groovy;

import java.io.IOException;

import org.citrusframework.common.DefaultTestLoader;
import org.citrusframework.common.TestSourceAware;
import org.citrusframework.groovy.dsl.GroovyShellUtils;
import org.citrusframework.groovy.dsl.test.TestCaseScript;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

/**
 * @author Christoph Deppisch
 */
public class GroovyTestLoader extends DefaultTestLoader implements TestSourceAware {

    private String source;

    protected void doLoad() {
        try {
            Resource scriptSource = FileUtils.getFileResource(this.getSource(), context);
            ImportCustomizer ic = new ImportCustomizer();

            String basePath;
            if (scriptSource instanceof Resources.ClasspathResource) {
                basePath = FileUtils.getBasePath(scriptSource.getLocation());
            } else {
                basePath = scriptSource.getFile().getParent();
            }

            String source = FileUtils.readToString(scriptSource);
            GroovyShellUtils.autoAddImports(source, ic);

            testCase = runner.getTestCase();
            configurer.forEach(it -> it.accept(testCase));
            runner.start();

            GroovyShellUtils.run(ic, new TestCaseScript(citrus, runner, context, basePath), source, citrus, context);

            handler.forEach(it -> it.accept(testCase));
        } catch (IOException e) {
            throw citrusContext.getTestContextFactory().getObject()
                    .handleError(testName, packageName, "Failed to load Groovy test source '" + testName + "'", e);
        }
    }

    public String getSource() {
        if (StringUtils.hasText(this.source)) {
            return this.source;
        } else {
            String path = packageName.replace('.', '/');
            String fileName = testName.endsWith(FileUtils.FILE_EXTENSION_GROOVY) ? testName : testName + FileUtils.FILE_EXTENSION_GROOVY;
            return Resources.CLASSPATH_RESOURCE_PREFIX + path + "/" + fileName;
        }
    }

    public void setSource(String source) {
        this.source = source;
    }

    public GroovyTestLoader source(String source) {
        setSource(source);
        return this;
    }
}
