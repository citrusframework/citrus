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
import com.consol.citrus.message.MessageCorrelator;
import com.consol.citrus.model.config.ftp.FtpClientDefinition;
import org.springframework.stereotype.Component;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
@Component
public class FtpEndpointConverter extends AbstractEndpointConverter<FtpClientDefinition> {

    @Override
    public EndpointData convert(FtpClientDefinition client) {
        EndpointData endpointData = new EndpointData(getEndpointType(), client.getId(), getModelClass());

        endpointData.add(property("host", client));
        endpointData.add(property("port", client));
        endpointData.add(property("user", client));
        endpointData.add(property("password", client));
        endpointData.add(property("messageCorrelator", client)
                .optionKey(MessageCorrelator.class.getName()));

        endpointData.add(property("pollingInterval", client));

        addEndpointProperties(endpointData, client);

        return endpointData;
    }

    @Override
    public Class<FtpClientDefinition> getModelClass() {
        return FtpClientDefinition.class;
    }
}
