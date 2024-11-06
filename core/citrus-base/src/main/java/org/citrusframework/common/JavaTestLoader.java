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

package org.citrusframework.common;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.ReflectionHelper;
import org.citrusframework.util.StringUtils;

/**
 * Test loader implementation able to compile a Java source file and run the test method
 */
public class JavaTestLoader extends DefaultTestLoader implements TestSourceAware {

    private String source;

    @Override
    public void doLoad() {
        Resource javaSource = FileUtils.getFileResource(getSource());

        try {
            // Compile source file.
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            int success = compiler.run(null, null, null, javaSource.getFile().getAbsolutePath());
            if (success != 0) {
                throw new CitrusRuntimeException("Failed to compile Java source file: %s".formatted(javaSource.getFile().getAbsolutePath()));
            }

            // Load and instantiate compiled class.
            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { Paths.get(javaSource.getURI()).getParent().toUri().toURL() });
            Class<?> cls = Class.forName(getClassName(), true, classLoader);
            Object instance = cls.getDeclaredConstructor().newInstance();

            CitrusAnnotations.injectAll(instance, citrus, context);
            CitrusAnnotations.injectTestRunner(instance, runner);

            doWithTestCase(tc -> {
                try {
                    ReflectionHelper.invokeMethod(instance.getClass().getDeclaredMethod("run"), instance);
                } catch (NoSuchMethodException e) {
                    throw new CitrusRuntimeException("Failed to run Java test method 'run()' on class: %s".formatted(instance.getClass()), e);
                }
            });

            super.doLoad();
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            throw context.handleError(testName, packageName, "Failed to load Java test with name '" + testName + "'", e);
        }
    }

    /**
     * Gets custom Spring application context file for the Java test case. If not set creates default
     * context file path from testName and packageName.
     * @return
     */
    public String getSource() {
        if (StringUtils.hasText(source)) {
            return source;
        } else {
            String path = StringUtils.hasText(packageName) ? packageName.replace('.', '/') : "";
            String fileName = testName.endsWith(FileUtils.FILE_EXTENSION_JAVA) ? testName : testName + FileUtils.FILE_EXTENSION_JAVA;
            return StringUtils.hasText(path) ? path + "/" + fileName : fileName;
        }
    }

    public String getClassName() {
        if (StringUtils.hasText(source)) {
            if (source.contains(":")) {
                return FileUtils.getBaseName(FileUtils.getFileName(source.substring(source.indexOf(":"))));
            }

            return FileUtils.getBaseName(FileUtils.getFileName(source));
        }

        return FileUtils.getBaseName(testName.endsWith(FileUtils.FILE_EXTENSION_JAVA) ? testName : testName + FileUtils.FILE_EXTENSION_JAVA);
    }

    /**
     * Sets custom Spring application context file for Java test case.
     * @param source
     */
    @Override
    public void setSource(String source) {
        this.source = source;
    }
}
