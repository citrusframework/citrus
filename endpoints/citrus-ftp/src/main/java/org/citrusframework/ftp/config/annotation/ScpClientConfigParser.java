/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.ftp.config.annotation;

import org.citrusframework.TestActor;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.ftp.client.ScpClient;
import org.citrusframework.ftp.client.ScpClientBuilder;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class ScpClientConfigParser implements AnnotationConfigParser<ScpClientConfig, ScpClient> {

    @Override
    public ScpClient parse(ScpClientConfig annotation, ReferenceResolver referenceResolver) {
        ScpClientBuilder builder = new ScpClientBuilder();

        if (StringUtils.hasText(annotation.host())) {
            builder.host(annotation.host());
        }

        builder.port(annotation.port());
        builder.portOption(annotation.portOption());

        if (StringUtils.hasText(annotation.username())) {
            builder.username(annotation.username());
        }

        if (StringUtils.hasText(annotation.password())) {
            builder.password(annotation.password());
        }

        if (StringUtils.hasText(annotation.privateKeyPath())) {
            builder.privateKeyPath(annotation.privateKeyPath());
        }

        if (StringUtils.hasText(annotation.privateKeyPassword())) {
            builder.privateKeyPassword(annotation.privateKeyPassword());
        }

        if (StringUtils.hasText(annotation.correlator())) {
            builder.correlator(referenceResolver.resolve(annotation.correlator(), MessageCorrelator.class));
        }

        builder.errorHandlingStrategy(annotation.errorStrategy());

        builder.pollingInterval(annotation.pollingInterval());

        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        return builder.initialize().build();
    }
}
