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
import org.testng.Assert;
import org.testng.annotations.Test;

public class ResourcePathTypeResolverTest {

    @Test
    public void testResolveProperty() {
        Assert.assertEquals(new ResourcePathTypeResolver().resolveProperty("mocks/foo", TypeResolver.DEFAULT_TYPE_PROPERTY), Foo.class.getName());
        Assert.assertEquals(new ResourcePathTypeResolver().resolveProperty("mocks/foo", "name"), "fooMock");
        Assert.assertEquals(new ResourcePathTypeResolver().resolveProperty("META-INF/mocks/foo", "name"), "fooMock");
        Assert.assertEquals(new ResourcePathTypeResolver("META-INF/mocks").resolveProperty("foo", TypeResolver.DEFAULT_TYPE_PROPERTY), Foo.class.getName());
        Assert.assertEquals(new ResourcePathTypeResolver("META-INF/mocks").resolveProperty("foo", "name"), "fooMock");
        Assert.assertEquals(new ResourcePathTypeResolver("META-INF/mocks").resolveProperty("META-INF/mocks/foo", "name"), "fooMock");
        Assert.assertEquals(new ResourcePathTypeResolver("META-INF/mocks").resolveProperty("bar", "name"), "barMock");
        Assert.assertEquals(new ResourcePathTypeResolver().resolveProperty("mocks/foo", TypeResolver.DEFAULT_TYPE_PROPERTY), Foo.class.getName());
        Assert.assertEquals(new ResourcePathTypeResolver().resolve("mocksWithParams/fooWithParams", 1,(short)2,3.d,4.f, 'c', true, new int[]{1,2,3},"StringParam").getClass(), FooWithParams.class);
    }

    @Test
    public void testResolve() {
        Assert.assertEquals(new ResourcePathTypeResolver("META-INF/mocks").resolve("foo").getClass(), Foo.class);
        Assert.assertEquals(new ResourcePathTypeResolver("META-INF/mocks").resolve("bar").getClass(), Bar.class);
        Assert.assertEquals(new ResourcePathTypeResolver("META-INF/mocks").resolve("foo", TypeResolver.DEFAULT_TYPE_PROPERTY).getClass(), Foo.class);
    }

    @Test
    public void testResolveAll() {
        Map<String, Object> resolved = new ResourcePathTypeResolver().resolveAll("mocks");
        Assert.assertEquals(resolved.size(), 3L);
        Assert.assertNotNull(resolved.get("foo"));
        Assert.assertEquals(resolved.get("foo").getClass(), Foo.class);
        Assert.assertNotNull(resolved.get("bar"));
        Assert.assertEquals(resolved.get("bar").getClass(), Bar.class);
        Assert.assertNotNull(resolved.get("singletonFoo"));
        Assert.assertEquals(resolved.get("singletonFoo").getClass(), SingletonFoo.class);

        resolved = new ResourcePathTypeResolver("META-INF/mocks").resolveAll();
        Assert.assertEquals(resolved.size(), 3L);
        Assert.assertNotNull(resolved.get("foo"));
        Assert.assertEquals(resolved.get("foo").getClass(), Foo.class);
        Assert.assertNotNull(resolved.get("bar"));
        Assert.assertEquals(resolved.get("bar").getClass(), Bar.class);
        Assert.assertNotNull(resolved.get("singletonFoo"));
        Assert.assertEquals(resolved.get("singletonFoo").getClass(), SingletonFoo.class);

        resolved = new ResourcePathTypeResolver().resolveAll("mocks", TypeResolver.DEFAULT_TYPE_PROPERTY, "name");
        Assert.assertEquals(resolved.size(), 3L);
        Assert.assertNotNull(resolved.get("fooMock"));
        Assert.assertEquals(resolved.get("fooMock").getClass(), Foo.class);
        Assert.assertNotNull(resolved.get("barMock"));
        Assert.assertEquals(resolved.get("barMock").getClass(), Bar.class);
        Assert.assertNotNull(resolved.get("singletonFooMock"));
        Assert.assertEquals(resolved.get("singletonFooMock").getClass(), SingletonFoo.class);

        resolved = new ResourcePathTypeResolver().resolveAll("all", TypeResolver.TYPE_PROPERTY_WILDCARD);
        Assert.assertEquals(resolved.size(), 3L);
        Assert.assertNotNull(resolved.get("mocks.foo"));
        Assert.assertEquals(resolved.get("mocks.foo").getClass(), Foo.class);
        Assert.assertNotNull(resolved.get("mocks.bar"));
        Assert.assertEquals(resolved.get("mocks.bar").getClass(), Bar.class);
        Assert.assertNotNull(resolved.get("mocks.singletonFoo"));
        Assert.assertEquals(resolved.get("mocks.singletonFoo").getClass(), SingletonFoo.class);
    }
}
