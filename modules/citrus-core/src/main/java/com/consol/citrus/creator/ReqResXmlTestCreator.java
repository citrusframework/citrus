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

package com.consol.citrus.creator;

import java.util.Properties;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class ReqResXmlTestCreator extends XmlTestCreator {

    /** Sample request */
    private String request;

    /** Sample response */
    private String response;

    @Override
    protected Properties getTemplateProperties() {
        Properties properties = super.getTemplateProperties();

        properties.put("test.request", request);
        properties.put("test.response", response);

        return properties;
    }

    @Override
    protected String getTemplateFilePath() {
        return "classpath:com/consol/citrus/creator/test-req-res-template.xml";
    }

    /**
     * Set the request to use.
     * @param request
     * @return
     */
    public ReqResXmlTestCreator withRequest(String request) {
        this.request = request;
        return this;
    }

    /**
     * Set the response to use.
     * @param response
     * @return
     */
    public ReqResXmlTestCreator withResponse(String response) {
        this.response = response;
        return this;
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
     * Gets the request.
     *
     * @return
     */
    public String getRequest() {
        return request;
    }

    /**
     * Sets the response.
     *
     * @param response
     */
    public void setResponse(String response) {
        this.response = response;
    }

    /**
     * Gets the response.
     *
     * @return
     */
    public String getResponse() {
        return response;
    }
}
