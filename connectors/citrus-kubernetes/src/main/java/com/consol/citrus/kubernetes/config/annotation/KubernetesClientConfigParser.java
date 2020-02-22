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

package com.consol.citrus.kubernetes.config.annotation;

import com.consol.citrus.config.annotation.AbstractAnnotationConfigParser;
import com.consol.citrus.context.ReferenceResolver;
import com.consol.citrus.kubernetes.client.KubernetesClient;
import com.consol.citrus.kubernetes.client.KubernetesClientBuilder;
import com.consol.citrus.kubernetes.message.KubernetesMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class KubernetesClientConfigParser extends AbstractAnnotationConfigParser<KubernetesClientConfig, KubernetesClient> {

    /**
     * Constructor matching super.
     * @param referenceResolver
     */
    public KubernetesClientConfigParser(ReferenceResolver referenceResolver) {
        super(referenceResolver);
    }

    @Override
    public KubernetesClient parse(KubernetesClientConfig annotation) {
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
            builder.messageConverter(getReferenceResolver().resolve(annotation.messageConverter(), KubernetesMessageConverter.class));
        }

        if (StringUtils.hasText(annotation.objectMapper())) {
            builder.objectMapper(getReferenceResolver().resolve(annotation.objectMapper(), ObjectMapper.class));
        }

        return builder.build();
    }
}
