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

package org.citrusframework.variable.dictionary;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.AbstractMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * Abstract data dictionary implementation provides global scope handling.
 * @author Christoph Deppisch
 */
public abstract class AbstractDataDictionary<T> extends AbstractMessageProcessor implements DataDictionary<T> {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(AbstractDataDictionary.class);

    /** Data dictionary name */
    private String name = getClass().getSimpleName();

    /** Scope defines where dictionary should be applied (explicit or global) */
    private boolean globalScope = true;

    /** Known mappings to this dictionary */
    protected Map<String, String> mappings = new LinkedHashMap<>();

    /** mapping file resource */
    protected Resource mappingFile;

    /** Kind of mapping strategy how to identify dictionary item */
    private PathMappingStrategy pathMappingStrategy = PathMappingStrategy.EXACT;

    /**
     * Convert to original value type if necessary.
     * @param value
     * @param originalValue
     * @param context
     * @param <V>
     * @return
     */
    protected <V> V convertIfNecessary(String value, V originalValue, TestContext context) {
        if (originalValue == null) {
            return (V) context.replaceDynamicContentInString(value);
        }

        return context.getTypeConverter().convertIfNecessary(context.replaceDynamicContentInString(value),
                                                            (Class<V>) originalValue.getClass());
    }

    @Override
    public void initialize() {
        if (mappingFile != null) {

            if (log.isDebugEnabled()) {
                log.debug("Reading data dictionary mapping " + mappingFile.getFilename());
            }

            Properties props;
            try {
                props = PropertiesLoaderUtils.loadProperties(mappingFile);
            } catch (IOException e) {
                throw new CitrusRuntimeException(e);
            }

            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                String key = entry.getKey().toString();

                if (log.isDebugEnabled()) {
                    log.debug("Loading data dictionary mapping: " + key + "=" + props.getProperty(key));
                }

                if (log.isDebugEnabled() && mappings.containsKey(key)) {
                    log.debug("Overwriting data dictionary mapping " + key + " old value:" + mappings.get(key)
                            + " new value:" + props.getProperty(key));
                }

                mappings.put(key, props.getProperty(key));
            }

            log.debug("Loaded data dictionary mapping " + mappingFile.getFilename());
        }
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the data dictionary name.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isGlobalScope() {
        return globalScope;
    }

    /**
     * Sets the global scope property.
     * @param scope
     */
    public void setGlobalScope(boolean scope) {
        this.globalScope = scope;
    }

    /**
     * Sets the mappings.
     * @param mappings
     */
    public void setMappings(Map<String, String> mappings) {
        this.mappings = mappings;
    }

    /**
     * Gets the mappings.
     * @return
     */
    public Map<String, String> getMappings() {
        return mappings;
    }

    /**
     * Gets the mapping file resource.
     * @return
     */
    public Resource getMappingFile() {
        return mappingFile;
    }

    /**
     * Sets the mapping file resource.
     * @param mappingFile
     */
    public void setMappingFile(Resource mappingFile) {
        this.mappingFile = mappingFile;
    }

    @Override
    public PathMappingStrategy getPathMappingStrategy() {
        return pathMappingStrategy;
    }

    /**
     * Sets the path mapping strategy.
     * @param pathMappingStrategy
     */
    public void setPathMappingStrategy(PathMappingStrategy pathMappingStrategy) {
        this.pathMappingStrategy = pathMappingStrategy;
    }
}
