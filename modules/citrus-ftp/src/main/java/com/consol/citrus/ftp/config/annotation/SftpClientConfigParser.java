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

package com.consol.citrus.ftp.config.annotation;

import com.consol.citrus.TestActor;
import com.consol.citrus.config.annotation.AbstractAnnotationConfigParser;
import com.consol.citrus.context.ReferenceResolver;
import com.consol.citrus.ftp.client.SftpClient;
import com.consol.citrus.ftp.client.SftpClientBuilder;
import com.consol.citrus.message.MessageCorrelator;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class SftpClientConfigParser extends AbstractAnnotationConfigParser<SftpClientConfig, SftpClient> {

    /**
     * Constructor matching super.
     * @param referenceResolver
     */
    public SftpClientConfigParser(ReferenceResolver referenceResolver) {
        super(referenceResolver);
    }

    @Override
    public SftpClient parse(SftpClientConfig annotation) {
        SftpClientBuilder builder = new SftpClientBuilder();

        if (StringUtils.hasText(annotation.host())) {
            builder.host(annotation.host());
        }

        builder.port(annotation.port());
        builder.autoReadFiles(annotation.autoReadFiles());
        builder.localPassiveMode(annotation.localPassiveMode());

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

        builder.strictHostChecking(annotation.strictHostChecking());

        if (StringUtils.hasText(annotation.knownHosts())) {
            builder.knownHosts(annotation.knownHosts());
        }

        if (StringUtils.hasText(annotation.preferredAuthentications())) {
            builder.preferredAuthentications(annotation.preferredAuthentications());
        }

        if (StringUtils.hasText(annotation.sessionConfigs())) {
            builder.sessionConfigs(getReferenceResolver().resolve(annotation.sessionConfigs(), Map.class));
        }

        if (StringUtils.hasText(annotation.correlator())) {
            builder.correlator(getReferenceResolver().resolve(annotation.correlator(), MessageCorrelator.class));
        }

        builder.errorHandlingStrategy(annotation.errorStrategy());

        builder.pollingInterval(annotation.pollingInterval());

        builder.timeout(annotation.timeout());

        if (StringUtils.hasText(annotation.actor())) {
            builder.actor(getReferenceResolver().resolve(annotation.actor(), TestActor.class));
        }

        return builder.initialize().build();
    }
}
