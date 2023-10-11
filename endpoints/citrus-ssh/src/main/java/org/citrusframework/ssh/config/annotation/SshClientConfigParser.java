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

package org.citrusframework.ssh.config.annotation;

import org.citrusframework.TestActor;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.ssh.client.SshClient;
import org.citrusframework.ssh.client.SshClientBuilder;
import org.citrusframework.ssh.message.SshMessageConverter;
import org.citrusframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class SshClientConfigParser implements AnnotationConfigParser<SshClientConfig, SshClient> {

    @Override
    public SshClient parse(SshClientConfig annotation, ReferenceResolver referenceResolver) {
        SshClientBuilder builder = new SshClientBuilder();

        if (StringUtils.hasText(annotation.host())) {
            builder.host(annotation.host());
        }

        builder.port(annotation.port());

        if (StringUtils.hasText(annotation.privateKeyPath())) {
            builder.privateKeyPath(annotation.privateKeyPath());
        }

        if (StringUtils.hasText(annotation.privateKeyPassword())) {
            builder.privateKeyPassword(annotation.privateKeyPassword());
        }

        builder.strictHostChecking(annotation.strictHostChecking());

        if (StringUtils.hasText(annotation.knownHosts())) {
            builder.knownHosts(annotation.knownHosts());
        }

        builder.commandTimeout(annotation.commandTimeout());
        builder.connectionTimeout(annotation.connectionTimeout());

        if (StringUtils.hasText(annotation.user())) {
            builder.user(annotation.user());
        }

        if (StringUtils.hasText(annotation.password())) {
            builder.password(annotation.password());
        }

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(referenceResolver.resolve(annotation.messageConverter(), SshMessageConverter.class));
        }

        if (StringUtils.hasText(annotation.correlator())) {
            builder.correlator(referenceResolver.resolve(annotation.correlator(), MessageCorrelator.class));
        }

        builder.pollingInterval(annotation.pollingInterval());

        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        return builder.initialize().build();
    }
}
