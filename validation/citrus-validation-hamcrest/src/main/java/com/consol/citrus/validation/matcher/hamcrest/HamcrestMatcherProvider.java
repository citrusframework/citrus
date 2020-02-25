/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.validation.matcher.hamcrest;

import java.util.Optional;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.spi.ResourcePathTypeResolver;
import com.consol.citrus.spi.TypeResolver;
import org.hamcrest.Matcher;
import org.slf4j.LoggerFactory;

/**
 * Matcher provider interface for custom matcher implementations.
 */
public interface HamcrestMatcherProvider {

    /** Resource path where to lookup custom matcher providers in classpath */
    String RESOURCE_PATH = "META-INF/citrus/hamcrest/matcher/provider";

    /** Type resolver to find custom matcher providers via resource path lookup */
    TypeResolver TYPE_RESOLVER = new ResourcePathTypeResolver(HamcrestMatcherProvider.RESOURCE_PATH);

    /**
     * Resolves matcher provider from resource path lookup. Returns optional instead of throwing exception when no matcher
     * could be found.
     * @param matcherName
     * @return
     */
    static Optional<HamcrestMatcherProvider> lookup(String matcherName) {
        try {
            HamcrestMatcherProvider matcherProvider = TYPE_RESOLVER.resolve(matcherName);
            return Optional.of(matcherProvider);
        } catch (CitrusRuntimeException e) {
            LoggerFactory.getLogger(HamcrestMatcherProvider.class).warn(String.format("Failed to resolve Hamcrest matcher provider with name '%s'", matcherName));
        }

        return Optional.empty();
    }

    /**
     * This matcher provider name.
     * @return
     */
    String getName();

    /**
     * Provide custom matcher with given predicate.
     * @param predicate
     * @return
     */
    Matcher<?> provideMatcher(String predicate);
}
