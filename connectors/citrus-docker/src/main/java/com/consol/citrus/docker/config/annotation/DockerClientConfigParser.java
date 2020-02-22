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

package com.consol.citrus.docker.config.annotation;

import com.consol.citrus.config.annotation.AbstractAnnotationConfigParser;
import com.consol.citrus.context.ReferenceResolver;
import com.consol.citrus.docker.client.DockerClient;
import com.consol.citrus.docker.client.DockerClientBuilder;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class DockerClientConfigParser extends AbstractAnnotationConfigParser<DockerClientConfig, DockerClient> {

    /**
     * Constructor matching super.
     * @param referenceResolver
     */
    public DockerClientConfigParser(ReferenceResolver referenceResolver) {
        super(referenceResolver);
    }

    @Override
    public DockerClient parse(DockerClientConfig annotation) {
        DockerClientBuilder builder = new DockerClientBuilder();

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

        if (StringUtils.hasText(annotation.email())) {
            builder.email(annotation.email());
        }

        if (StringUtils.hasText(annotation.registry())) {
            builder.registry(annotation.registry());
        }

        builder.verifyTls(annotation.verifyTls());

        if (StringUtils.hasText(annotation.certPath())) {
            builder.certPath(annotation.certPath());
        }

        if (StringUtils.hasText(annotation.configPath())) {
            builder.configPath(annotation.configPath());
        }

        return builder.initialize().build();
    }
}
