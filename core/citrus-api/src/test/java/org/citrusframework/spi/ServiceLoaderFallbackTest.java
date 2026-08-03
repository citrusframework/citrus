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

package org.citrusframework.spi;

import java.util.Map;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.mocks.BarService;
import org.citrusframework.spi.mocks.FooService;
import org.citrusframework.spi.mocks.MockService;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ServiceLoaderFallbackTest {

    @Test
    public void testResolveByNameViaServiceLoader() {
        ResourcePathTypeResolver resolver = new ResourcePathTypeResolver(
                "META-INF/citrus/nonexistent/path", MockService.class);

        MockService service = resolver.resolve("fooService");
        Assert.assertNotNull(service);
        Assert.assertEquals(service.getClass(), FooService.class);
        Assert.assertEquals(service.getName(), "fooService");
    }

    @Test
    public void testResolveByNameViaServiceLoaderBarService() {
        ResourcePathTypeResolver resolver = new ResourcePathTypeResolver(
                "META-INF/citrus/nonexistent/path", MockService.class);

        MockService service = resolver.resolve("barService");
        Assert.assertNotNull(service);
        Assert.assertEquals(service.getClass(), BarService.class);
        Assert.assertEquals(service.getName(), "barService");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testResolveByNameNotFoundInServiceLoader() {
        ResourcePathTypeResolver resolver = new ResourcePathTypeResolver(
                "META-INF/citrus/nonexistent/path", MockService.class);

        resolver.resolve("unknownService");
    }

    @Test
    public void testResolveAllViaServiceLoader() {
        ResourcePathTypeResolver resolver = new ResourcePathTypeResolver(
                "META-INF/citrus/nonexistent/path", MockService.class);

        Map<String, MockService> services = resolver.resolveAll();
        Assert.assertNotNull(services);
        Assert.assertTrue(services.containsKey("fooService"));
        Assert.assertTrue(services.containsKey("barService"));
        Assert.assertEquals(services.get("fooService").getClass(), FooService.class);
        Assert.assertEquals(services.get("barService").getClass(), BarService.class);
    }

    @Test
    public void testResourcePathTakesPrecedenceOverServiceLoader() {
        ResourcePathTypeResolver resolver = new ResourcePathTypeResolver(
                "META-INF/mocks", MockService.class);

        Map<String, MockService> services = resolver.resolveAll();
        Assert.assertNotNull(services);
        Assert.assertTrue(services.containsKey("foo"), "Resource path result should be present");
        Assert.assertTrue(services.containsKey("bar"), "Resource path result should be present");
        Assert.assertTrue(services.containsKey("fooService"), "ServiceLoader result should be merged");
        Assert.assertTrue(services.containsKey("barService"), "ServiceLoader result should be merged");
    }

    @Test
    public void testResolveWithoutServiceTypeDoesNotFallback() {
        ResourcePathTypeResolver resolver = new ResourcePathTypeResolver(
                "META-INF/citrus/nonexistent/path");

        Map<String, Object> services = resolver.resolveAll();
        Assert.assertNotNull(services);
        Assert.assertTrue(services.isEmpty());
    }

    @Test
    public void testNamedServiceAnnotation() {
        NamedService annotation = FooService.class.getAnnotation(NamedService.class);
        Assert.assertNotNull(annotation);
        Assert.assertEquals(annotation.name(), "fooService");

        annotation = BarService.class.getAnnotation(NamedService.class);
        Assert.assertNotNull(annotation);
        Assert.assertEquals(annotation.name(), "barService");
    }
}
