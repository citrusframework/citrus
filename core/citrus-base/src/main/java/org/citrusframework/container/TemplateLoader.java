package org.citrusframework.container;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.functions.Function;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.citrusframework.spi.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface TemplateLoader extends ReferenceResolverAware {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(Function.class);

    /** Function resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/template/loader";

    Map<String, Function> loaders = new HashMap<>();

    /** Type resolver to find custom message validators on classpath via resource path lookup */
    TypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /**
     * Resolves template loader from resource path lookup with given resource name. Scans classpath for meta information
     * with given name and returns an instance of the loader. Returns optional instead of throwing exception when no template loader
     * could be found.
     * @param name
     * @return
     */
    static Optional<TemplateLoader> lookup(String name) {
        try {
            TemplateLoader instance = TYPE_RESOLVER.resolve(name);
            return Optional.of(instance);
        } catch (CitrusRuntimeException e) {
            logger.warn(String.format("Failed to resolve template loader from resource '%s/%s'", RESOURCE_PATH, name));
        }

        return Optional.empty();
    }

    /**
     * Loads the template from given file.
     * @param filePath
     * @return
     */
    Template load(String filePath);
}
