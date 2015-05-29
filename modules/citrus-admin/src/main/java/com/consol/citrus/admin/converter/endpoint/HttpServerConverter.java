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

package com.consol.citrus.admin.converter.endpoint;

import com.consol.citrus.admin.model.EndpointData;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.message.MessageConverter;
import com.consol.citrus.model.config.http.HttpServerDefinition;
import org.springframework.stereotype.Component;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Component
public class HttpServerConverter extends AbstractEndpointConverter<HttpServerDefinition> {

    public static final String TRUE = "true";
    public static final String FALSE = "false";

    @Override
    public EndpointData convert(HttpServerDefinition server) {
        EndpointData endpointData = new EndpointData(getEndpointType(), server.getId(), getModelClass());

        endpointData.add(property("port", server));
        endpointData.add(property("autoStart", server, TRUE)
                .options(TRUE, FALSE));
        endpointData.add(property("resourceBase", server));
        endpointData.add(property("contextPath", server));
        endpointData.add(property("rootParentContext", server, TRUE)
                .options(TRUE, FALSE));
        endpointData.add(property("messageConverter", server)
                .optionKey(MessageConverter.class.getName()));
        endpointData.add(property("endpointAdapter", server)
                .optionKey(EndpointAdapter.class.getName()));
        endpointData.add(property("securityHandler", server));
        endpointData.add(property("servletHandler", server));
        endpointData.add(property("connector", server));
        endpointData.add(property("connectors", server));
        endpointData.add(property("servletName", server));
        endpointData.add(property("servletMappingPath", server));
        endpointData.add(property("interceptors", server));

        endpointData.add(property("timeout", server, "5000"));

        return endpointData;
    }

    @Override
    public Class<HttpServerDefinition> getModelClass() {
        return HttpServerDefinition.class;
    }

    @Override
    public String getEndpointType() {
        return "http-server";
    }
}
