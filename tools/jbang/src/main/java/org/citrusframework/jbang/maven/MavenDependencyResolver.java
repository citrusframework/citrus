/*
 * Copyright the original author or authors.
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

package org.citrusframework.jbang.maven;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.camel.tooling.maven.MavenArtifact;
import org.apache.camel.tooling.maven.MavenDownloader;
import org.apache.camel.tooling.maven.MavenDownloaderImpl;
import org.citrusframework.CitrusVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dependency resolver is able to load Maven dependencies as artifacts and add it to the given classloader.
 */
public class MavenDependencyResolver {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(MavenDependencyResolver.class);

    private final MavenDownloader downloader;
    private final Map<String, String> repositories = new LinkedHashMap<>();

    public MavenDependencyResolver() {
        this(new MavenDownloaderImpl());
    }

    public MavenDependencyResolver(MavenDownloader downloader) {
        this.downloader = downloader;

        downloader.build();
    }

    /**
     * Resolves Maven artifact using passed coordinates and use downloaded artifact
     * as one of the URLs in the classLoader, so classes in the artifact become accessible in the classLoader.
     */
    public List<MavenArtifact> resolve(String gav, boolean useSnapshots, boolean transitive) {
        try {
            Set<String> extraRepositories = new LinkedHashSet<>(repositories.values());

            logger.info("Resolving Maven dependency: " + gav);

            List<MavenArtifact> artifacts =
                    downloader.resolveArtifacts(Collections.singletonList(gav), extraRepositories, transitive,
                            useSnapshots);

            if (logger.isDebugEnabled()) {
                artifacts.forEach(mavenArtifact -> logger.debug("Loaded Maven artifact: " + mavenArtifact));
            }

            return artifacts;
        } catch (Throwable e) {
            logger.warn(String.format("Error resolving artifact %s due to %s", gav, e.getMessage()), e);
        }

        return Collections.emptyList();
    }

    /**
     * Resolve Citrus module using the current Citrus version.
     * Automatically adds Maven groupId and version information to build proper Maven gav coordinates for the Citrus module.
     */
    public List<MavenArtifact> resolveModule(String module) {
        String moduleName;
        if (module.startsWith("citrus-")) {
            moduleName = module;
        } else {
            moduleName = "citrus-" + module;
        }

        return resolve("org.citrusframework:%s:%s".formatted(moduleName, CitrusVersion.version()),
                CitrusVersion.version().contains("-SNAPSHOT"), true);
    }

    public MavenDependencyResolver withRepository(String key, String value) {
        repositories.put(key, value);
        return this;
    }
}
