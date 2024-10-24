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

package org.citrusframework.container;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.functions.Function;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.citrusframework.spi.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface TemplateLoader extends ReferenceResolverAware {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(TemplateLoader.class);

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
            logger.warn("Failed to resolve template loader from resource '{}/{}'", RESOURCE_PATH, name);
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
