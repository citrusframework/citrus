package com.consol.citrus.cucumber.backend;

import java.util.function.Supplier;

import com.consol.citrus.cucumber.backend.spring.CitrusSpringBackend;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Lookup;
import org.springframework.util.ClassUtils;

/**
 * @author Christoph Deppisch
 */
public class CitrusBackendProviderService implements BackendProviderService {
    @Override
    public Backend create(Lookup lookup, Container container, Supplier<ClassLoader> classLoader) {
        if (ClassUtils.isPresent("com.consol.citrus.CitrusSpringContext", getClass().getClassLoader())) {
            return new CitrusSpringBackend(lookup, container);
        } else {
            return new CitrusBackend(lookup, container);
        }
    }
}
