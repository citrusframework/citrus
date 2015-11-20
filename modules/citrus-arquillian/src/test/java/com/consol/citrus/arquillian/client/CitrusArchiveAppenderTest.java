/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.arquillian.client;

import com.consol.citrus.arquillian.CitrusExtensionConstants;
import com.consol.citrus.arquillian.configuration.CitrusConfiguration;
import com.consol.citrus.arquillian.container.CitrusRemoteConfigurationProducer;
import com.consol.citrus.arquillian.container.CitrusRemoteExtension;
import com.consol.citrus.arquillian.enricher.CitrusRemoteInstanceProducer;
import com.consol.citrus.arquillian.enricher.CitrusTestEnricher;
import com.consol.citrus.arquillian.helper.InjectionHelper;
import com.consol.citrus.arquillian.lifecycle.CitrusRemoteLifecycleHandler;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.shrinkwrap.api.*;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.mockito.Mockito;
import org.springframework.util.ReflectionUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

import static org.mockito.Mockito.*;

public class CitrusArchiveAppenderTest {

    private CitrusArchiveAppender archiveAppender = new CitrusArchiveAppender();

    /** Required extension classes */
    private final static List<Class<?>> REQUIRED_CLASSES = Arrays.asList(
            CitrusExtensionConstants.class, CitrusConfiguration.class, CitrusRemoteInstanceProducer.class,
            CitrusRemoteLifecycleHandler.class, CitrusTestEnricher.class,
            CitrusRemoteConfigurationProducer.class, CitrusRemoteExtension.class,
            ReflectionUtils.class);

    @Test
    public void testBuildArchive() throws Exception {
        CitrusConfiguration configuration = CitrusConfiguration.from(new Properties());
        Instance<CitrusConfiguration> configurationInstance = Mockito.mock(Instance.class);
        when(configurationInstance.get()).thenReturn(configuration);

        InjectionHelper.inject(archiveAppender, "configurationInstance", configurationInstance);

        Archive archive = archiveAppender.createAuxiliaryArchive();

        Assert.assertNotNull(archive);
        Assert.assertTrue(archive instanceof JavaArchive);

        for (Class required : REQUIRED_CLASSES) {
            Assert.assertTrue(archive.contains(getArchivePath(required)), String.format("Missing required class '%s' in auxiliary archive", required));
        }

        Assert.assertTrue(archive.contains("/" + CitrusExtensionConstants.CITRUS_REMOTE_PROPERTIES));
    }

    private ArchivePath getArchivePath(Class type) {
        StringBuilder sb = new StringBuilder()
                .append("/")
                .append(type.getName().replace(".", "/"))
                .append(".class");

        return ArchivePaths.create(sb.toString());
    }
}