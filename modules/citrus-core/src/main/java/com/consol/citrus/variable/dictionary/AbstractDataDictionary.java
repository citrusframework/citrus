/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.variable.dictionary;

import com.consol.citrus.validation.interceptor.AbstractMessageConstructionInterceptor;

/**
 * Abstract data dictionary implementation provides global scope handling.
 * @author Christoph Deppisch
 */
public abstract class AbstractDataDictionary<T> extends AbstractMessageConstructionInterceptor implements DataDictionary<T> {

    /** Scope defines where dictionary should be applied (explicit or global) */
    private boolean globalScope = true;

    /** Kind of mapping strategy how to identify dictionary item */
    private PathMappingStrategy pathMappingStrategy = PathMappingStrategy.EXACT_MATCH;

    @Override
    public boolean isGlobalScope() {
        return globalScope;
    }

    @Override
    public void setGlobalScope(boolean scope) {
        this.globalScope = scope;
    }

    /**
     * Sets the path mapping strategy.
     * @param pathMappingStrategy
     */
    public void setPathMappingStrategy(PathMappingStrategy pathMappingStrategy) {
        this.pathMappingStrategy = pathMappingStrategy;
    }

    /**
     * Gets the path mapping strategy.
     * @return
     */
    public PathMappingStrategy getPathMappingStrategy() {
        return pathMappingStrategy;
    }
}