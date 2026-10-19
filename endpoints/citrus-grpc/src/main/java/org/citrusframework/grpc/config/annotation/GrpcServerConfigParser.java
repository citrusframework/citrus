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

import java.util.Arrays;

import org.citrusframework.TestActor;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.context.TestContext;
import org.citrusframework.grpc.server.GrpcServer;
import org.citrusframework.grpc.server.GrpcServerBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.StringUtils;

/**
 * Parses {@link GrpcServerConfig} annotations into gRPC simulator endpoints.
 */
public class GrpcServerConfigParser implements AnnotationConfigParser<GrpcServerConfig, GrpcServer> {

    @Override
    public GrpcServer parse(GrpcServerConfig annotation, ReferenceResolver referenceResolver, TestContext context) {
        GrpcServerBuilder builder = new GrpcServerBuilder();

        builder.port(annotation.port());
        if (annotation.services().length > 0) {
            builder.services(annotation.services());
        }
        if (annotation.descriptors().length > 0) {
            builder.setDescriptors(Arrays.stream(annotation.descriptors())
                    .map(context::replaceDynamicContentInString).toList());
        }
        if (annotation.protoPaths().length > 0) {
            builder.setProtoPaths(Arrays.stream(annotation.protoPaths())
                    .map(context::replaceDynamicContentInString).toList());
        }
        builder.tls(annotation.tls());
        if (StringUtils.hasText(annotation.certificateChain())) {
            builder.certificateChain(context.replaceDynamicContentInString(annotation.certificateChain()));
        }
        if (StringUtils.hasText(annotation.privateKey())) {
            builder.privateKey(context.replaceDynamicContentInString(annotation.privateKey()));
        }
        builder.omitDefaultValues(annotation.omitDefaultValues());
        builder.timeout(annotation.timeout());
        builder.autoStart(annotation.autoStart());

        if (StringUtils.hasText(annotation.endpointAdapter())) {
            builder.endpointAdapter(annotation.endpointAdapter());
        }

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        builder.referenceResolver(referenceResolver);
        return builder.initialize().build();
    }
}
