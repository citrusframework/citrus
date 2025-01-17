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

package org.citrusframework.testcontainers.postgresql;

import java.io.IOException;
import javax.script.ScriptException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.testcontainers.TestContainersSettings;
import org.citrusframework.testcontainers.actions.StartTestcontainersAction;
import org.citrusframework.util.FileUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.ext.ScriptUtils;
import org.testcontainers.jdbc.JdbcDatabaseDelegate;
import org.testcontainers.utility.DockerImageName;

public class StartPostgreSQLAction extends StartTestcontainersAction<PostgreSQLContainer<?>> {

    private final String dataSourceName;
    private final String initScript;
    private final Resource initScriptResource;

    public StartPostgreSQLAction(Builder builder) {
        super(builder);

        this.dataSourceName = builder.dataSourceName;
        this.initScript = builder.initScript;
        this.initScriptResource = builder.initScriptResource;
    }

    @Override
    public void doExecute(TestContext context) {
        super.doExecute(context);

        try {
            String resolvedInitScript = "";
            if (initScript != null) {
                resolvedInitScript = context.replaceDynamicContentInString(initScript);
            } else if (initScriptResource != null) {
                resolvedInitScript = context.replaceDynamicContentInString(FileUtils.readToString(initScriptResource));
            }

            if (!resolvedInitScript.isEmpty()) {
                try {
                    ScriptUtils.executeDatabaseScript(new JdbcDatabaseDelegate(getContainer(), ""), "init.sql", resolvedInitScript);
                } catch (ScriptException e) {
                    throw new CitrusRuntimeException("Failed to execute init script");
                }
            }
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read init script", e);
        }
    }

    @Override
    protected void exposeConnectionSettings(PostgreSQLContainer<?> container, TestContext context) {
        PostgreSQLSettings.exposeConnectionSettings(container, serviceName, context);

        BasicDataSource postgreSQLDataSource = new BasicDataSource();
        postgreSQLDataSource.setDriverClassName(getContainer().getDriverClassName());
        postgreSQLDataSource.setUrl(getContainer().getJdbcUrl());
        postgreSQLDataSource.setUsername(getContainer().getUsername());
        postgreSQLDataSource.setPassword(getContainer().getPassword());

        context.getReferenceResolver().bind(dataSourceName, postgreSQLDataSource);
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractBuilder<PostgreSQLContainer<?>, StartPostgreSQLAction, Builder> {

        private String postgreSQLVersion = PostgreSQLSettings.getPostgreSQLVersion();

        private String dataSourceName = "postgreSQL";
        private String databaseName = PostgreSQLSettings.getDatabaseName();
        private String username = PostgreSQLSettings.getUsername();
        private String password = PostgreSQLSettings.getPassword();

        private String initScript;
        private Resource initScriptResource;

        public Builder() {
            withStartupTimeout(PostgreSQLSettings.getStartupTimeout());
        }

        public Builder version(String postgreSQLVersion) {
           this.postgreSQLVersion = postgreSQLVersion;
           return this;
        }

        public Builder databaseName(String databaseName) {
           this.databaseName = databaseName;
           return this;
        }

        public Builder dataSourceName(String dataSourceName) {
           this.dataSourceName = dataSourceName;
           return this;
        }

        public Builder username(String username) {
           this.username = username;
           return this;
        }

        public Builder password(String password) {
           this.password = password;
           return this;
        }

        public Builder initScript(String initScript) {
           this.initScript = initScript;
           return this;
        }

        public Builder initScript(Resource resource) {
           this.initScriptResource = resource;
           return this;
        }

        public Builder loadInitScript(String resource) {
           this.initScriptResource = Resources.create(resource);
           return this;
        }

        @Override
        protected void prepareBuild() {
            if (containerName == null) {
                containerName(PostgreSQLSettings.getContainerName());
            }

            if (serviceName == null) {
                serviceName(PostgreSQLSettings.getServiceName());
            }

            if (image == null) {
                image(PostgreSQLSettings.getImageName());
            }

            env.putIfAbsent("PGDATA", "/var/lib/postgresql/data/mydata");

            withLabel("app", "citrus");
            withLabel("com.joyrex2001.kubedock.name-prefix", serviceName);
            withLabel("app.kubernetes.io/name", "postgresql");
            withLabel("app.kubernetes.io/part-of", TestContainersSettings.getTestName());
            withLabel("app.openshift.io/connects-to", TestContainersSettings.getTestId());

            PostgreSQLContainer<?> postgreSQLContainer;
            if (referenceResolver != null && referenceResolver.isResolvable(containerName, PostgreSQLContainer.class)) {
                postgreSQLContainer = referenceResolver.resolve(containerName, PostgreSQLContainer.class);
            } else {
                DockerImageName imageName;
                if (TestContainersSettings.isRegistryMirrorEnabled()) {
                    // make sure the mirror image is declared as compatible with original image
                    imageName = DockerImageName.parse(image).withTag(postgreSQLVersion)
                            .asCompatibleSubstituteFor(DockerImageName.parse("postgres"));
                } else {
                    imageName =
                        DockerImageName.parse(image).withTag(postgreSQLVersion);
                }

                postgreSQLContainer = new PostgreSQLContainer<>(imageName)
                        .withUsername(username)
                        .withPassword(password)
                        .withDatabaseName(databaseName)
                        .withNetwork(network)
                        .withNetworkAliases(serviceName)
                        .waitingFor(Wait.forListeningPort()
                                .withStartupTimeout(startupTimeout));
            }

            container(postgreSQLContainer);
        }

        @Override
        public StartPostgreSQLAction doBuild() {
            return new StartPostgreSQLAction(this);
        }
    }
}
