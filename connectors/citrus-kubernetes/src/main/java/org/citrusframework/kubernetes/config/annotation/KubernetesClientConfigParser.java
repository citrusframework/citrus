/*
 * Copyright 2006-2016 the original author or authors.
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
import org.citrusframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class KubernetesClientConfigParser implements AnnotationConfigParser<KubernetesClientConfig, KubernetesClient> {

    @Override
    public KubernetesClient parse(KubernetesClientConfig annotation, ReferenceResolver referenceResolver) {
        KubernetesClientBuilder builder = new KubernetesClientBuilder();

        if (StringUtils.hasText(annotation.url())) {
            builder.url(annotation.url());
        }

        if (StringUtils.hasText(annotation.version())) {
            builder.version(annotation.version());
        }

        if (StringUtils.hasText(annotation.username())) {
            builder.username(annotation.username());
        }

        if (StringUtils.hasText(annotation.password())) {
            builder.password(annotation.password());
        }

        if (StringUtils.hasText(annotation.namespace())) {
            builder.namespace(annotation.namespace());
        }

        if (StringUtils.hasText(annotation.certFile())) {
            builder.certFile(annotation.certFile());
        }

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(referenceResolver.resolve(annotation.messageConverter(), KubernetesMessageConverter.class));
        }

        if (StringUtils.hasText(annotation.objectMapper())) {
            builder.objectMapper(referenceResolver.resolve(annotation.objectMapper(), ObjectMapper.class));
        }

        return builder.build();
    }
}
