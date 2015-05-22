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

import com.consol.citrus.arquillian.configuration.CitrusConfiguration;
import com.consol.citrus.arquillian.shrinkwrap.CitrusArchiveBuilder;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Archive processor automatically adds Citrus libraries to deployable archive. According to extension configuration
 * explicit Citrus version is loaded and libraries are added automatically to enterprise or web archive.
 *
 * @author Christoph Deppisch
 * @since 2.2
 */
public class CitrusArchiveProcessor implements ApplicationArchiveProcessor {

    @Inject
    private Instance<CitrusConfiguration> configurationInstance;

    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        if (getConfiguration().isAutoPackage() &&
                (applicationArchive instanceof EnterpriseArchive || applicationArchive instanceof WebArchive)) {
            addDependencies(applicationArchive);
        }
    }

    /**
     * Adds Citrus archive dependencies and all transitive dependencies to archive.
     * @param archive
     */
    protected void addDependencies(Archive<?> archive) {
        String version = getConfiguration().getCitrusVersion();
        CitrusArchiveBuilder archiveBuilder;

        if (version != null) {
            archiveBuilder = CitrusArchiveBuilder.version(version);
        } else {
            archiveBuilder = CitrusArchiveBuilder.latestVersion();
        }

        if (archive instanceof EnterpriseArchive) {
            EnterpriseArchive ear = (EnterpriseArchive) archive;
            ear.addAsModules(archiveBuilder.all().build());
        } else if (archive instanceof WebArchive) {
            WebArchive war = (WebArchive) archive;
            war.addAsLibraries(archiveBuilder.all().build());
        }
    }

    /**
     * Gets the extension configuration.
     * @return
     */
    public CitrusConfiguration getConfiguration() {
        return configurationInstance.get();
    }

}
