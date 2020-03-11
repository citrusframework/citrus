package com.consol.citrus.cucumber.backend;

import java.util.function.Supplier;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Lookup;

/**
 * @author Christoph Deppisch
 */
public class CitrusBackendProviderService implements BackendProviderService {
    @Override
    public Backend create(Lookup lookup, Container container, Supplier<ClassLoader> classLoader) {
        return new CitrusBackend(lookup, container);
    }
}
