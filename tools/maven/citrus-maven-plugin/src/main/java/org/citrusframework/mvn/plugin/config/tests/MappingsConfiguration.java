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

package org.citrusframework.mvn.plugin.config.tests;

import org.apache.maven.plugins.annotations.Parameter;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class MappingsConfiguration implements Serializable {

    @Parameter
    private Map<String, String> inbound;

    @Parameter
    private Map<String, String> outbound;

    @Parameter
    private String inboundFile;

    @Parameter
    private String outboundFile;

    /**
     * Gets the inbound.
     *
     * @return
     */
    public Map<String, String> getInbound() {
        return inbound;
    }

    /**
     * Sets the inbound.
     *
     * @param inbound
     */
    public void setInbound(Map<String, String> inbound) {
        this.inbound = inbound;
    }

    /**
     * Gets the outbound.
     *
     * @return
     */
    public Map<String, String> getOutbound() {
        return outbound;
    }

    /**
     * Sets the outbound.
     *
     * @param outbound
     */
    public void setOutbound(Map<String, String> outbound) {
        this.outbound = outbound;
    }

    /**
     * Gets the inboundFile.
     *
     * @return
     */
    public String getInboundFile() {
        return inboundFile;
    }

    /**
     * Sets the inboundFile.
     *
     * @param inboundFile
     */
    public void setInboundFile(String inboundFile) {
        this.inboundFile = inboundFile;
    }

    /**
     * Gets the outboundFile.
     *
     * @return
     */
    public String getOutboundFile() {
        return outboundFile;
    }

    /**
     * Sets the outboundFile.
     *
     * @param outboundFile
     */
    public void setOutboundFile(String outboundFile) {
        this.outboundFile = outboundFile;
    }
}
