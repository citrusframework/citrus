/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.endpoint.adapter.mapping;

import org.citrusframework.message.Message;

/**
 * Abstract mapping key extractor adds common mapping prefix and suffix added to evaluated mapping key.
 * Subclasses do evaluate mapping key from incoming request message and optional prefix and/or suffix are
 * automatically added to resulting mapping key.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class AbstractMappingKeyExtractor implements MappingKeyExtractor {

    /** Optional prefix/suffix values to add */
    private String mappingKeyPrefix = "";
    private String mappingKeySuffix = "";

    @Override
    public final String extractMappingKey(Message request) {
        return mappingKeyPrefix + getMappingKey(request) + mappingKeySuffix;
    }

    /**
     * Provides mapping key from incoming request message. Subclasses must implement.
     * @param request
     * @return
     */
    protected abstract String getMappingKey(Message request);

    /**
     * Sets the static mapping key prefix automatically added to extracted mapping key.
     * @param mappingKeyPrefix
     */
    public void setMappingKeyPrefix(String mappingKeyPrefix) {
        this.mappingKeyPrefix = mappingKeyPrefix;
    }

    /**
     * Sets the static mapping key suffix automatically added to extracted mapping key.
     * @param mappingKeySuffix
     */
    public void setMappingKeySuffix(String mappingKeySuffix) {
        this.mappingKeySuffix = mappingKeySuffix;
    }

}
