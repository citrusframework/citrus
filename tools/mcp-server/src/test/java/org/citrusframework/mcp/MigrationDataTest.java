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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationDataTest {

    private final MigrationData migrationData = new MigrationData();

    @Test
    void shouldReturnGuideForVersion5() {
        MigrationData.MigrationGuide guide = migrationData.getGuide("5.0");

        assertThat(guide).isNotNull();
        assertThat(guide.targetVersion()).isEqualTo("5.0");
        assertThat(guide.title()).isEqualTo("Citrus 4.x to 5.x");
    }

    @Test
    void shouldReturnNullForUnknownVersion() {
        assertThat(migrationData.getGuide("99.0")).isNull();
    }

    @Test
    void shouldListAvailableVersions() {
        assertThat(migrationData.getAvailableVersions()).containsExactly("5.0");
    }

    @Test
    void shouldContainArtifactRenames() {
        MigrationData.MigrationGuide guide = migrationData.getGuide("5.0");

        assertThat(guide.artifactRenames()).isNotEmpty();
        assertThat(guide.artifactRenames()).anyMatch(r ->
                "citrus-junit5".equals(r.oldArtifactId()) && "citrus-junit-jupiter".equals(r.newArtifactId()));
    }

    @Test
    void shouldContainDependencyUpgrades() {
        MigrationData.MigrationGuide guide = migrationData.getGuide("5.0");

        assertThat(guide.dependencyUpgrades()).isNotEmpty();
        assertThat(guide.dependencyUpgrades()).anyMatch(u ->
                "Spring Framework".equals(u.dependency()) && "7.x".equals(u.newVersion()));
        assertThat(guide.dependencyUpgrades()).anyMatch(u ->
                "Jackson".equals(u.dependency()));
    }

    @Test
    void shouldContainPackageRenames() {
        MigrationData.MigrationGuide guide = migrationData.getGuide("5.0");

        assertThat(guide.packageRenames()).isNotEmpty();
        assertThat(guide.packageRenames()).anyMatch(r ->
                "org.citrusframework.TestActionSupport".equals(r.oldPackage()) &&
                        "org.citrusframework.dsl.TestActionSupport".equals(r.newPackage()));
        assertThat(guide.packageRenames()).anyMatch(r ->
                "org.citrusframework.actions.".equals(r.oldPackage()) &&
                        "org.citrusframework.api.actions.".equals(r.newPackage()));
    }

    @Test
    void shouldContainNotesForBroadPatterns() {
        MigrationData.MigrationGuide guide = migrationData.getGuide("5.0");

        assertThat(guide.packageRenames()).anyMatch(r ->
                "org.citrusframework.actions.".equals(r.oldPackage()) &&
                        r.note() != null && r.note().contains("Action builder interfaces only"));
        assertThat(guide.packageRenames()).anyMatch(r ->
                "org.citrusframework.container.".equals(r.oldPackage()) &&
                        r.note() != null && r.note().contains("Container interfaces only"));
        assertThat(guide.packageRenames()).anyMatch(r ->
                "org.citrusframework.openapi.".equals(r.oldPackage()) &&
                        r.note() != null && r.note().contains("OpenAPI API types only"));
        assertThat(guide.packageRenames()).anyMatch(r ->
                "org.citrusframework.validation.json.".equals(r.oldPackage()) &&
                        r.note() != null && r.note().contains("validation context classes"));
    }

    @Test
    void shouldNotContainRemovedBroadMessageBuilderPattern() {
        MigrationData.MigrationGuide guide = migrationData.getGuide("5.0");

        assertThat(guide.packageRenames()).noneMatch(r ->
                "org.citrusframework.message.builder.".equals(r.oldPackage()) &&
                        "org.citrusframework.base.message.builder.".equals(r.newPackage()));
    }

    @Test
    void shouldContainCamelCliPackageRenames() {
        MigrationData.MigrationGuide guide = migrationData.getGuide("5.0");

        assertThat(guide.packageRenames()).anyMatch(r ->
                "org.citrusframework.camel.jbang.CamelJBang".equals(r.oldPackage()) &&
                        "org.citrusframework.camel.cli.CamelCli".equals(r.newPackage()) &&
                        "citrus-camel".equals(r.module()));
        assertThat(guide.packageRenames()).anyMatch(r ->
                "org.citrusframework.camel.jbang.CamelJBangSettings".equals(r.oldPackage()) &&
                        "org.citrusframework.camel.cli.CamelCliSettings".equals(r.newPackage()));
        assertThat(guide.packageRenames()).anyMatch(r ->
                "org.citrusframework.actions.camel.CamelJBangActionBuilder".equals(r.oldPackage()) &&
                        "org.citrusframework.actions.camel.CamelCliActionBuilder".equals(r.newPackage()) &&
                        r.note() != null && r.note().contains("deprecated alias"));
    }

    @Test
    void shouldContainDslRenames() {
        MigrationData.MigrationGuide guide = migrationData.getGuide("5.0");

        assertThat(guide.dslRenames()).isNotEmpty();
        assertThat(guide.dslRenames()).anyMatch(r ->
                "java".equals(r.dslType()) &&
                        "camel().jbang()".equals(r.oldSyntax()) &&
                        "camel().cli()".equals(r.newSyntax()));
        assertThat(guide.dslRenames()).anyMatch(r ->
                "xml".equals(r.dslType()) &&
                        "<jbang>".equals(r.oldSyntax()) &&
                        "<cli>".equals(r.newSyntax()));
        assertThat(guide.dslRenames()).anyMatch(r ->
                "yaml".equals(r.dslType()) &&
                        "jbang:".equals(r.oldSyntax()) &&
                        "cli:".equals(r.newSyntax()));
    }

    @Test
    void shouldContainPropertyRenames() {
        MigrationData.MigrationGuide guide = migrationData.getGuide("5.0");

        assertThat(guide.propertyRenames()).isNotEmpty();
        assertThat(guide.propertyRenames()).anyMatch(r ->
                "citrus.camel.jbang.version".equals(r.oldProperty()) &&
                        "citrus.camel.cli.version".equals(r.newProperty()));
        assertThat(guide.propertyRenames()).anyMatch(r ->
                "citrus.camel.jbang.work.dir".equals(r.oldProperty()) &&
                        "citrus.camel.cli.work.dir".equals(r.newProperty()));
        assertThat(guide.propertyRenames()).anyMatch(r ->
                "citrus.camel.jbang.auto.remove.plugins".equals(r.oldProperty()) &&
                        "citrus.camel.cli.auto.remove.plugins".equals(r.newProperty()));
        assertThat(guide.propertyRenames()).allMatch(r ->
                r.note() != null && r.note().contains("fallback"));
    }

    @Test
    void shouldContainApiChanges() {
        MigrationData.MigrationGuide guide = migrationData.getGuide("5.0");

        assertThat(guide.apiChanges()).isNotEmpty();
        assertThat(guide.apiChanges()).anyMatch(c ->
                "interface-extraction".equals(c.type()) && c.summary().contains("CitrusContext"));
    }

    @Test
    void shouldContainSpiChanges() {
        MigrationData.MigrationGuide guide = migrationData.getGuide("5.0");

        assertThat(guide.spiChanges()).isNotEmpty();
        assertThat(guide.spiChanges()).anyMatch(s ->
                s.serviceFile().contains("CitrusContext") && "new".equals(s.changeType()));
    }
}
