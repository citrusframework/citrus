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

import org.citrusframework.spi.mocks.Bar;
import org.citrusframework.spi.mocks.Foo;
import org.citrusframework.spi.mocks.FooWithParams;
import org.citrusframework.spi.mocks.SingletonFoo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourcePathTypeResolverTest {

    @Test
    public void testResolveProperty() {
        Assertions.assertEquals(Foo.class.getName(), new ResourcePathTypeResolver().resolveProperty("mocks/foo", TypeResolver.DEFAULT_TYPE_PROPERTY));
        Assertions.assertEquals("fooMock", new ResourcePathTypeResolver().resolveProperty("mocks/foo", "name"));
        Assertions.assertEquals("fooMock", new ResourcePathTypeResolver().resolveProperty("META-INF/mocks/foo", "name"));
        Assertions.assertEquals(Foo.class.getName(), new ResourcePathTypeResolver("META-INF/mocks").resolveProperty("foo", TypeResolver.DEFAULT_TYPE_PROPERTY));
        Assertions.assertEquals("fooMock", new ResourcePathTypeResolver("META-INF/mocks").resolveProperty("foo", "name"));
        Assertions.assertEquals("fooMock", new ResourcePathTypeResolver("META-INF/mocks").resolveProperty("META-INF/mocks/foo", "name"));
        Assertions.assertEquals("barMock", new ResourcePathTypeResolver("META-INF/mocks").resolveProperty("bar", "name"));
        Assertions.assertEquals(Foo.class.getName(), new ResourcePathTypeResolver().resolveProperty("mocks/foo", TypeResolver.DEFAULT_TYPE_PROPERTY));
        Assertions.assertEquals(FooWithParams.class, new ResourcePathTypeResolver().resolve("mocksWithParams/fooWithParams", 1,(short)2,3.d,4.f, 'c', true, new int[]{1,2,3},"StringParam").getClass());
    }

    @Test
    public void testResolve() {
        Assertions.assertEquals(Foo.class, new ResourcePathTypeResolver("META-INF/mocks").resolve("foo").getClass());
        Assertions.assertEquals(Bar.class, new ResourcePathTypeResolver("META-INF/mocks").resolve("bar").getClass());
        Assertions.assertEquals(Foo.class, new ResourcePathTypeResolver("META-INF/mocks").resolve("foo", TypeResolver.DEFAULT_TYPE_PROPERTY).getClass());
    }

    @Test
    public void testIsNonCitrusApiJarUrl() {
        Assertions.assertTrue(ResourcePathTypeResolver.isNonCitrusApiJarUrl("file:/path/to/some-library-1.0.0.jar"));
        Assertions.assertTrue(ResourcePathTypeResolver.isNonCitrusApiJarUrl("file:/path/to/some-library-1.0.0.jar!/"));
    }

    @Test
    public void testIsNonCitrusApiJarUrlExcludesCitrusApiJar() {
        Assertions.assertFalse(ResourcePathTypeResolver.isNonCitrusApiJarUrl("file:/path/to/citrus-api-5.0.1.jar"));
        Assertions.assertFalse(ResourcePathTypeResolver.isNonCitrusApiJarUrl("file:/path/to/citrus-api-5.0.1.jar!/"));
        Assertions.assertFalse(ResourcePathTypeResolver.isNonCitrusApiJarUrl("file:/path/to/citrus-api-5.0.1-SNAPSHOT.jar"));
        Assertions.assertFalse(ResourcePathTypeResolver.isNonCitrusApiJarUrl("file:/path/to/citrus-api-5.0.1-SNAPSHOT.jar!/"));
    }

    @Test
    public void testIsNonCitrusApiJarUrlExcludesNestedJarUrls() {
        Assertions.assertFalse(ResourcePathTypeResolver.isNonCitrusApiJarUrl(
                "jar:nested:/path/to/fat.jar/!BOOT-INF/lib/citrus-api-5.0.1.jar!/"));
        Assertions.assertFalse(ResourcePathTypeResolver.isNonCitrusApiJarUrl(
                "jar:nested:/path/to/fat.jar/!BOOT-INF/lib/some-library-1.0.0.jar!/"));
    }

    @Test
    public void testIsNonCitrusApiJarUrlExcludesNonFileUrls() {
        Assertions.assertFalse(ResourcePathTypeResolver.isNonCitrusApiJarUrl(
                "jar:file:/path/to/fat.jar!/BOOT-INF/lib/some-library-1.0.0.jar!/"));
        Assertions.assertFalse(ResourcePathTypeResolver.isNonCitrusApiJarUrl("http://example.com/some.jar"));
    }

    @Test
    public void testIsNonCitrusApiJarUrlExcludesNonJarFiles() {
        Assertions.assertFalse(ResourcePathTypeResolver.isNonCitrusApiJarUrl("file:/path/to/classes/"));
        Assertions.assertFalse(ResourcePathTypeResolver.isNonCitrusApiJarUrl("file:/path/to/some-file.txt"));
    }

    @Test
    public void testResolveAll() {
        Map<String, Object> resolved = new ResourcePathTypeResolver().resolveAll("mocks");
        Assertions.assertEquals(3L, resolved.size());
        Assertions.assertNotNull(resolved.get("foo"));
        Assertions.assertEquals(Foo.class, resolved.get("foo").getClass());
        Assertions.assertNotNull(resolved.get("bar"));
        Assertions.assertEquals(Bar.class, resolved.get("bar").getClass());
        Assertions.assertNotNull(resolved.get("singletonFoo"));
        Assertions.assertEquals(SingletonFoo.class, resolved.get("singletonFoo").getClass());

        resolved = new ResourcePathTypeResolver("META-INF/mocks").resolveAll();
        Assertions.assertEquals(3L, resolved.size());
        Assertions.assertNotNull(resolved.get("foo"));
        Assertions.assertEquals(Foo.class, resolved.get("foo").getClass());
        Assertions.assertNotNull(resolved.get("bar"));
        Assertions.assertEquals(Bar.class, resolved.get("bar").getClass());
        Assertions.assertNotNull(resolved.get("singletonFoo"));
        Assertions.assertEquals(SingletonFoo.class, resolved.get("singletonFoo").getClass());

        resolved = new ResourcePathTypeResolver().resolveAll("mocks", TypeResolver.DEFAULT_TYPE_PROPERTY, "name");
        Assertions.assertEquals(3L, resolved.size());
        Assertions.assertNotNull(resolved.get("fooMock"));
        Assertions.assertEquals(Foo.class, resolved.get("fooMock").getClass());
        Assertions.assertNotNull(resolved.get("barMock"));
        Assertions.assertEquals(Bar.class, resolved.get("barMock").getClass());
        Assertions.assertNotNull(resolved.get("singletonFooMock"));
        Assertions.assertEquals(SingletonFoo.class, resolved.get("singletonFooMock").getClass());

        resolved = new ResourcePathTypeResolver().resolveAll("all", TypeResolver.TYPE_PROPERTY_WILDCARD);
        Assertions.assertEquals(3L, resolved.size());
        Assertions.assertNotNull(resolved.get("mocks.foo"));
        Assertions.assertEquals(Foo.class, resolved.get("mocks.foo").getClass());
        Assertions.assertNotNull(resolved.get("mocks.bar"));
        Assertions.assertEquals(Bar.class, resolved.get("mocks.bar").getClass());
        Assertions.assertNotNull(resolved.get("mocks.singletonFoo"));
        Assertions.assertEquals(SingletonFoo.class, resolved.get("mocks.singletonFoo").getClass());
    }
}
