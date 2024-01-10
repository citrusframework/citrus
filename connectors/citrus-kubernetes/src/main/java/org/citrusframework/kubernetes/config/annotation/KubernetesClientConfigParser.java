/*
 * Copyright 2006-2024 the original author or authors.
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

package org.citrusframework.kubernetes.config.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.kubernetes.client.KubernetesClientBuilder;
import org.citrusframework.kubernetes.message.KubernetesMessageConverter;
import org.citrusframework.spi.ReferenceResolver;

import static org.citrusframework.kubernetes.config.CredentialValidator.isValid;
import static org.citrusframework.util.StringUtils.hasText;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class KubernetesClientConfigParser implements AnnotationConfigParser<KubernetesClientConfig, KubernetesClient> {

    @Override
    public KubernetesClient parse(KubernetesClientConfig annotation, ReferenceResolver referenceResolver) {
        KubernetesClientBuilder builder = new KubernetesClientBuilder();

        if (!isValid(annotation.username(), annotation.password(), annotation.oauthToken())) {
            throw new IllegalArgumentException("Parameters not set correctly - check if either an oauthToke or password and username is set");
        }

        if (hasText(annotation.url())) {
            builder.url(annotation.url());
        }

        if (hasText(annotation.version())) {
            builder.version(annotation.version());
        }

        if (hasText(annotation.username())) {
            builder.username(annotation.username());
        }

        if (hasText(annotation.password())) {
            builder.password(annotation.password());
        }

        if (hasText(annotation.oauthToken())) {
            builder.oauthToken(annotation.oauthToken());
        }

        if (hasText(annotation.namespace())) {
            builder.namespace(annotation.namespace());
        }

        if (hasText(annotation.certFile())) {
            builder.certFile(annotation.certFile());
        }

        if (hasText(annotation.messageConverter())) {
            builder.messageConverter(referenceResolver.resolve(annotation.messageConverter(), KubernetesMessageConverter.class));
        }

        if (hasText(annotation.objectMapper())) {
            builder.objectMapper(referenceResolver.resolve(annotation.objectMapper(), ObjectMapper.class));
        }

        return builder.build();
    }
}
