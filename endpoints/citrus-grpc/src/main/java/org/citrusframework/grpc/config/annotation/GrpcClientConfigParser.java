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
package org.citrusframework.grpc.config.annotation;

import java.time.Duration;
import java.util.Arrays;

import org.citrusframework.TestActor;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.context.TestContext;
import org.citrusframework.grpc.client.GrpcClient;
import org.citrusframework.grpc.client.GrpcClientBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.StringUtils;

/**
 * Parses {@link GrpcClientConfig} annotations into gRPC client endpoints.
 */
public class GrpcClientConfigParser implements AnnotationConfigParser<GrpcClientConfig, GrpcClient> {

    @Override
    public GrpcClient parse(GrpcClientConfig annotation, ReferenceResolver referenceResolver, TestContext context) {
        GrpcClientBuilder builder = new GrpcClientBuilder();

        if (StringUtils.hasText(annotation.target())) {
            builder.target(context.replaceDynamicContentInString(annotation.target()));
        }
        if (annotation.descriptors().length > 0) {
            builder.setDescriptors(Arrays.stream(annotation.descriptors())
                    .map(context::replaceDynamicContentInString).toList());
        }
        if (annotation.protoPaths().length > 0) {
            builder.setProtoPaths(Arrays.stream(annotation.protoPaths())
                    .map(context::replaceDynamicContentInString).toList());
        }
        if (annotation.deadline() > 0) {
            builder.deadline(Duration.ofMillis(annotation.deadline()));
        }
        builder.tls(annotation.tls());
        if (StringUtils.hasText(annotation.trustCertificate())) {
            builder.trustCertificate(context.replaceDynamicContentInString(annotation.trustCertificate()));
        }
        builder.omitDefaultValues(annotation.omitDefaultValues());
        builder.maxBufferedMessages(annotation.maxBufferedMessages());
        builder.timeout(annotation.timeout());
        builder.pollingInterval(annotation.pollingInterval());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        builder.referenceResolver(referenceResolver);
        return builder.initialize().build();
    }
}
