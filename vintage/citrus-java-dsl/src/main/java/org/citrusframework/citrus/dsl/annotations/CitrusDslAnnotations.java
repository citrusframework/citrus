/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.citrus.dsl.annotations;

import org.citrusframework.citrus.annotations.CitrusResource;
import org.citrusframework.citrus.dsl.design.TestDesigner;
import org.citrusframework.citrus.dsl.runner.TestRunner;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public abstract class CitrusDslAnnotations {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CitrusDslAnnotations.class);

    /**
     * Prevent instantiation.
     */
    private CitrusDslAnnotations() {
        super();
    }

    /**
     * Inject test designer instance to the test class fields with {@link CitrusResource} annotation.
     *
     * @param target
     * @param designer
     */
    public static void injectTestDesigner(final Object target, final TestDesigner designer) {
        ReflectionUtils.doWithFields(target.getClass(), field -> {
            Class<?> type = field.getType();
            if (TestDesigner.class.isAssignableFrom(type)) {
                log.debug(String.format("Injecting test designer instance on test class field '%s'", field.getName()));
                ReflectionUtils.setField(field, target, designer);
            } else {
                throw new CitrusRuntimeException("Not able to provide a Citrus resource injection for type " + type);
            }
        }, field -> {
            if (field.isAnnotationPresent(CitrusResource.class) && TestDesigner.class.isAssignableFrom(field.getType())) {
                if (!field.isAccessible()) {
                    ReflectionUtils.makeAccessible(field);
                }

                return true;
            }

            return false;
        });
    }

    /**
     * Inject test designer instance to the test class fields with {@link CitrusResource} annotation.
     *
     * @param target
     * @param runner
     */
    public static void injectTestRunner(final Object target, final TestRunner runner) {
        ReflectionUtils.doWithFields(target.getClass(), field -> {
            Class<?> type = field.getType();
            if (TestRunner.class.isAssignableFrom(type)) {
                log.debug(String.format("Injecting test runner instance on test class field '%s'", field.getName()));
                ReflectionUtils.setField(field, target, runner);
            } else {
                throw new CitrusRuntimeException("Not able to provide a Citrus resource injection for type " + type);
            }
        }, field -> {
            if (field.isAnnotationPresent(CitrusResource.class) && TestRunner.class.isAssignableFrom(field.getType())) {
                if (!field.isAccessible()) {
                    ReflectionUtils.makeAccessible(field);
                }

                return true;
            }

            return false;
        });
    }
}
