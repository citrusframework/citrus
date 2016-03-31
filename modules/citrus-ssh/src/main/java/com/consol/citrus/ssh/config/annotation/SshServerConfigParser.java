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

package com.consol.citrus.ssh.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.config.annotation.AbstractAnnotationConfigParser;
import com.consol.citrus.context.ReferenceResolver;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.ssh.message.SshMessageConverter;
import com.consol.citrus.ssh.server.SshServer;
import com.consol.citrus.ssh.server.SshServerBuilder;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class SshServerConfigParser extends AbstractAnnotationConfigParser<SshServerConfig, SshServer> {

    /**
     * Constructor matching super.
     * @param referenceResolver
     */
    public SshServerConfigParser(ReferenceResolver referenceResolver) {
        super(referenceResolver);
    }

    @Override
    public SshServer parse(SshServerConfig annotation) {
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

        if (StringUtils.hasText(annotation.allowedKeyPath())) {
            builder.allowedKeyPath(annotation.allowedKeyPath());
        }

        if (StringUtils.hasText(annotation.messageConverter())) {
            builder.messageConverter(getReferenceResolver().resolve(annotation.messageConverter(), SshMessageConverter.class));
        }

        builder.pollingInterval(annotation.pollingInterval());

        if (StringUtils.hasText(annotation.endpointAdapter())) {
            builder.endpointAdapter(getReferenceResolver().resolve(annotation.endpointAdapter(), EndpointAdapter.class));
        }

        builder.autoStart(annotation.autoStart());
        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(getReferenceResolver().resolve(annotation.actor(), TestActor.class));
        }

        return builder.build();
    }
}
