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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.citrusframework.TestClass;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class ClassPathTestScanner extends AbstractTestScanner {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(ClassPathTestScanner.class);

    /** Test annotation marking test classes and methods */
    private final Class<? extends Annotation> annotationType;

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
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    ClassUtils.convertClassNameToResourcePath(packageName) + "/**/*.class";

            Resource[] resources = resolver.getResources(packageSearchPath);

            List<String> classes = new ArrayList<>();
            for (Resource resource : resources) {
                if (!resource.isReadable()) {
                    continue;
                }

                MetadataReader metadataReader = new SimpleMetadataReaderFactory().getMetadataReader(resource);
                if (isIncluded(metadataReader.getClassMetadata())) {
                    classes.add(metadataReader.getClassMetadata().getClassName());
                }
            }

            return classes.stream()
                    .distinct()
                    .map(TestClass::new)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new CitrusRuntimeException(String.format("Failed to scan classpath package '%s'", packageName), e);
        }
    }

    protected boolean isIncluded(ClassMetadata metadata) {
        if (!isIncluded(metadata.getClassName())) {
            return false;
        }

        try {
            Class<?> clazz = Class.forName(metadata.getClassName());
            if (clazz.isAnnotationPresent(annotationType)) {
                return true;
            }

            AtomicBoolean hasTestMethod = new AtomicBoolean(false);
            ReflectionUtils.doWithMethods(clazz, method -> hasTestMethod.set(true), method -> AnnotationUtils.findAnnotation(method, annotationType) != null);
            return hasTestMethod.get();
        } catch (NoClassDefFoundError | ClassNotFoundException e) {
            LOG.warn("Unable to access class: " + metadata.getClassName());
            return false;
        }
    }
}
