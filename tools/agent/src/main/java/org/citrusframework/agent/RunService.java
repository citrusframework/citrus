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

package org.citrusframework.agent;

import java.net.MalformedURLException;
import java.util.List;

import org.apache.camel.tooling.maven.MavenArtifact;
import org.citrusframework.agent.util.ConfigurationHelper;
import org.citrusframework.main.TestEngine;
import org.citrusframework.main.TestRunConfiguration;
import org.citrusframework.util.ClassLoaderHelper;
import org.citrusframework.xml.actions.XmlTestActionBuilder;
import org.citrusframework.yaml.actions.YamlTestActionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunService {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(RunService.class);

    /**
     * Run Citrus application with given configuration and cached Citrus instance.
     */
    public void run(TestRunConfiguration configuration) {
        try {
            resolveArtifacts(configuration);

            TestEngine engine = TestEngine.lookup(configuration);
            engine.run();
        } finally {
            if (!configuration.getModules().isEmpty() || !configuration.getDependencies().isEmpty()) {
                ClassLoaderHelper.reset();
            }
        }
    }

    private void resolveArtifacts(TestRunConfiguration runConfiguration) {
        if (!runConfiguration.getModules().isEmpty() || !runConfiguration.getDependencies().isEmpty()) {
            // Resolve with new modules and artifacts
            List<MavenArtifact> artifacts = ConfigurationHelper.resolveArtifacts(
                    runConfiguration.getModules(), runConfiguration.getDependencies());
            for (MavenArtifact artifact : artifacts) {
                try {
                    ClassLoaderHelper.addArtifact(artifact.toString(), artifact.getFile().toURI().toURL(), false);
                } catch (MalformedURLException e) {
                    logger.warn(String.format("Error resolving artifact %s due to '%s'", artifact, e.getMessage()));
                }
            }

            // Update context class loader and clear cache for resource path lookup
            if (ClassLoaderHelper.updateContextClassloader(true)) {
                XmlTestActionBuilder.clearCache();
                YamlTestActionBuilder.clearCache();
            }
        }
    }

}
