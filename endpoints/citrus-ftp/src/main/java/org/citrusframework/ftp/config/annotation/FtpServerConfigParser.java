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

package org.citrusframework.ftp.config.annotation;

import org.apache.ftpserver.ftplet.UserManager;
import org.citrusframework.TestActor;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.ftp.server.FtpServer;
import org.citrusframework.ftp.server.FtpServerBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class FtpServerConfigParser implements AnnotationConfigParser<FtpServerConfig, FtpServer> {

    @Override
    public FtpServer parse(FtpServerConfig annotation, ReferenceResolver referenceResolver) {
        FtpServerBuilder builder = new FtpServerBuilder();

        builder.autoStart(annotation.autoStart());
        builder.autoConnect(annotation.autoConnect());
        builder.autoLogin(annotation.autoLogin());
        builder.timeout(annotation.timeout());

        builder.autoHandleCommands(annotation.autoHandleCommands());

        builder.port(annotation.port());
        builder.debugLogging(annotation.debugLogging());

        if (StringUtils.hasText(annotation.endpointAdapter())) {
            builder.endpointAdapter(referenceResolver.resolve(annotation.endpointAdapter(), EndpointAdapter.class));
        }

        if (StringUtils.hasText(annotation.server())) {
            builder.server(referenceResolver.resolve(annotation.server(), org.apache.ftpserver.FtpServer.class));
        }

        if (StringUtils.hasText(annotation.userManager())) {
            builder.userManager(referenceResolver.resolve(annotation.userManager(), UserManager.class));
        }

        if (StringUtils.hasText(annotation.userManagerProperties())) {
            builder.userManagerProperties(Resources.create(annotation.userManagerProperties()));
        }

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(referenceResolver.resolve(annotation.actor(), TestActor.class));
        }

        return builder.initialize().build();
    }
}
