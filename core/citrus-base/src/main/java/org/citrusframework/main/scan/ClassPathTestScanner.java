/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.main.scan;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.citrusframework.TestClass;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ClasspathResourceResolver;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.ReflectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class ClassPathTestScanner extends AbstractTestScanner {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(ClassPathTestScanner.class);

    /** Test annotation marking test classes and methods */
    private final Class<? extends Annotation> annotationType;

    /** Classpath resource resolver */
    private final ClasspathResourceResolver resolver = new ClasspathResourceResolver();

    /**
     * Default constructor using run configuration.
     * @param includes
     */
    public ClassPathTestScanner(Class<? extends Annotation> annotationType, String... includes) {
        super(includes);
        this.annotationType = annotationType;
    }

    @Override
    public List<TestClass> findTestsInPackage(String packageName) {
        try {
            Set<Path> resources = resolver.getClasses(packageName);

            List<String> classes = new ArrayList<>();
            for (Path resource : resources) {
                String className = String.format("%s.%s", packageName, FileUtils.getBaseName(String.valueOf(resource.getFileName())));
                if (isIncluded(className)) {
                    classes.add(className);
                }
            }

            return classes.stream()
                    .distinct()
                    .map(TestClass::fromString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new CitrusRuntimeException(String.format("Failed to scan classpath package '%s'", packageName), e);
        }
    }

    protected boolean isIncluded(String className) {
        if (!super.isIncluded(className)) {
            return false;
        }

        try {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(annotationType)) {
                return true;
            }

            AtomicBoolean hasTestMethod = new AtomicBoolean(false);
            ReflectionHelper.doWithMethods(clazz, method -> {
                if (method.getDeclaredAnnotation(annotationType) != null) {
                    hasTestMethod.set(true);
                }
            });
            return hasTestMethod.get();
        } catch (NoClassDefFoundError | ClassNotFoundException e) {
            logger.warn("Unable to access class: " + className);
            return false;
        }
    }
}
