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
import software.amazon.awssdk.services.sns.SnsClient;

public class SnsClientFactory implements ClientFactory<SnsClient> {

    @Override
    public SnsClient createClient(LocalStackContainer container, Map<String, String> options) {
        SnsClient snsClient = SnsClient.builder()
                .endpointOverride(container.getServiceEndpoint())
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(container.getAccessKey(), container.getSecretKey())
                        )
                )
                .region(Region.of(container.getRegion()))
                .build();

        if (options.containsKey("topics")) {
            String[] topics = options.get("topics").split(",");
            for (String topic : topics) {
                snsClient.createTopic(builder -> builder.name(topic));
            }
        }

        return snsClient;
    }

    @Override
    public boolean supports(LocalStackContainer.Service service) {
        return LocalStackContainer.Service.SNS == service;
    }
}
