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

package org.citrusframework.testng.spring;

import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.ResolvedTestSource;
import org.citrusframework.common.TestSourceResolver;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.internal.MethodInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * TestNG method interceptor duplicates method instances for each package scan test and test name in
 * Citrus annotated test method. So TestNG test is executed multiple times respectively for each Citrus test
 * constructed by method annotation.
 *
 * @since 1.3.1
 */
public class TestNGCitrusSpringMethodInterceptor implements IMethodInterceptor {

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        List<IMethodInstance> interceptedMethods = new ArrayList<>();

        for (IMethodInstance method : methods) {
            boolean baseMethodAdded = false;
            if (method.getInstance() instanceof TestNGCitrusSpringSupport) {
                java.lang.reflect.Method testMethod = method.getMethod().getConstructorOrMethod().getMethod();
                CitrusTestSource citrusTestAnnotation = testMethod.getAnnotation(CitrusTestSource.class);
                if (citrusTestAnnotation != null) {
                    List<ResolvedTestSource> resolvedTestSources = TestSourceResolver.resolve(citrusTestAnnotation, testMethod);
                    if (resolvedTestSources.size() > 1) {
                        for (int i = 0; i < resolvedTestSources.size(); i++) {
                            if (i == 0) {
                                baseMethodAdded = true;
                                interceptedMethods.add(method);
                            } else {
                                interceptedMethods.add(new MethodInstance(method.getMethod()));
                            }
                        }
                    }
                }
            }

            if (!baseMethodAdded) {
                interceptedMethods.add(method);
            }
        }

        return interceptedMethods;
    }
}
