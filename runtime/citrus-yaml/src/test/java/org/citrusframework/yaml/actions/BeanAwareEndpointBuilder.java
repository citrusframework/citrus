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

package org.citrusframework.yaml.actions;

import org.citrusframework.api.common.Named;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointBuilder;
import org.citrusframework.endpoint.direct.DirectEndpoint;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

/**
 * Test endpoint builder that resolves a bean from the bean registry when the endpoint is built,
 * similar to endpoint builders that resolve e.g. a marshaller bean by name.
 */
public class BeanAwareEndpointBuilder implements EndpointBuilder<Endpoint>, ReferenceResolverAware, Named {

    private String name;
    private String beanName;
    private ReferenceResolver referenceResolver;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public Endpoint build() {
        FooBean bean = referenceResolver.resolve(beanName, FooBean.class);

        DirectEndpoint endpoint = new DirectEndpoint();
        endpoint.setName(name);
        endpoint.getEndpointConfiguration().setQueueName(bean.getMessage());
        return endpoint;
    }

    @Override
    public boolean supports(Class<?> endpointType) {
        return false;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
