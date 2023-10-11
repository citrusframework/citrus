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
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.ftp.server.SftpServer;
import org.citrusframework.ftp.server.SftpServerBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class SftpServerConfigParser implements AnnotationConfigParser<SftpServerConfig, SftpServer> {

    @Override
    public SftpServer parse(SftpServerConfig annotation, ReferenceResolver referenceResolver) {
        SftpServerBuilder builder = new SftpServerBuilder();

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

        builder.pollingInterval(annotation.pollingInterval());

        builder.debugLogging(annotation.debugLogging());

        if (StringUtils.hasText(annotation.endpointAdapter())) {
            builder.endpointAdapter(referenceResolver.resolve(annotation.endpointAdapter(), EndpointAdapter.class));
        }

        builder.autoStart(annotation.autoStart());
        builder.autoConnect(annotation.autoConnect());
        builder.autoLogin(annotation.autoLogin());
        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        return builder.initialize().build();
    }
}
