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

package org.citrusframework.mcp;

import io.quarkiverse.mcp.server.TextResourceContents;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationResourcesTest {

    private MigrationResources createResources() {
        MigrationResources resources = new MigrationResources();
        resources.migrationData = new MigrationData();
        resources.springBeanXmlDslMigrationData = new SpringBeanXmlDslMigrationData();
        return resources;
    }

    @Test
    void shouldGetMigrationGuide() {
        MigrationResources resources = createResources();

        TextResourceContents result = resources.citrus_docs_migration_guide("5.0");

        assertThat(result).isNotNull();
        assertThat(result.uri()).isEqualTo("citrus://docs/migration-guide/5.0");
        assertThat(result.text()).contains("\"targetVersion\":\"5.0\"");
        assertThat(result.text()).contains("\"packageRenames\"");
        assertThat(result.text()).contains("\"artifactRenames\"");
        assertThat(result.text()).contains("\"dependencyUpgrades\"");
        assertThat(result.text()).contains("\"dslRenames\"");
        assertThat(result.text()).contains("\"propertyRenames\"");
        assertThat(result.text()).contains("\"apiChanges\"");
        assertThat(result.text()).contains("\"spiChanges\"");
    }

    @Test
    void shouldReturnErrorForUnknownVersion() {
        MigrationResources resources = createResources();

        TextResourceContents result = resources.citrus_docs_migration_guide("99.0");

        assertThat(result).isNotNull();
        assertThat(result.uri()).isEqualTo("citrus://docs/migration-guide/99.0");
        assertThat(result.text()).contains("\"error\"");
        assertThat(result.text()).contains("Migration guide not found");
    }

    @Test
    void shouldReturnErrorForBlankVersion() {
        MigrationResources resources = createResources();

        TextResourceContents result = resources.citrus_docs_migration_guide("");

        assertThat(result).isNotNull();
        assertThat(result.text()).contains("\"error\"");
        assertThat(result.text()).contains("Version is required");
    }

    @Test
    void shouldContainPackageRenameDetails() {
        MigrationResources resources = createResources();

        TextResourceContents result = resources.citrus_docs_migration_guide("5.0");

        assertThat(result.text()).contains("org.citrusframework.TestActionSupport");
        assertThat(result.text()).contains("org.citrusframework.dsl.TestActionSupport");
        assertThat(result.text()).contains("citrus-junit-jupiter");
        assertThat(result.text()).contains("camel().cli()");
        assertThat(result.text()).contains("citrus.camel.cli.version");
    }

    @Test
    void shouldGetSpringBeanXmlDslMigrationGuide() {
        MigrationResources resources = createResources();

        TextResourceContents result = resources.citrus_docs_spring_bean_xml_dsl_migration();

        assertThat(result).isNotNull();
        assertThat(result.uri()).isEqualTo("citrus://docs/spring-bean-xml-dsl-migration");
        assertThat(result.text()).contains("\"javaSteps\"");
        assertThat(result.text()).contains("\"dependencies\"");
        assertThat(result.text()).contains("\"springRemovalSteps\"");
        assertThat(result.text()).contains("\"xmlTransformations\"");
        assertThat(result.text()).contains("\"knownLimitations\"");
    }

    @Test
    void shouldContainBindToRegistryInfo() {
        MigrationResources resources = createResources();

        TextResourceContents result = resources.citrus_docs_spring_bean_xml_dsl_migration();

        assertThat(result.text()).contains("@BindToRegistry");
        assertThat(result.text()).contains("@CitrusEndpoint");
        assertThat(result.text()).contains("@CitrusSupport");
    }

    @Test
    void shouldContainTestLoaderChange() {
        MigrationResources resources = createResources();

        TextResourceContents result = resources.citrus_docs_spring_bean_xml_dsl_migration();

        assertThat(result.text()).contains("TestLoader.SPRING");
        assertThat(result.text()).contains("TestLoader.XML");
    }
}
