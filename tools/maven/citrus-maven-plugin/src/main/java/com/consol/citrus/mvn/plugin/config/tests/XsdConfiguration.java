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

package com.consol.citrus.mvn.plugin.config.tests;

import org.apache.maven.plugins.annotations.Parameter;

import java.io.Serializable;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class XsdConfiguration implements Serializable {

    /**
     * Actor (client/server) describing which part to generate test for.
     */
    @Parameter(property = "citrus.xsd.actor", defaultValue = "client")
    private String actor;

    /**
     * Path of the xsd from which the sample request and response are get from
     */
    @Parameter(property = "citrus.xsd.file")
    private String file;

    /**
     * Name of the xsd-element used to create the xml-sample-request
     */
    @Parameter(property = "citrus.xsd.request")
    private String request;

    /**
     * Name of the xsd-element used to create the xml-sample-response
     */
    @Parameter(property = "citrus.xsd.response")
    private String response;

    /**
     * Gets the actor.
     *
     * @return
     */
    public String getActor() {
        return actor;
    }

    /**
     * Sets the actor.
     *
     * @param actor
     */
    public void setActor(String actor) {
        this.actor = actor;
    }

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
     * Gets the request.
     *
     * @return
     */
    public String getRequest() {
        return request;
    }

    /**
     * Sets the request.
     *
     * @param request
     */
    public void setRequest(String request) {
        this.request = request;
    }

    /**
     * Gets the response.
     *
     * @return
     */
    public String getResponse() {
        return response;
    }

    /**
     * Sets the response.
     *
     * @param response
     */
    public void setResponse(String response) {
        this.response = response;
    }
}
