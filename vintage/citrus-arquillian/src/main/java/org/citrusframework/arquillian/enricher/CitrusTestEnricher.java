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

package org.citrusframework.arquillian.enricher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.citrusframework.Citrus;
import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.arquillian.CitrusExtensionConstants;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test enricher works on Citrus annotated test class fields and test methods. Injects
 * Citrus framework instance as well as Citrus test instances to Arquillian test methods.
 *
 * @author Christoph Deppisch
 * @since 2.2
 */
public class CitrusTestEnricher implements TestEnricher {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(CitrusTestEnricher.class);

    @Inject
    private Instance<Citrus> citrusInstance;

    @Override
    public void enrich(Object testCase) {
        try {
            log.debug("Starting test class field injection for Citrus resources");

            CitrusAnnotations.injectAll(testCase, citrusInstance.get());

            log.info("Enriched test class with Citrus field resource injection");
        } catch (Exception e) {
            log.error(CitrusExtensionConstants.CITRUS_EXTENSION_ERROR, e);
            throw e;
        }
    }

    @Override
    public Object[] resolve(Method method) {
        Object[] values = new Object[method.getParameterTypes().length];
        log.debug("Starting method parameter injection for Citrus resources");

        try {
            Class<?>[] parameterTypes = method.getParameterTypes();
            TestContext context = citrusInstance.get().getCitrusContext().createTestContext();
            for (int i = 0; i < parameterTypes.length; i++) {
                final Annotation[] parameterAnnotations = method.getParameterAnnotations()[i];
                for (Annotation annotation : parameterAnnotations) {
                    if (annotation instanceof CitrusResource) {
                        Class<?> type = parameterTypes[i];
                        if (TestCaseRunner.class.isAssignableFrom(type)) {
                            TestCaseRunner testRunner = new DefaultTestCaseRunner(context);
                            testRunner.name(method.getDeclaringClass().getSimpleName() + "." + method.getName());

                            log.debug("Injecting Citrus test runner on method parameter");
                            values[i] = testRunner;
                        } else if (TestContext.class.isAssignableFrom(type)) {
                            log.debug("Injecting Citrus test context on method parameter");
                            values[i] = context;
                        } else {
                            throw new CitrusRuntimeException("Not able to provide a Citrus resource injection for type " + type);
                        }
                    }
                }
            }

            log.info("Enriched method parameters with Citrus method resource injection");
        } catch (Exception e) {
            log.error(CitrusExtensionConstants.CITRUS_EXTENSION_ERROR, e);
            throw e;
        }

        return values;
    }
}
