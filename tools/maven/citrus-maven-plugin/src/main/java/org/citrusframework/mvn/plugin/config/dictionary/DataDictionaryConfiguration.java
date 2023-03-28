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

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class DataDictionaryConfiguration implements Serializable {

    @Parameter
    private MappingsConfiguration inbound;

    @Parameter
    private MappingsConfiguration outbound;

    /**
     * Gets the inbound.
     *
     * @return
     */
    public MappingsConfiguration getInbound() {
        return inbound;
    }

    /**
     * Sets the inbound.
     *
     * @param inbound
     */
    public void setInbound(MappingsConfiguration inbound) {
        this.inbound = inbound;
    }

    /**
     * Gets the outbound.
     *
     * @return
     */
    public MappingsConfiguration getOutbound() {
        return outbound;
    }

    /**
     * Sets the outbound.
     *
     * @param outbound
     */
    public void setOutbound(MappingsConfiguration outbound) {
        this.outbound = outbound;
    }
}
