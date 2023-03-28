package org.citrusframework.spi;

import java.util.Map;

import org.citrusframework.spi.mocks.Bar;
import org.citrusframework.spi.mocks.Foo;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
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
    }

    @Test
    public void testResolve() {
        Assert.assertEquals(new ResourcePathTypeResolver().resolve("mocks/foo").getClass(), Foo.class);
        Assert.assertEquals(new ResourcePathTypeResolver("META-INF/mocks").resolve("foo").getClass(), Foo.class);
        Assert.assertEquals(new ResourcePathTypeResolver("META-INF/mocks").resolve("bar").getClass(), Bar.class);
        Assert.assertEquals(new ResourcePathTypeResolver("META-INF/mocks").resolve("foo", TypeResolver.DEFAULT_TYPE_PROPERTY).getClass(), Foo.class);
    }

    @Test
    public void testResolveAll() {
        Map<String, Object> resolved = new ResourcePathTypeResolver().resolveAll("mocks");
        Assert.assertEquals(resolved.size(), 2L);
        Assert.assertNotNull(resolved.get("foo"));
        Assert.assertEquals(resolved.get("foo").getClass(), Foo.class);
        Assert.assertNotNull(resolved.get("bar"));
        Assert.assertEquals(resolved.get("bar").getClass(), Bar.class);

        resolved = new ResourcePathTypeResolver("META-INF/mocks").resolveAll();
        Assert.assertEquals(resolved.size(), 2L);
        Assert.assertNotNull(resolved.get("foo"));
        Assert.assertEquals(resolved.get("foo").getClass(), Foo.class);
        Assert.assertNotNull(resolved.get("bar"));
        Assert.assertEquals(resolved.get("bar").getClass(), Bar.class);

        resolved = new ResourcePathTypeResolver().resolveAll("mocks", TypeResolver.DEFAULT_TYPE_PROPERTY, "name");
        Assert.assertEquals(resolved.size(), 2L);
        Assert.assertNotNull(resolved.get("fooMock"));
        Assert.assertEquals(resolved.get("fooMock").getClass(), Foo.class);
        Assert.assertNotNull(resolved.get("barMock"));
        Assert.assertEquals(resolved.get("barMock").getClass(), Bar.class);

        resolved = new ResourcePathTypeResolver().resolveAll("all", TypeResolver.TYPE_PROPERTY_WILDCARD);
        Assert.assertEquals(resolved.size(), 2L);
        Assert.assertNotNull(resolved.get("mocks.foo"));
        Assert.assertEquals(resolved.get("mocks.foo").getClass(), Foo.class);
        Assert.assertNotNull(resolved.get("mocks.bar"));
        Assert.assertEquals(resolved.get("mocks.bar").getClass(), Bar.class);
    }
}
