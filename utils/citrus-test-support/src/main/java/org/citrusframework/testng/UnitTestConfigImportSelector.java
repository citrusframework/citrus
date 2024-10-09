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

package org.citrusframework.testng;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

public class UnitTestConfigImportSelector implements DeferredImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        if (ClassUtils.isPresent("org.citrusframework.config.ComponentLifecycleProcessor", getClass().getClassLoader())) {
            return new String[]{
                    DefaultUnitTestConfig.class.getName(),
                    ComponentLifecycleConfig.class.getName()
            };
        } else {
            return new String[]{
                    DefaultUnitTestConfig.class.getName()
            };
        }
    }

    @Configuration
    @ImportResource(locations = "classpath:org/citrusframework/context/citrus-unit-context.xml")
    public static class DefaultUnitTestConfig {
    }

    @Configuration
    @ImportResource(locations = "classpath:component-lifecycle-context.xml")
    public static class ComponentLifecycleConfig {
    }
}
