package org.citrusframework.spi;

import static java.lang.Thread.currentThread;
import static java.util.Collections.enumeration;
import static java.util.Objects.requireNonNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ClassPathResourceResolverTest {

    public static final String LOAD_FROM_FAT_JAR_SPRING_BOOT = "loadFromFatJarSpringBoot";
    private final ClasspathResourceResolver fixture = new ClasspathResourceResolver();

    @DataProvider(name = LOAD_FROM_FAT_JAR_SPRING_BOOT)
    public Object[][] loadFromFatJarSpringBoot() {
        return new Object[][]{
            {"META-INF/citrus/test/parser/core", "file"},
            {"META-INF/citrus/test/parser/core/*", "file"},
            {"META-INF/citrus/test/parser/core.*", "file"},
            {"/META-INF/citrus/test/parser/core.*", "file"},
            {"classpath:META-INF/citrus/test/parser/core.*", "file"},
            {"META-INF/citrus/test/parser/core", "nested"},
            {"META-INF/citrus/test/parser/core/*", "nested"},
            {"META-INF/citrus/test/parser/core.*", "nested"},
            {"/META-INF/citrus/test/parser/core.*", "nested"},
            {"classpath:META-INF/citrus/test/parser/core.*", "nested"}
        };
    }

    @Test(dataProvider = LOAD_FROM_FAT_JAR_SPRING_BOOT)
    public void loadFromFatJarAbsoluteUrl(String resourcePath, String nestedProtocol) throws IOException {
        verifyLoadedResourcesFromJar(nestedProtocol, "fatjar.jar", "!/BOOT-INF/lib/test-nested-jar.jar", resourcePath);
    }

    @Test(dataProvider = LOAD_FROM_FAT_JAR_SPRING_BOOT)
    public void loadFromFatJarRelativeUrl(String resourcePath, String nestedProtocol) throws IOException {
        verifyLoadedResourcesFromJar(nestedProtocol, "fatjar.jar", "!BOOT-INF/lib/test-nested-jar.jar", resourcePath);
    }

    @Test
    void loadFromSimpleJar() throws IOException {
        verifyLoadedResourcesFromJar("file", "simplejar.jar", "", "META-INF/citrus/test/parser/core");
    }

    private void verifyLoadedResourcesFromJar(String nestedProtocol, String baseJar, String nestedJar, String resourcePath) throws IOException {
        ClassLoader contextClassLoader = currentThread().getContextClassLoader();

        try {
            currentThread()
                .setContextClassLoader(
                    // Note the *missing* leading slash, which makes this a relative URL
                    new SimulatedNestedJarClassLoader(nestedProtocol, baseJar, nestedJar,contextClassLoader));
            Set<Path> resources = fixture.getResources(resourcePath);

            assertTrue(resources.contains(Path.of("META-INF/citrus/test/parser/core/schema-collection")));
            assertTrue(resources.contains(Path.of("META-INF/citrus/test/parser/core/xml-data-dictionary")));
            assertTrue(resources.contains(Path.of("META-INF/citrus/test/parser/core/xpath-data-dictionary")));
        } finally {
            currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    /**
     * A classloader that simulates resolving from a nested jar. This kind of jar, also known as fat
     * jar or uber jar is used in spring boot applications.
     */
    private static class SimulatedNestedJarClassLoader extends ClassLoader {

        private final String nestedProtocol;
        private final String baseJar;
        private final String nestedJar;
        private final ClassLoader delegate;

        private SimulatedNestedJarClassLoader(String nestedProtocol, String baseJar,
            String nestedJar, ClassLoader delegate) {
            this.nestedProtocol = nestedProtocol;
            this.baseJar = baseJar;
            this.nestedJar = nestedJar;
            this.delegate = delegate;
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            if (name.equals("META-INF/citrus/test/parser/core/")) {
                URL url = delegate.getResource(baseJar);
                requireNonNull(url);

                URL jarResourceUrl = new URL("jar:" + normalizeUrl(url) + nestedJar + "!/META-INF/citrus/test/parser/core");

                // "nested" is not recognized protocol and can thus not be used for creating URLS.
                // Therefore, use a spy to fake in the "nested" protocol if needed.
                URL urlSpy = spy(jarResourceUrl);
                doReturn(jarResourceUrl.getFile().replace("file:", nestedProtocol + ":")).when(urlSpy).getFile();

                return enumeration(List.of(urlSpy));
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
            requireNonNull(url);

            try {
                return new URL("jar:" + normalizeUrl(url) + "!/BOOT-INF/lib/test-nested-jar.jar");
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            return delegate.getResourceAsStream(name);
        }

        private static String normalizeUrl(URL url) {
            return url.toString().replace("\\", "/");
        }
    }
}
