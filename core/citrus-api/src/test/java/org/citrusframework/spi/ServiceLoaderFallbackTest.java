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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ServiceLoaderFallbackTest {

    @Test
    public void testResolveByNameViaServiceLoader() {
        ResourcePathTypeResolver resolver = new ResourcePathTypeResolver(
                "META-INF/citrus/nonexistent/path", MockService.class);

        MockService service = resolver.resolve("fooService");
        Assertions.assertNotNull(service);
        Assertions.assertEquals(FooService.class, service.getClass());
        Assertions.assertEquals("fooService", service.getName());
    }

    @Test
    public void testResolveByNameViaServiceLoaderBarService() {
        ResourcePathTypeResolver resolver = new ResourcePathTypeResolver(
                "META-INF/citrus/nonexistent/path", MockService.class);

        MockService service = resolver.resolve("barService");
        Assertions.assertNotNull(service);
        Assertions.assertEquals(BarService.class, service.getClass());
        Assertions.assertEquals("barService", service.getName());
    }

    @Test
    public void testResolveByNameNotFoundInServiceLoader() {
        ResourcePathTypeResolver resolver = new ResourcePathTypeResolver(
                "META-INF/citrus/nonexistent/path", MockService.class);

        assertThrows(CitrusRuntimeException.class, () -> resolver.resolve("unknownService"));
    }

    @Test
    public void testResolveAllViaServiceLoader() {
        ResourcePathTypeResolver resolver = new ResourcePathTypeResolver(
                "META-INF/citrus/nonexistent/path", MockService.class);

        Map<String, MockService> services = resolver.resolveAll();
        Assertions.assertNotNull(services);
        Assertions.assertTrue(services.containsKey("fooService"));
        Assertions.assertTrue(services.containsKey("barService"));
        Assertions.assertEquals(FooService.class, services.get("fooService").getClass());
        Assertions.assertEquals(BarService.class, services.get("barService").getClass());
    }

    @Test
    public void testResourcePathTakesPrecedenceOverServiceLoader() {
        ResourcePathTypeResolver resolver = new ResourcePathTypeResolver(
                "META-INF/mocks", MockService.class);

        Map<String, MockService> services = resolver.resolveAll();
        Assertions.assertNotNull(services);
        Assertions.assertTrue(services.containsKey("foo"), "Resource path result should be present");
        Assertions.assertTrue(services.containsKey("bar"), "Resource path result should be present");
        Assertions.assertTrue(services.containsKey("fooService"), "ServiceLoader result should be merged");
        Assertions.assertTrue(services.containsKey("barService"), "ServiceLoader result should be merged");
    }

    @Test
    public void testResolveWithoutServiceTypeDoesNotFallback() {
        ResourcePathTypeResolver resolver = new ResourcePathTypeResolver(
                "META-INF/citrus/nonexistent/path");

        Map<String, Object> services = resolver.resolveAll();
        Assertions.assertNotNull(services);
        Assertions.assertTrue(services.isEmpty());
    }

    @Test
    public void testNamedServiceAnnotation() {
        NamedService annotation = FooService.class.getAnnotation(NamedService.class);
        Assertions.assertNotNull(annotation);
        Assertions.assertEquals("fooService", annotation.name());

        annotation = BarService.class.getAnnotation(NamedService.class);
        Assertions.assertNotNull(annotation);
        Assertions.assertEquals("barService", annotation.name());
    }
}
