/*
 * Copyright the original author or authors.
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

package org.citrusframework.actions.ws;

import java.nio.charset.Charset;

import org.citrusframework.TestAction;
import org.citrusframework.spi.Resource;

public interface SoapSendFaultMessageBuilderFactory<T extends TestAction, M extends SoapSendFaultMessageBuilderFactory<T, M>>
        extends SoapSendMessageBuilderFactory<T, M> {

    /**
     * Adds custom SOAP fault code.
     */
    SoapSendFaultMessageBuilderFactory<T, M> faultCode(String code);

    /**
     * Add custom fault string to SOAP fault message.
     */
    SoapSendFaultMessageBuilderFactory<T, M> faultString(String faultString);

    /**
     * Add custom fault string to SOAP fault message.
     */
    SoapSendFaultMessageBuilderFactory<T, M> faultActor(String faultActor);

    /**
     * Adds a fault detail to SOAP fault message.
     */
    SoapSendFaultMessageBuilderFactory<T, M> faultDetail(String faultDetail);

    /**
     * Adds a fault detail from file resource.
     */
    SoapSendFaultMessageBuilderFactory<T, M> faultDetailResource(Resource resource);

    /**
     * Adds a fault detail from file resource.
     */
    SoapSendFaultMessageBuilderFactory<T, M> faultDetailResource(Resource resource, Charset charset);

    /**
     * Adds a fault detail from file resource path.
     */
    SoapSendFaultMessageBuilderFactory<T, M> faultDetailResource(String filePath);

    /**
     * Sets the response status.
     */
    SoapSendFaultMessageBuilderFactory<T, M> status(Object status);

    /**
     * Sets the response status code.
     */
    SoapSendFaultMessageBuilderFactory<T, M> statusCode(Integer statusCode);

    /**
     * Sets the response status reason phrase.
     */
    SoapSendFaultMessageBuilderFactory<T, M> reasonPhrase(String reasonPhrase);
}
