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
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActionContainerBuilder;
import org.citrusframework.actions.ReferenceResolverAwareBuilder;
import org.citrusframework.container.TestActionContainer;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.spi.Resource;
import org.citrusframework.validation.context.ValidationContext;

public interface SoapAssertFaultActionBuilder<T extends TestActionContainer, B extends TestActionContainerBuilder<T, B>>
        extends TestActionContainerBuilder<T, B>, ReferenceResolverAwareBuilder<T, B> {

    /**
     * Action producing the SOAP fault.
     */
    SoapAssertFaultActionBuilder<T, B> when(TestAction action);

    /**
     * Action producing the SOAP fault.
     */
    SoapAssertFaultActionBuilder<T, B> when(TestActionBuilder<?> builder);

    /**
     * Sets the message endpoint to send messages to.
     */
    SoapAssertFaultActionBuilder<T, B> endpoint(Endpoint messageEndpoint);

    /**
     * Sets the message endpoint uri to send messages to.
     */
    SoapAssertFaultActionBuilder<T, B> endpoint(String messageEndpointUri);

    /**
     * Expect fault code in SOAP fault message.
     */
    SoapAssertFaultActionBuilder<T, B> faultCode(String code);

    /**
     * Expect fault string in SOAP fault message.
     */
    SoapAssertFaultActionBuilder<T, B> faultString(String faultString);

    /**
     * Expect fault actor in SOAP fault message.
     */
    SoapAssertFaultActionBuilder<T, B> faultActor(String faultActor);

    /**
     * Expect fault detail in SOAP fault message.
     */
    SoapAssertFaultActionBuilder<T, B> faultDetail(String faultDetail);

    /**
     * Expect fault detail from file resource.
     */
    SoapAssertFaultActionBuilder<T, B> faultDetailResource(Resource resource);

    /**
     * Expect fault detail from file resource.
     */
    SoapAssertFaultActionBuilder<T, B> faultDetailResource(Resource resource, Charset charset);

    /**
     * Expect fault detail from file resource.
     */
    SoapAssertFaultActionBuilder<T, B> faultDetailResource(String filePath);

    /**
     * Set explicit SOAP fault validator implementation.
     */
    SoapAssertFaultActionBuilder<T, B> validator(Object validator);

    /**
     * Set explicit SOAP fault validator implementation by bean name.
     */
    SoapAssertFaultActionBuilder<T, B> validator(String validatorName);

    /**
     * Specifies the validationContext.
     */
    SoapAssertFaultActionBuilder<T, B> validate(ValidationContext.Builder<?, ?> validationContext);

    /**
     * Specifies the validationContext.
     */
    SoapAssertFaultActionBuilder<T, B> validateDetail(ValidationContext.Builder<?, ?> validationContext);
}
