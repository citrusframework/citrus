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

package org.citrusframework.testcontainers.aws2.client;

import java.util.Optional;

import org.citrusframework.actions.testcontainers.aws2.AwsService;
import org.citrusframework.actions.testcontainers.aws2.ClientFactory;
import org.citrusframework.spi.ReferenceResolver;

/**
 * Client factory resolver resolves factory instances for a given AWS service.
 * Implements mapping logic to find proper client factory instances for a given AWS service
 * Provides logic to resolve factory instances as beans from the bean registry using a reference resolver.
 */
public class DefaultClientFactoryResolver {

    /**
     * Lookup client factory for given service.
     * Tries to resolve the client factory as a bean in the registry using the reference resolver.
     * If not present delegates to lookup logic for given service.
     */
    public Optional<ClientFactory<?>> resolve(ReferenceResolver referenceResolver, AwsService service) {
        Optional<ClientFactory<?>> clientFactoryBean = referenceResolver.resolveAll(ClientFactory.class).values()
                .stream()
                .filter(factory -> factory.supports(service))
                .findFirst()
                .map(factory -> (ClientFactory<?>) factory);

        if (clientFactoryBean.isPresent()) {
            return clientFactoryBean;
        }

        return resolve(service);
    }

    public Optional<ClientFactory<?>> resolve(AwsService service) {
        return switch (service) {
            case S3 -> Optional.of(new S3ClientFactory());
            case SQS -> Optional.of(new SqsClientFactory());
            case SNS -> Optional.of(new SnsClientFactory());
            case KINESIS -> Optional.of(new KinesisClientFactory());
            case EVENT_BRIDGE -> Optional.of(new EventBridgeClientFactory());
            case DYNAMODB -> Optional.of(new DynamoDbClientFactory());
            default -> Optional.empty();
        };
    }

}
