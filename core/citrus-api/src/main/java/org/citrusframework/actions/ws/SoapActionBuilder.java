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

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReferenceResolverAwareBuilder;
import org.citrusframework.endpoint.Endpoint;

public interface SoapActionBuilder<T extends TestAction, B extends SoapActionBuilder<T, B>>
        extends ReferenceResolverAwareBuilder<T, B>, TestActionBuilder<T> {

    /**
     * Initiate soap client action.
     */
    SoapClientActionBuilder<?, ?> client();

    /**
     * Initiate soap client action.
     */
    SoapClientActionBuilder<?, ?> client(Endpoint client);

    /**
     * Initiate soap client action.
     */
    SoapClientActionBuilder<?, ?> client(String soapClient);

    /**
     * Initiate soap server action.
     */
    SoapServerActionBuilder<?, ?> server();

    /**
     * Initiate soap server action.
     */
    SoapServerActionBuilder<?, ?> server(Endpoint soapServer);

    /**
     * Initiate soap server action.
     */
    SoapServerActionBuilder<?, ?> server(String soapServer);

    interface BuilderFactory {

        SoapActionBuilder<?, ?> soap();

    }
}
