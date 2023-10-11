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
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.ssh.message.SshMessageConverter;
import org.citrusframework.ssh.server.SshServer;
import org.citrusframework.ssh.server.SshServerBuilder;
import org.citrusframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class SshServerConfigParser implements AnnotationConfigParser<SshServerConfig, SshServer> {

    @Override
    public SshServer parse(SshServerConfig annotation, ReferenceResolver referenceResolver) {
        SshServerBuilder builder = new SshServerBuilder();

        builder.port(annotation.port());

        if (StringUtils.hasText(annotation.user())) {
            builder.user(annotation.user());
        }

        if (StringUtils.hasText(annotation.password())) {
            builder.password(annotation.password());
        }

        if (StringUtils.hasText(annotation.hostKeyPath())) {
            builder.hostKeyPath(annotation.hostKeyPath());
        }

        if (StringUtils.hasText(annotation.userHomePath())) {
            builder.userHomePath(annotation.userHomePath());
        }

        if (StringUtils.hasText(annotation.allowedKeyPath())) {
            builder.allowedKeyPath(annotation.allowedKeyPath());
        }

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(referenceResolver.resolve(annotation.messageConverter(), SshMessageConverter.class));
        }

        builder.pollingInterval(annotation.pollingInterval());

        builder.debugLogging(annotation.debugLogging());

        if (StringUtils.hasText(annotation.endpointAdapter())) {
            builder.endpointAdapter(referenceResolver.resolve(annotation.endpointAdapter(), EndpointAdapter.class));
        }

        builder.autoStart(annotation.autoStart());
        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        return builder.initialize().build();
    }
}
