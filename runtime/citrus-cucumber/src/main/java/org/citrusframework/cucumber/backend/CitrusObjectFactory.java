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

package org.citrusframework.cucumber.backend;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.exception.CucumberException;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.TestCaseRunnerFactory;
import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.context.TestContext;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class CitrusObjectFactory implements ObjectFactory {

    /** Test runner */
    private TestCaseRunner runner;

    /** Test context */
    private TestContext context;

    /** Static self reference */
    private static CitrusObjectFactory selfReference;

    /** In memory cache for created instances */
    private final Map<Class<?>, Object> instances = new HashMap<>();

    /**
     * Default constructor with static self reference initialization.
     */
    public CitrusObjectFactory() {
        selfReference = this;
    }

    @Override
    public void start() {
        context = CitrusInstanceManager.getOrDefault().getCitrusContext().createTestContext();
        runner = TestCaseRunnerFactory.createRunner(context);
    }

    @Override
    public void stop() {
        instances.clear();
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        if (CitrusObjectFactory.class.isAssignableFrom(type)) {
            return (T) this;
        }

        T instance = type.cast(Optional.ofNullable(instances.get(type))
                                        .orElseGet(() -> this.createNewInstance(type)));
        CitrusAnnotations.injectAll(instance, CitrusInstanceManager.getOrDefault(), context);
        CitrusAnnotations.injectTestRunner(instance, runner);

        return instance;
    }

    private <T> T createNewInstance(Class<T> type) {
        try {
            Constructor<T> constructor = type.getConstructor();
            T instance = constructor.newInstance();
            instances.put(type, instance);
            return instance;
        } catch (NoSuchMethodException e) {
            throw new CucumberException(String.format("%s doesn't have an empty constructor", type), e);
        } catch (Exception e) {
            throw new CucumberException(String.format("Failed to instantiate %s", type), e);
        }
    }

    @Override
    public boolean addClass(Class<?> glueClass) {
        return true;
    }

    /**
     * Static access to self reference.
     * @return
     */
    public static CitrusObjectFactory instance() throws IllegalAccessException {
        if (selfReference == null) {
            throw new IllegalAccessException("Illegal access to self reference - not available yet");
        }

        return selfReference;
    }
}
