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

package org.citrusframework.testcontainers.redpanda;

import org.citrusframework.context.TestContext;
import org.citrusframework.testcontainers.TestContainersSettings;
import org.citrusframework.testcontainers.actions.StartTestcontainersAction;
import org.citrusframework.testcontainers.postgresql.PostgreSQLSettings;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.redpanda.RedpandaContainer;
import org.testcontainers.utility.DockerImageName;

public class StartRedpandaAction extends StartTestcontainersAction<RedpandaContainer> {

    public StartRedpandaAction(Builder builder) {
        super(builder);
    }

    @Override
    protected void exposeConnectionSettings(RedpandaContainer container, TestContext context) {
        RedpandaSettings.exposeConnectionSettings(container, serviceName, context);
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractBuilder<RedpandaContainer, StartRedpandaAction, Builder> {

        private String redpandaVersion = RedpandaSettings.getRedpandaVersion();

        public Builder() {
            withStartupTimeout(PostgreSQLSettings.getStartupTimeout());
        }

        public Builder version(String redpandaVersion) {
           this.redpandaVersion = redpandaVersion;
           return this;
        }

        @Override
        protected void prepareBuild() {
            if (containerName == null) {
                containerName(RedpandaSettings.getContainerName());
            }

            if (serviceName == null) {
                serviceName(RedpandaSettings.getServiceName());
            }

            if (image == null) {
                image(RedpandaSettings.getImageName());
            }

            withLabel("app", "citrus");
            withLabel("com.joyrex2001.kubedock.name-prefix", serviceName);
            withLabel("app.kubernetes.io/name", "redpanda");
            withLabel("app.kubernetes.io/part-of", TestContainersSettings.getTestName());
            withLabel("app.openshift.io/connects-to", TestContainersSettings.getTestId());

            RedpandaContainer redpandaContainer;
            if (referenceResolver != null && referenceResolver.isResolvable(containerName, RedpandaContainer.class)) {
                redpandaContainer = referenceResolver.resolve(containerName, RedpandaContainer.class);
            } else {
                DockerImageName imageName;
                if (TestContainersSettings.isRegistryMirrorEnabled()) {
                    // make sure the mirror image is declared as compatible with original image
                    imageName = DockerImageName.parse(image).withTag(redpandaVersion)
                            .asCompatibleSubstituteFor(DockerImageName.parse("redpandadata/redpanda"));
                } else {
                    imageName = DockerImageName.parse(image).withTag(redpandaVersion);
                }

                redpandaContainer = new RedpandaContainer(imageName)
                        .withNetwork(network)
                        .withNetworkAliases(serviceName)
                        .waitingFor(Wait.forLogMessage(".*Successfully started Redpanda!.*", 1)
                                .withStartupTimeout(startupTimeout))
                        // TODO: Remove once Redpanda container works with Podman
                        . withCreateContainerCmdModifier(cmd -> {
                            cmd.withEntrypoint();
                            cmd.withEntrypoint("/entrypoint-tc.sh");
                            cmd.withUser("root:root");
                        })
                        .withCommand("redpanda", "start", "--mode=dev-container", "--smp=1", "--memory=1G");
            }

            container(redpandaContainer);
        }

        @Override
        public StartRedpandaAction doBuild() {
            return new StartRedpandaAction(this);
        }
    }
}
