package org.citrusframework.cucumber.backend;

import java.util.function.Supplier;

import org.citrusframework.cucumber.backend.spring.CitrusSpringBackend;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Lookup;
import org.citrusframework.spi.Resources;

/**
 * @author Christoph Deppisch
 */
public class CitrusBackendProviderService implements BackendProviderService {
    @Override
    public Backend create(Lookup lookup, Container container, Supplier<ClassLoader> classLoader) {
        if (Resources.fromClasspath("org/citrusframework/CitrusSpringContext.class").exists()) {
            return new CitrusSpringBackend(lookup, container);
        } else {
            return new CitrusBackend(lookup, container);
        }
    }
}
