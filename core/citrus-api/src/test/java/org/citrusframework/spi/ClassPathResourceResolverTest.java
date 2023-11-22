package org.citrusframework.spi;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClassPathResourceResolverTest {

    @Test
    void loadFromFatJar() throws IOException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread()
                .setContextClassLoader(new SimulatedNestedJarClassLoader("fatjar.jar", "!/BOOT-INF/lib/test-nested-jar.jar", contextClassLoader));
            ClasspathResourceResolver resolver = new ClasspathResourceResolver();
            Set<Path> resources = resolver.getResources("META-INF/citrus/test/parser/core");
            Assertions.assertTrue(
                resources.contains(Path.of("META-INF/citrus/test/parser/core/schema-collection")));
            Assertions.assertTrue(resources.contains(
                Path.of("META-INF/citrus/test/parser/core/xml-data-dictionary")));
            Assertions.assertTrue(resources.contains(
                Path.of("META-INF/citrus/test/parser/core/xpath-data-dictionary")));
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    @Test
    void loadFromSimpleJar() throws IOException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread()
                .setContextClassLoader(new SimulatedNestedJarClassLoader("simplejar.jar", "", contextClassLoader));
            ClasspathResourceResolver resolver = new ClasspathResourceResolver();
            Set<Path> resources = resolver.getResources("META-INF/citrus/test/parser/core");
            Assertions.assertTrue(
                resources.contains(Path.of("META-INF/citrus/test/parser/core/schema-collection")));
            Assertions.assertTrue(resources.contains(
                Path.of("META-INF/citrus/test/parser/core/xml-data-dictionary")));
            Assertions.assertTrue(resources.contains(
                Path.of("META-INF/citrus/test/parser/core/xpath-data-dictionary")));
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    /**
     * A classloader that simulates resolving from a nested jar. This kind of jar, also known as fat
     * jar or uber jar is used in spring boot applications.
     */
    private class SimulatedNestedJarClassLoader extends ClassLoader {

        private final String baseJar;
        private final String nestedJar;
        private final ClassLoader delegate;

        private SimulatedNestedJarClassLoader(String baseJar, String nestedJar, ClassLoader delegate) {
            this.baseJar = baseJar;
            this.nestedJar = nestedJar;
            this.delegate = delegate;
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {

            if (name.equals("META-INF/citrus/test/parser/core/")) {
                URL url = delegate.getResource(baseJar);
                URL jarResourceUrl = new URL("jar:" + url.toString().replace("\\", "/")
                    + nestedJar+ "!/META-INF/citrus/test/parser/core");
                return Collections.enumeration(List.of(jarResourceUrl));
            }
            return delegate.getResources(name);
        }

        @Override
        public URL getResource(String name) {
            if ("BOOT-INF/lib/test-nested-jar.jar".equals(name)) {
                return getNestedJarUrl();
            } else if (name.startsWith("META-INF/citrus/test/parser/core")) {
                URL nestedJarUrl = getNestedJarUrl();
                try {
                    return new URL(nestedJarUrl + "/" + name);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
            return delegate.getResource(name);
        }

        private URL getNestedJarUrl() {
            URL url = delegate.getResource(baseJar);
            try {
                return new URL("jar:" + url.toString().replace("\\", "/")
                    + "!/BOOT-INF/lib/test-nested-jar.jar");
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            return delegate.getResourceAsStream(name);
        }

    }
}