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

package org.citrusframework.openapi.testapi;

import java.util.List;

import org.citrusframework.ws.actions.ReceiveSoapMessageAction;

import static java.lang.String.format;

public class SoapApiReceiveMessageActionBuilder extends ReceiveSoapMessageAction.Builder {

    private final GeneratedApi generatedApi;

    private final List<ApiActionBuilderCustomizer> customizers;

    public SoapApiReceiveMessageActionBuilder(GeneratedApi generatedApi, String soapAction) {
        super();

        this.generatedApi = generatedApi;
        this.customizers = generatedApi.getCustomizers();

        endpoint(generatedApi.getEndpoint());

        name(format("receive-%s:%s", generatedApi.getClass().getSimpleName().toLowerCase(), soapAction));
    }

    public GeneratedApi getGeneratedApi() {
        return generatedApi;
    }

    public List<ApiActionBuilderCustomizer> getCustomizers() {
        return customizers;
    }

    @Override
    public ReceiveSoapMessageAction doBuild() {

        // If no endpoint was set explicitly, use the default endpoint given by api
        if (getEndpoint() == null && getEndpointUri() == null) {
            endpoint(generatedApi.getEndpoint());
        }

        return new ReceiveSoapMessageAction(this);
    }
}
