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

package org.citrusframework.testcontainers.aws2;

import java.util.Map;
import java.util.Optional;

import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testcontainers.aws2.client.DefaultClientFactoryFinder;

@FunctionalInterface
public interface ClientFactory<T> {

    /**
     * Create client for given LocalStackContainer.
     * @param container
     * @param options
     * @return
     */
    T createClient(LocalStackContainer container, Map<String, String> options);

    /**
     * Checks if created client is suitable for given service.
     * @param service
     * @return
     */
    default boolean supports(LocalStackContainer.Service service) {
        return service != null;
    }

    static Optional<ClientFactory<?>> lookup(ReferenceResolver referenceResolver, LocalStackContainer.Service service) {
        Optional<ClientFactory<?>> clientFactoryBean = referenceResolver.resolveAll(ClientFactory.class).values()
                .stream()
                .filter(factory -> factory.supports(service))
                .findFirst()
                .map(factory -> (ClientFactory<?>) factory);

        if (clientFactoryBean.isPresent()) {
            return clientFactoryBean;
        }

        return lookup(service);
    }

    static Optional<ClientFactory<?>> lookup(LocalStackContainer.Service service) {
        return new DefaultClientFactoryFinder().find(service);
    }
}
