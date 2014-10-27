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

import com.consol.citrus.TestActor;
import com.consol.citrus.admin.converter.AbstractObjectConverter;
import com.consol.citrus.admin.model.EndpointData;

import javax.xml.bind.annotation.XmlSchema;

/**
 * Abstract endpoint converter provides basic endpoint property handling by Java reflection on JAXb objects.
 *
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public abstract class AbstractEndpointConverter<S> extends AbstractObjectConverter<EndpointData, S> implements EndpointConverter<S> {

    /**
     * Adds basic endpoint properties using reflection on definition objects.
     * @param endpointData
     * @param definition
     */
    protected void addEndpointProperties(EndpointData endpointData, S definition) {
        endpointData.add(property("timeout", definition, "5000"));
        endpointData.add(property("actor", "TestActor", definition).optionKey(TestActor.class.getName()));
    }

    @Override
    public String getEndpointType() {
        String endpointNamespace = getModelClass().getPackage().getAnnotation(XmlSchema.class).namespace();
        return endpointNamespace.substring("http://www.citrusframework.org/schema/".length(), endpointNamespace.indexOf("/config"));
    }
}
