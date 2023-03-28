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

package org.citrusframework.mvn.plugin.config.dictionary;

import org.apache.maven.plugins.annotations.Parameter;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class MappingsConfiguration implements Serializable {

    @Parameter
    private String file;

    @Parameter
    private Map<String, Object> mappings;

    /**
     * Gets the file.
     *
     * @return
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the file.
     *
     * @param file
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * Gets the mappings.
     *
     * @return
     */
    public Map<String, Object> getMappings() {
        return mappings;
    }

    /**
     * Sets the mappings.
     *
     * @param mappings
     */
    public void setMappings(Map<String, Object> mappings) {
        this.mappings = mappings;
    }
}
