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

package org.citrusframework.testcontainers.mongodb;

import org.citrusframework.context.TestContext;
import org.citrusframework.testcontainers.TestContainersSettings;
import org.citrusframework.testcontainers.actions.StartTestcontainersAction;
import org.citrusframework.testcontainers.postgresql.PostgreSQLSettings;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class StartMongoDBAction extends StartTestcontainersAction<MongoDBContainer> {

    public StartMongoDBAction(Builder builder) {
        super(builder);
    }

    @Override
    protected void exposeConnectionSettings(MongoDBContainer container, TestContext context) {
        MongoDBSettings.exposeConnectionSettings(container, serviceName, context);
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractBuilder<MongoDBContainer, StartMongoDBAction, Builder> {

        private String mongoDBVersion = MongoDBSettings.getMongoDBVersion();

        public Builder() {
            withStartupTimeout(PostgreSQLSettings.getStartupTimeout());
        }

        public Builder version(String mongoDBVersion) {
           this.mongoDBVersion = mongoDBVersion;
           return this;
        }

        @Override
        protected void prepareBuild() {
            if (containerName == null) {
                containerName(MongoDBSettings.getContainerName());
            }

            if (serviceName == null) {
                serviceName(MongoDBSettings.getServiceName());
            }

            if (image == null) {
                image(MongoDBSettings.getImageName());
            }

            withLabel("app", "citrus");
            withLabel("com.joyrex2001.kubedock.name-prefix", serviceName);
            withLabel("app.kubernetes.io/name", "mongoDB");
            withLabel("app.kubernetes.io/part-of", TestContainersSettings.getTestName());
            withLabel("app.openshift.io/connects-to", TestContainersSettings.getTestId());

            MongoDBContainer mongoDBContainer;
            if (referenceResolver != null && referenceResolver.isResolvable(containerName, MongoDBContainer.class)) {
                mongoDBContainer = referenceResolver.resolve(containerName, MongoDBContainer.class);
            } else {
                DockerImageName imageName;
                if (TestContainersSettings.isRegistryMirrorEnabled()) {
                    // make sure the mirror image is declared as compatible with original image
                    imageName = DockerImageName.parse(image).withTag(mongoDBVersion)
                            .asCompatibleSubstituteFor(DockerImageName.parse("mongo"));
                } else {
                    imageName = DockerImageName.parse(image).withTag(mongoDBVersion);
                }

                mongoDBContainer = new MongoDBContainer(imageName)
                        .withNetwork(network)
                        .withNetworkAliases(serviceName)
                        .waitingFor(Wait.forLogMessage("(?i).*waiting for connections.*", 1)
                                .withStartupTimeout(startupTimeout));
            }

            container(mongoDBContainer);
        }

        @Override
        public StartMongoDBAction doBuild() {
            return new StartMongoDBAction(this);
        }
    }
}
