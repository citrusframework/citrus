/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.groovy;

import java.io.IOException;

import org.citrusframework.TestSource;
import org.citrusframework.common.DefaultTestLoader;
import org.citrusframework.common.TestLoader;
import org.citrusframework.common.TestSourceAware;
import org.citrusframework.groovy.dsl.GroovyShellUtils;
import org.citrusframework.groovy.dsl.test.TestCaseScript;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

public class GroovyTestLoader extends DefaultTestLoader implements TestSourceAware {

    private TestSource source;

    protected void doLoad() {
        try {
            Resource scriptSource = getSource().getSourceFile(context);
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
            throw context.handleError(testName, packageName, "Failed to load Groovy test source '" + testName + "'", e);
        }
    }

    public TestSource getSource() {
        if (source != null) {
            return source;
        } else {
            String path = packageName.replace('.', '/');
            String fileName = testName.endsWith(FileUtils.FILE_EXTENSION_GROOVY) ? testName : testName + FileUtils.FILE_EXTENSION_GROOVY;
            return new TestSource(TestLoader.GROOVY, testName, Resources.CLASSPATH_RESOURCE_PREFIX + path + "/" + fileName);
        }
    }

    public void setSource(TestSource source) {
        this.source = source;
    }

    public GroovyTestLoader source(String sourceFile) {
        setSource(new TestSource(TestLoader.GROOVY, FileUtils.getBaseName(FileUtils.getFileName(sourceFile)), sourceFile));
        return this;
    }
}
