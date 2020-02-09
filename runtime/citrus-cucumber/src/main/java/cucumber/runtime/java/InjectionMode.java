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

package cucumber.runtime.java;

import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.runner.TestRunner;
import org.springframework.util.ReflectionUtils;


/**
 * Enum with mode selection for test designer or runner.
 *
 * @author Christoph Deppisch
 * @since 2.6
 */
public enum InjectionMode {
    UNDEFINED,
    DESIGNER,
    RUNNER;

    /**
     * Analyse class fields to require test designer or test runner. Returns designer mode or runner mode only
     * if class is explicitly requiring one of those exclusively. If class is using both designer and runner
     * return mode as is.
     * @param clazz
     * @return
     */
    public static InjectionMode analyseMode(Class<?> clazz, InjectionMode fallback) {
        final Boolean[] designerMode = { false };
        final Boolean[] runnerMode = { false };

        ReflectionUtils.doWithFields(clazz, field -> {
            Class<?> type = field.getType();
            if (TestDesigner.class.isAssignableFrom(type)) {
                designerMode[0] = true;
            } else if (TestRunner.class.isAssignableFrom(type)) {
                runnerMode[0] = true;
            }
        }, field -> field.isAnnotationPresent(CitrusResource.class));

        return designerMode[0].equals(runnerMode[0]) ? fallback : (designerMode[0] ? InjectionMode.DESIGNER : InjectionMode.RUNNER);
    }
}
