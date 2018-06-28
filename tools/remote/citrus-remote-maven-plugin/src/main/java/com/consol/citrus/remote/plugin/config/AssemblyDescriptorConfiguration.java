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

package com.consol.citrus.remote.plugin.config;

import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.assembly.model.Assembly;

import java.io.Serializable;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class AssemblyDescriptorConfiguration implements Serializable {

    @Parameter(property = "citrus.remote.assembly.descriptor")
    private String file;

    @Parameter(property = "citrus.remote.assembly.descriptorRef")
    private String ref;

    @Parameter
    private Assembly inline;

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
     * Gets the ref.
     *
     * @return
     */
    public String getRef() {
        return ref;
    }

    /**
     * Sets the ref.
     *
     * @param ref
     */
    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     * Gets the inline.
     *
     * @return
     */
    public Assembly getInline() {
        return inline;
    }

    /**
     * Sets the inline.
     *
     * @param inline
     */
    public void setInline(Assembly inline) {
        this.inline = inline;
    }
}
