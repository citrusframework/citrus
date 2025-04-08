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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.citrusframework.TestClass;
import org.citrusframework.TestSource;
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

    private static final Pattern packageNamePattern = Pattern.compile("^package\\s+([a-zA-Z_][.a-zA-Z_]+);$", Pattern.MULTILINE);
    private TestSource source;

    @Override
    public void doLoad() {
        try {
            Resource javaSource = getSource().getSourceFile();
            // Compile source file.
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            int success = compiler.run(null, null, null, javaSource.getFile().getAbsolutePath());
            if (success != 0) {
                throw new CitrusRuntimeException("Failed to compile Java source file: %s".formatted(javaSource.getFile().getAbsolutePath()));
            }

            String packageName = extractPackageName(javaSource);
            String qualifiedClassName = StringUtils.hasText(packageName) ? packageName + "." + getClassName() : getClassName();

            // Load and instantiate compiled class.
            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { getClassLoaderBaseURL(packageName, javaSource) });
            Class<?> cls = Class.forName(qualifiedClassName, true, classLoader);
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
     * Gets custom source file for the Java test case. If not set creates default
     * context file path from testName and packageName.
     */
    public TestSource getSource() {
        if (source != null) {
            return source;
        } else {
            String path = StringUtils.hasText(packageName) ? packageName.replace('.', '/') : "";
            String fileName = testName.endsWith(FileUtils.FILE_EXTENSION_JAVA) ? testName : testName + FileUtils.FILE_EXTENSION_JAVA;
            return new TestSource(TestLoader.JAVA, testName, StringUtils.hasText(path) ? path + "/" + fileName : fileName);
        }
    }

    public String getClassName() {
        if (source != null) {
            if (source instanceof TestClass clazz) {
                return clazz.getName();
            }

            String filePath;
            if (StringUtils.hasText(source.getFilePath())) {
                filePath = source.getFilePath();
            } else {
                filePath = source.getSourceFile().getLocation();
            }

            if (filePath.contains(":")) {
                return FileUtils.getBaseName(FileUtils.getFileName(filePath.substring(filePath.indexOf(":"))));
            }

            return FileUtils.getBaseName(FileUtils.getFileName(filePath));
        }

        return FileUtils.getBaseName(testName.endsWith(FileUtils.FILE_EXTENSION_JAVA) ? testName : testName + FileUtils.FILE_EXTENSION_JAVA);
    }

    private static String extractPackageName(Resource javaSource) throws IOException {
        String content = FileUtils.readToString(javaSource);
        Matcher matcher = packageNamePattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }

    /**
     * Get Class loader base URL for given Java file resource path.
     * The package name of the class must be taken into account when walking the parent tree of the folder structure upwards.
     * @param packageName
     * @param javaSource
     * @return
     * @throws MalformedURLException
     */
    private static URL getClassLoaderBaseURL(String packageName, Resource javaSource) throws MalformedURLException {
        Path clBase = Paths.get(javaSource.getURI()).getParent();

        if (StringUtils.hasText(packageName)) {
            for (int i = 0; i < packageName.split("\\.").length; i++) {
                clBase = clBase.getParent();
            }
        }

        return clBase.toUri().toURL();
    }

    /**
     * Sets custom source file for Java test case.
     */
    @Override
    public void setSource(TestSource source) {
        this.source = source;
    }
}
