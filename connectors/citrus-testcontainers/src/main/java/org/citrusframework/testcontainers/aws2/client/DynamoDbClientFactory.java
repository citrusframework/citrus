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

import java.util.Map;

import org.citrusframework.testcontainers.aws2.ClientFactory;
import org.citrusframework.testcontainers.aws2.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.StreamSpecification;
import software.amazon.awssdk.services.dynamodb.model.StreamViewType;

public class DynamoDbClientFactory implements ClientFactory<DynamoDbClient> {

    @Override
    public DynamoDbClient createClient(LocalStackContainer container, Map<String, String> options) {
        DynamoDbClient ddbClient = DynamoDbClient.builder()
                .endpointOverride(container.getServiceEndpoint())
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(container.getAccessKey(), container.getSecretKey())
                        )
                )
                .region(Region.of(container.getRegion()))
                .build();

        if (options.containsKey("tables")) {
            String[] tables = options.get("tables").split(",");
            for (String table : tables) {
                ddbClient.createTable(b -> {
                    b.tableName(table);

                    String idAttr = options.getOrDefault("%s.id".formatted(table), "id");

                    b.keySchema(KeySchemaElement.builder().attributeName(idAttr).keyType(KeyType.HASH).build());
                    b.attributeDefinitions(AttributeDefinition.builder().attributeName(idAttr).attributeType(ScalarAttributeType.N).build());

                    StreamViewType viewType = StreamViewType.valueOf(options.getOrDefault("%s.view.type".formatted(table),
                            StreamViewType.NEW_AND_OLD_IMAGES.name()));

                    b.streamSpecification(StreamSpecification.builder()
                            .streamEnabled(true)
                            .streamViewType(viewType).build());

                    long readCapacity = Long.parseLong(options.getOrDefault("%s.read.capacity".formatted(table), "1"));
                    long writeCapacity = Long.parseLong(options.getOrDefault("%s.write.capacity".formatted(table), "1"));

                    b.provisionedThroughput(
                            ProvisionedThroughput.builder()
                                    .readCapacityUnits(readCapacity)
                                    .writeCapacityUnits(writeCapacity).build());
                });
            }
        }

        return ddbClient;
    }

    @Override
    public boolean supports(LocalStackContainer.Service service) {
        return LocalStackContainer.Service.DYNAMODB == service;
    }
}
