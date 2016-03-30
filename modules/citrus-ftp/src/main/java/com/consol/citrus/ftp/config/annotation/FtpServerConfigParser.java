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

package com.consol.citrus.ftp.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.config.annotation.AbstractAnnotationConfigParser;
import com.consol.citrus.context.ReferenceResolver;
import com.consol.citrus.endpoint.EndpointAdapter;
import com.consol.citrus.ftp.server.FtpServer;
import com.consol.citrus.ftp.server.FtpServerBuilder;
import org.apache.ftpserver.ftplet.UserManager;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class FtpServerConfigParser extends AbstractAnnotationConfigParser<FtpServerConfig, FtpServer> {

    /**
     * Constructor matching super.
     * @param referenceResolver
     */
    public FtpServerConfigParser(ReferenceResolver referenceResolver) {
        super(referenceResolver);
    }

    @Override
    public FtpServer parse(FtpServerConfig annotation) {
        FtpServerBuilder builder = new FtpServerBuilder();

        builder.autoStart(annotation.autoStart());
        builder.timeout(annotation.timeout());

        builder.port(annotation.port());

        if (StringUtils.hasText(annotation.endpointAdapter())) {
            builder.endpointAdapter(getReferenceResolver().resolve(annotation.endpointAdapter(), EndpointAdapter.class));
        }

        if (StringUtils.hasText(annotation.server())) {
            builder.server(getReferenceResolver().resolve(annotation.server(), org.apache.ftpserver.FtpServer.class));
        }

        if (StringUtils.hasText(annotation.userManager())) {
            builder.userManager(getReferenceResolver().resolve(annotation.userManager(), UserManager.class));
        }

        if (StringUtils.hasText(annotation.userManagerProperties())) {
            builder.userManagerProperties(new PathMatchingResourcePatternResolver().getResource(annotation.userManagerProperties()));
        }

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(getReferenceResolver().resolve(annotation.actor(), TestActor.class));
        }

        return builder.build();
    }
}
