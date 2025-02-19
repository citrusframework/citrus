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

package org.citrusframework.kubernetes.functions;

import java.util.Arrays;
import java.util.Collections;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.functions.DefaultFunctionLibrary;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.kubernetes.ClusterType;
import org.citrusframework.kubernetes.KubernetesVariableNames;
import org.citrusframework.kubernetes.UnitTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ResolveServiceUrlFunctionTest extends UnitTestSupport {

    private final ResolveServiceUrlFunction function = new ResolveServiceUrlFunction();

    @Test
    public void shouldResolveService() {
        TestContext context = TestContextFactory.newInstance().getObject();

        context.setVariable(KubernetesVariableNames.NAMESPACE.value(), "default");

        Assert.assertEquals(function.execute(Collections.singletonList("test-service"), context), "http://test-service.default");
        Assert.assertEquals(function.execute(Arrays.asList("test-service", "8080"), context), "http://test-service.default");
    }

    @Test
    public void shouldResolveSecureService() {
        TestContext context = TestContextFactory.newInstance().getObject();

        context.setVariable(KubernetesVariableNames.NAMESPACE.value(), "default");

        Assert.assertEquals(function.execute(Arrays.asList("test-service", "TRUE"), context), "https://test-service.default");
        Assert.assertEquals(function.execute(Arrays.asList("test-service", "8080", "TRUE"), context), "https://test-service.default");
    }

    @Test
    public void shouldResolveLocalService() {
        try {
            System.setProperty("citrus.kubernetes.cluster.type", ClusterType.LOCAL.name());
            TestContext context = TestContextFactory.newInstance().getObject();

            context.getReferenceResolver().bind("test-service", new HttpServerBuilder()
                    .autoStart(false)
                    .port(8888)
                    .build());

            Assert.assertEquals(function.execute(Collections.singletonList("test-service"), context),"http://localhost:8888");
            Assert.assertEquals(function.execute(Collections.singletonList("foo-service"), context), "http://localhost");
            Assert.assertEquals(function.execute(Arrays.asList("foo-service", "8080"), context), "http://localhost:8080");
        } finally {
            System.setProperty("citrus.kubernetes.cluster.type", ClusterType.KUBERNETES.name());
        }
    }

    @Test
    public void shouldResolve() {
        Assert.assertNotNull(new DefaultFunctionLibrary().getFunction("resolveServiceUrl"));
    }
}
