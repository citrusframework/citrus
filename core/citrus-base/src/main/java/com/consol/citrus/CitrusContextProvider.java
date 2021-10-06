package com.consol.citrus;

import java.util.Map;
import java.util.Optional;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.spi.ResourcePathTypeResolver;
import com.consol.citrus.spi.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 */
@FunctionalInterface
public interface CitrusContextProvider {

    /** Logger */
    Logger LOG = LoggerFactory.getLogger(CitrusContextProvider.class);

    /** Endpoint parser resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/context/provider";

    /** Default Citrus context provider from classpath resource properties */
    ResourcePathTypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /**
     * Create Citrus context with this provider.
     * @return
     */
    CitrusContext create();

    /**
     * Resolves context provider from resource path lookup. Scans classpath for provider meta information
     * and instantiates first found or default fallback provider.
     * @return
     */
    static CitrusContextProvider lookup() {
        Map<String, CitrusContextProvider> provider =
                TYPE_RESOLVER.resolveAll("", TypeResolver.TYPE_PROPERTY_WILDCARD);

        if (provider.isEmpty()) {
            LOG.debug("Using default Citrus context provider");
            return new DefaultCitrusContextProvider();
        }

        if (provider.size() > 1) {
            LOG.warn(String.format("Found %d Citrus context provider implementations. Please choose one of them.", provider.size()));
        }

        if (LOG.isDebugEnabled()) {
            provider.forEach((k, v) -> LOG.debug(String.format("Found Citrus context provider '%s' as %s", k, v.getClass())));
        }

        CitrusContextProvider contextProvider = provider.values().iterator().next();
        LOG.debug(String.format("Using Citrus context provider '%s' as %s", provider.keySet().iterator().next(), contextProvider));
        return contextProvider;
    }

    /**
     * Resolves context provider from resource path lookup with given resource name. Scans classpath for provider meta information
     * with given name and returns the instance. Returns optional instead of throwing exception when no context provider
     * could be found.
     *
     * @param name
     * @return
     */
    static Optional<CitrusContextProvider> lookup(String name) {
        try {
            CitrusContextProvider instance = TYPE_RESOLVER.resolve(name);
            return Optional.of(instance);
        } catch (CitrusRuntimeException e) {
            LOG.warn(String.format("Failed to resolve Citrus context provider from resource '%s/%s'", RESOURCE_PATH, name));
        }

        return Optional.empty();
    }
}
