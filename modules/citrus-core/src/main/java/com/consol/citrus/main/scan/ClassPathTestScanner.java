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

package com.consol.citrus.main.scan;

import com.consol.citrus.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class ClassPathTestScanner {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(ClassPathTestScanner.class);

    /** Test name patterns to include */
    private final String[] includes;

    /**
     * Default constructor using run configuration.
     * @param includes
     */
    public ClassPathTestScanner(String... includes) {
        this.includes = includes;
    }

    /**
     * Find classes in package suitable to running as test with given annotation.
     * @param packageName
     * @param annotation
     * @return
     */
    public List<TestClass> findTestsInPackage(String packageName, Class<? extends Annotation> annotation) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AbstractClassTestingTypeFilter() {
            @Override
            protected boolean match(ClassMetadata metadata) {
                if (Stream.of(includes)
                        .parallel()
                        .map(Pattern::compile)
                        .noneMatch(pattern -> pattern.matcher(metadata.getClassName()).matches())) {
                    return false;
                }

                try {
                    Class<?> clazz = Class.forName(metadata.getClassName());
                    if (clazz.isAnnotationPresent(annotation)) {
                        return true;
                    }

                    AtomicBoolean hasTestMethod = new AtomicBoolean(false);
                    ReflectionUtils.doWithMethods(clazz, method -> hasTestMethod.set(true), method -> AnnotationUtils.findAnnotation(method, annotation) != null);
                    return hasTestMethod.get();
                } catch (NoClassDefFoundError | ClassNotFoundException e) {
                    log.warn("Unable to access class: " + metadata.getClassName());
                    return false;
                }
            }
        });

        return provider.findCandidateComponents(packageName)
                .stream()
                .map(BeanDefinition::getBeanClassName)
                .distinct()
                .map(TestClass::new)
                .collect(Collectors.toList());
    }
}
