/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.variable;

import java.util.Optional;

import org.citrusframework.builder.WithExpressions;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.spi.ResourcePathTypeResolver;
import org.citrusframework.spi.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class extracting variables form messages. Implementing classes may read
 * message contents and save those to test variables.
 *
 * @author Christoph Deppisch
 */
public interface VariableExtractor extends MessageProcessor {

    /** Logger */
    Logger logger = LoggerFactory.getLogger(VariableExtractor.class);

    /** Variable extractor resource lookup path */
    String RESOURCE_PATH = "META-INF/citrus/variable/extractor";

    /** Type resolver to find custom variable extractors on classpath via resource path lookup */
    TypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(RESOURCE_PATH);

    /**
     * Resolves extractor from resource path lookup with given extractor resource name. Scans classpath for extractor meta information
     * with given name and returns instance of extractor. Returns optional instead of throwing exception when no extractor
     * could be found.
     * @param extractor
     * @return
     */
    static <T extends VariableExtractor, B extends Builder<T, B>> Optional<Builder<T, B>> lookup(String extractor) {
        try {
            Builder<T, B> instance = TYPE_RESOLVER.resolve(extractor);
            return Optional.of(instance);
        } catch (CitrusRuntimeException e) {
            logger.warn(String.format("Failed to resolve variable extractor from resource '%s/%s'", RESOURCE_PATH, extractor));
        }

        return Optional.empty();
    }

    @Override
    default void process(Message message, TestContext context) {
        extractVariables(message, context);
    }

    /**
     * Extract variables from given message.
     * @param message
     * @param context
     */
    void extractVariables(Message message, TestContext context);

    /**
     * Fluent builder
     * @param <T> extractor type
     * @param <B> builder reference to self
     */
    interface Builder<T extends VariableExtractor, B extends Builder<T, B>> extends MessageProcessor.Builder<T, B>, WithExpressions<B> {

        /**
         * Builds new variable extractor instance.
         * @return the built extractor.
         */
        T build();
    }
}
