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

package org.citrusframework.quarkus;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.quarkus.test.common.QuarkusTestResourceConfigurableLifecycleManager;
import org.citrusframework.Citrus;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.TestCaseRunnerFactory;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.annotations.CitrusFramework;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Quarkus test resource that takes care of injecting Citrus resources
 * such as TestContext, TestCaseRunner, CitrusEndpoints and many more.
 */
public class CitrusTestResource implements QuarkusTestResourceConfigurableLifecycleManager<CitrusSupport> {

    private Citrus citrus;

    private TestCaseRunner runner;

    private TestContext context;

    private final Set<ApplicationPropertiesSupplier> applicationPropertiesSupplier = new HashSet<>();

    @Override
    public void init(CitrusSupport config) {
        for (Class<? extends ApplicationPropertiesSupplier> supplierType : config.applicationPropertiesSupplier()) {
            try {
                registerApplicationPropertiesSupplier(supplierType.getDeclaredConstructor().newInstance());
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                throw new CitrusRuntimeException("Failed to instantiate application properties supplier from type: %s"
                        .formatted(supplierType), e);
            }
        }
    }

    @Override
    public void init(Map<String, String> initArgs) {
        String[] qualifiedClassNames = initArgs.getOrDefault(ApplicationPropertiesSupplier.INIT_ARG, "").split(",");
        for (String qualifiedClassName : qualifiedClassNames) {
            try {
                Class<?> cls = Class.forName(qualifiedClassName, true, Thread.currentThread().getContextClassLoader());
                Object instance = cls.getDeclaredConstructor().newInstance();
                if (instance instanceof ApplicationPropertiesSupplier supplier) {
                    applicationPropertiesSupplier.add(supplier);
                }
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                     IllegalAccessException | NoSuchMethodException e) {
                throw new CitrusRuntimeException("Failed to instantiate application property supplier from type: %s"
                        .formatted(qualifiedClassName), e);
            }
        }
    }

    @Override
    public Map<String, String> start() {
        if (citrus == null) {
            citrus = CitrusInstanceManager.newInstance();
            citrus.beforeSuite("citrus-quarkus");
        }

        Map<String, String> applicationProperties = new HashMap<>();
        applicationPropertiesSupplier.forEach(supplier -> applicationProperties.putAll(supplier.get()));
        return applicationProperties;
    }

    @Override
    public void stop() {
        if (runner != null) {
            runner.stop();
        }

        citrus.afterSuite("citrus-quarkus");

        runner = null;
        context = null;
        applicationPropertiesSupplier.clear();
    }

    @Override
    public void inject(Object testInstance) {
        if (runner != null) {
            runner.stop();
        }

        context = citrus.getCitrusContext().createTestContext();
        runner = TestCaseRunnerFactory.createRunner(context);
        runner.testClass(testInstance.getClass());
        runner.packageName(testInstance.getClass().getPackageName());
        runner.name(testInstance.getClass().getSimpleName());
        runner.start();

        citrus.getCitrusContext().parseConfiguration(testInstance);
        CitrusAnnotations.injectEndpoints(testInstance, context);
    }

    @Override
    public void inject(TestInjector testInjector) {
        testInjector.injectIntoFields(citrus, new TestInjector.AnnotatedAndMatchesType(CitrusFramework.class, Citrus.class));
        testInjector.injectIntoFields(runner, new TestInjector.AnnotatedAndMatchesType(CitrusResource.class, TestActionRunner.class));
        testInjector.injectIntoFields(runner, new TestInjector.AnnotatedAndMatchesType(CitrusResource.class, GherkinTestActionRunner.class));
        testInjector.injectIntoFields(runner, new TestInjector.AnnotatedAndMatchesType(CitrusResource.class, TestCaseRunner.class));
        testInjector.injectIntoFields(context, new TestInjector.AnnotatedAndMatchesType(CitrusResource.class, TestContext.class));
    }

    /**
     * Add new application properties supplier.
     * @param supplier
     */
    public void registerApplicationPropertiesSupplier(ApplicationPropertiesSupplier supplier) {
        applicationPropertiesSupplier.add(supplier);
    }

}
