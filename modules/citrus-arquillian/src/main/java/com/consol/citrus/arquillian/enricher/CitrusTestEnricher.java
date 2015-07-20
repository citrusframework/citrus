/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.arquillian.enricher;

import com.consol.citrus.Citrus;
import com.consol.citrus.annotations.CitrusFramework;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.design.DefaultTestDesigner;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.runner.DefaultTestRunner;
import com.consol.citrus.dsl.runner.TestRunner;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Test enricher works on Citrus annotated test class fields and test methods. Injects
 * Citrus framework instance as well as Citrus test instances to Arquillian test methods.
 *
 * @author Christoph Deppisch
 * @since 2.2
 */
public class CitrusTestEnricher implements TestEnricher {

    @Inject
    private Instance<Citrus> citrusInstance;

    @Override
    public void enrich(final Object testCase) {
        ReflectionUtils.doWithFields(testCase.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                ReflectionUtils.setField(field, testCase, citrusInstance.get());
            }
        }, new ReflectionUtils.FieldFilter() {
            @Override
            public boolean matches(Field field) {
                if (field.isAnnotationPresent(CitrusFramework.class) &&
                        Citrus.class.isAssignableFrom(field.getType())) {
                    if (!field.isAccessible()) {
                        ReflectionUtils.makeAccessible(field);
                    }

                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public Object[] resolve(Method method) {
        Object[] values = new Object[method.getParameterTypes().length];
        Class<?>[] parameterTypes = method.getParameterTypes();

        for (int i = 0; i < parameterTypes.length; i++) {
            final Annotation[] parameterAnnotations = method.getParameterAnnotations()[i];
            for (Annotation annotation : parameterAnnotations) {
                if (annotation instanceof CitrusResource) {
                    Class<?> type = parameterTypes[i];
                    if (TestDesigner.class.isAssignableFrom(type)) {
                        TestDesigner testDesigner = new DefaultTestDesigner(citrusInstance.get().getApplicationContext());
                        testDesigner.name(method.getDeclaringClass().getSimpleName() + "." + method.getName());

                        values[i] = testDesigner;
                    } else if (TestRunner.class.isAssignableFrom(type)) {
                        TestRunner testRunner = new DefaultTestRunner(citrusInstance.get().getApplicationContext());
                        testRunner.name(method.getDeclaringClass().getSimpleName() + "." + method.getName());

                        values[i] = testRunner;
                    } else {
                        throw new RuntimeException("Not able to provide a Citrus resource injection for type " + type);
                    }
                }
            }
        }

        return values;
    }
}
