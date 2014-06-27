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

package com.consol.citrus.admin.service;

import com.consol.citrus.admin.converter.endpoint.EndpointConverter;
import com.consol.citrus.admin.exception.CitrusAdminRuntimeException;
import com.consol.citrus.admin.model.EndpointData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 1.3.1
 */
@Component
public class EndpointService {

    @Autowired
    private SpringBeanService springBeanService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private List<EndpointConverter<?>> endpointConverter;

    /**
     * Gets the endpoint data object from bean in application context. Bean is identified by its id.
     * @param id
     * @return
     */
    public EndpointData getEndpoint(String id) {
        for (EndpointConverter converter : endpointConverter) {
            Object model = springBeanService.getBeanDefinition(projectService.getProjectContextConfigFile(), id, converter.getModelClass());
            if (model != null) {
                return converter.convert(model);
            }
        }

        throw new CitrusAdminRuntimeException("Unable to find endpoint definition for id '" + id + "'");
    }

    /**
     * Gets the endpoint type data which is an empty endpoint data object containing all endpoint properties. The type is a name
     * that is mapped to a endpoint implementation class.
     * @param type
     * @return
     */
    public EndpointData getEndpointType(String type) {
        for (EndpointConverter converter : endpointConverter) {
            if (converter.getEndpointType().equals(type)) {
                try {
                    return converter.convert(converter.getModelClass().newInstance());
                } catch (InstantiationException e) {
                    throw new CitrusAdminRuntimeException("Failed to create new endpoint model instance", e);
                } catch (IllegalAccessException e) {
                    throw new CitrusAdminRuntimeException("Failed to create new endpoint model instance", e);
                }
            }
        }

        throw new CitrusAdminRuntimeException("Unable to find endpoint definition for type '" + type + "'");
    }

    /**
     * List all message receiver types in application context.
     * @return
     */
    public List<EndpointData> listEndpoints() {
        List<EndpointData> endpointData = new ArrayList<EndpointData>();

        for (EndpointConverter converter : endpointConverter) {
            List<?> models = springBeanService.getBeanDefinitions(projectService.getProjectContextConfigFile(), converter.getModelClass());
            for (Object endpoint : models) {
                endpointData.add(converter.convert(endpoint));
            }
        }

        return endpointData;
    }

}
