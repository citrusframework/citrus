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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MigrationData {

    private static final Map<String, MigrationGuide> GUIDES = new LinkedHashMap<>();

    static {
        GUIDES.put("5.0", createGuide5_0());
    }

    public MigrationGuide getGuide(String version) {
        return GUIDES.get(version);
    }

    public List<String> getAvailableVersions() {
        return List.copyOf(GUIDES.keySet());
    }

    private static MigrationGuide createGuide5_0() {
        return new MigrationGuide(
                "5.0",
                "Citrus 4.x to 5.x",
                "Migration guide for upgrading from Citrus 4.x to 5.0. " +
                        "Covers split package removal, Maven artifact renames, dependency upgrades, " +
                        "API changes, and SPI changes.",
                createArtifactRenames5_0(),
                createDependencyUpgrades5_0(),
                createPackageRenames5_0(),
                createApiChanges5_0(),
                createSpiChanges5_0()
        );
    }

    private static List<ArtifactRename> createArtifactRenames5_0() {
        List<ArtifactRename> renames = new ArrayList<>();
        renames.add(new ArtifactRename("org.citrusframework", "citrus-junit5", "citrus-junit-jupiter"));
        return renames;
    }

    private static List<DependencyUpgrade> createDependencyUpgrades5_0() {
        List<DependencyUpgrade> upgrades = new ArrayList<>();
        upgrades.add(new DependencyUpgrade("Spring Framework", "6.x", "7.x"));
        upgrades.add(new DependencyUpgrade("Spring Boot", "3.x", "4.x"));
        upgrades.add(new DependencyUpgrade("Spring WS", "4.x", "5.x"));
        upgrades.add(new DependencyUpgrade("Spring Integration", "6.x", "7.x"));
        upgrades.add(new DependencyUpgrade("Jackson", "2.x (com.fasterxml.jackson)", "3.x (tools.jackson)"));
        return upgrades;
    }

    private static List<PackageRename> createPackageRenames5_0() {
        List<PackageRename> renames = new ArrayList<>();

        // citrus-api: actions, containers, conditions, etc.
        renames.add(new PackageRename("org.citrusframework.actions.", "org.citrusframework.api.actions.", "citrus-api",
                "Action builder interfaces only; action implementations in citrus-base " +
                        "(e.g. CreateVariablesAction, SleepAction, ReceiveMessageAction) keep their original packages."));
        renames.add(new PackageRename("org.citrusframework.container.", "org.citrusframework.api.container.", "citrus-api",
                "Container interfaces only; implementations in citrus-base " +
                        "(e.g. SequenceAfterSuite, SequenceBeforeSuite, FinallySequence) keep their original packages."));
        renames.add(new PackageRename("org.citrusframework.condition.", "org.citrusframework.api.condition.", "citrus-api"));
        renames.add(new PackageRename("org.citrusframework.common.", "org.citrusframework.api.common.", "citrus-api"));
        renames.add(new PackageRename("org.citrusframework.agent.", "org.citrusframework.api.agent.", "citrus-api"));
        renames.add(new PackageRename("org.citrusframework.openapi.", "org.citrusframework.api.openapi.", "citrus-api",
                "OpenAPI API types only; connector-level classes in citrus-openapi " +
                        "(e.g. OpenApiSpecification, OpenApiActionBuilder) keep their original packages."));
        renames.add(new PackageRename("org.citrusframework.kubernetes.", "org.citrusframework.api.kubernetes.", "citrus-api"));
        renames.add(new PackageRename("org.citrusframework.main.", "org.citrusframework.api.main.", "citrus-api"));
        renames.add(new PackageRename("org.citrusframework.xml.namespace.", "org.citrusframework.api.xml.namespace.", "citrus-api"));
        renames.add(new PackageRename("org.citrusframework.xml.Marshaller", "org.citrusframework.api.xml.Marshaller", "citrus-api"));
        renames.add(new PackageRename("org.citrusframework.xml.Unmarshaller", "org.citrusframework.api.xml.Unmarshaller", "citrus-api"));
        renames.add(new PackageRename("org.citrusframework.xml.StringResult", "org.citrusframework.api.xml.StringResult", "citrus-api"));
        renames.add(new PackageRename("org.citrusframework.xml.StringSource", "org.citrusframework.api.xml.StringSource", "citrus-api"));
        renames.add(new PackageRename("org.citrusframework.yaml.", "org.citrusframework.api.yaml.", "citrus-api"));

        // citrus-api: validation contexts
        String validationContextNote = "Only validation context classes moved; validators and processors " +
                "in citrus-validation-* modules (e.g. XmlMarshallingValidationProcessor, " +
                "JsonMappingValidationProcessor) keep their original packages.";
        renames.add(new PackageRename("org.citrusframework.validation.json.", "org.citrusframework.validation.context.json.", "citrus-api", validationContextNote));
        renames.add(new PackageRename("org.citrusframework.validation.xml.", "org.citrusframework.validation.context.xml.", "citrus-api", validationContextNote));
        renames.add(new PackageRename("org.citrusframework.validation.yaml.", "org.citrusframework.validation.context.yaml.", "citrus-api", validationContextNote));
        renames.add(new PackageRename("org.citrusframework.validation.script.", "org.citrusframework.validation.context.script.", "citrus-api", validationContextNote));
        renames.add(new PackageRename("org.citrusframework.validation.ws.", "org.citrusframework.validation.context.ws.", "citrus-api", validationContextNote));
        renames.add(new PackageRename("org.citrusframework.validation.openapi.", "org.citrusframework.validation.context.openapi.", "citrus-api", validationContextNote));

        // citrus-api: utilities
        renames.add(new PackageRename("org.citrusframework.json.", "org.citrusframework.util.json.", "citrus-api"));
        renames.add(new PackageRename("org.citrusframework.yaml.YamlNodeStringBuilder", "org.citrusframework.util.yaml.YamlNodeStringBuilder", "citrus-api"));
        renames.add(new PackageRename("org.citrusframework.yaml.YamlStringBuilder", "org.citrusframework.util.yaml.YamlStringBuilder", "citrus-api"));

        // citrus-base
        renames.add(new PackageRename("org.citrusframework.functions.", "org.citrusframework.base.functions.", "citrus-base"));
        renames.add(new PackageRename("org.citrusframework.validation.matcher.", "org.citrusframework.base.validation.matcher.", "citrus-base"));
        renames.add(new PackageRename("org.citrusframework.report.", "org.citrusframework.base.report.", "citrus-base"));
        renames.add(new PackageRename("org.citrusframework.endpoint.adapter.", "org.citrusframework.base.endpoint.adapter.", "citrus-base"));
        renames.add(new PackageRename("org.citrusframework.endpoint.AbstractEndpointAdapter", "org.citrusframework.base.endpoint.AbstractEndpointAdapter", "citrus-base"));
        renames.add(new PackageRename("org.citrusframework.endpoint.AbstractEndpointBuilder", "org.citrusframework.base.endpoint.AbstractEndpointBuilder", "citrus-base"));
        renames.add(new PackageRename("org.citrusframework.endpoint.DefaultEndpointFactory", "org.citrusframework.base.endpoint.DefaultEndpointFactory", "citrus-base"));
        renames.add(new PackageRename("org.citrusframework.server.", "org.citrusframework.base.server.", "citrus-base"));
        renames.add(new PackageRename("org.citrusframework.context.StaticTestContextFactory", "org.citrusframework.base.context.StaticTestContextFactory", "citrus-base"));
        renames.add(new PackageRename("org.citrusframework.annotations.CitrusAnnotations", "org.citrusframework.base.annotations.CitrusAnnotations", "citrus-base"));
        renames.add(new PackageRename("org.citrusframework.main.AbstractTestEngine", "org.citrusframework.base.main.AbstractTestEngine", "citrus-base"));
        renames.add(new PackageRename("org.citrusframework.main.scan.", "org.citrusframework.base.main.scan.", "citrus-base"));

        // citrus-base: DSL classes
        renames.add(new PackageRename("org.citrusframework.DefaultTestActionBuilder", "org.citrusframework.dsl.DefaultTestActionBuilder", "citrus-base"));
        renames.add(new PackageRename("org.citrusframework.DefaultTestActions", "org.citrusframework.dsl.DefaultTestActions", "citrus-base"));
        renames.add(new PackageRename("org.citrusframework.TestActionSupport", "org.citrusframework.dsl.TestActionSupport", "citrus-base"));

        // citrus-docker (connector)
        renames.add(new PackageRename("org.citrusframework.actions.docker.", "org.citrusframework.docker.", "citrus-docker"));

        // citrus-spring
        renames.add(new PackageRename("org.citrusframework.config.xml.", "org.citrusframework.spring.config.xml.", "citrus-spring"));
        renames.add(new PackageRename("org.citrusframework.config.handler.", "org.citrusframework.spring.config.handler.", "citrus-spring"));
        renames.add(new PackageRename("org.citrusframework.config.util.", "org.citrusframework.spring.config.util.", "citrus-spring"));
        renames.add(new PackageRename("org.citrusframework.config.", "org.citrusframework.spring.config.", "citrus-spring"));
        renames.add(new PackageRename("org.citrusframework.context.SpringBeanReferenceResolver", "org.citrusframework.spring.context.SpringBeanReferenceResolver", "citrus-spring"));
        renames.add(new PackageRename("org.citrusframework.context.TestContextFactoryBean", "org.citrusframework.spring.context.TestContextFactoryBean", "citrus-spring"));
        renames.add(new PackageRename("org.citrusframework.CitrusSpringContext", "org.citrusframework.spring.CitrusSpringContext", "citrus-spring"));
        renames.add(new PackageRename("org.citrusframework.CitrusSpringContextProvider", "org.citrusframework.spring.CitrusSpringContextProvider", "citrus-spring"));
        renames.add(new PackageRename("org.citrusframework.CitrusSpringSettings", "org.citrusframework.spring.CitrusSpringSettings", "citrus-spring"));

        // citrus-spring-integration
        renames.add(new PackageRename("org.citrusframework.channel.", "org.citrusframework.springintegration.channel.", "citrus-spring-integration"));
        renames.add(new PackageRename("org.citrusframework.actions.PurgeMessageChannelAction", "org.citrusframework.springintegration.actions.PurgeMessageChannelAction", "citrus-spring-integration"));

        // citrus-groovy (runtime)
        renames.add(new PackageRename("org.citrusframework.script.", "org.citrusframework.groovy.actions.", "citrus-groovy"));
        renames.add(new PackageRename("org.citrusframework.message.builder.script.", "org.citrusframework.groovy.message.builder.", "citrus-groovy"));
        renames.add(new PackageRename("org.citrusframework.util.GroovyTypeConverter", "org.citrusframework.groovy.util.GroovyTypeConverter", "citrus-groovy"));

        // citrus-sql (connector)
        renames.add(new PackageRename("org.citrusframework.actions.AbstractDatabaseConnectingTestAction", "org.citrusframework.sql.actions.AbstractDatabaseConnectingTestAction", "citrus-sql"));
        renames.add(new PackageRename("org.citrusframework.actions.ExecutePLSQLAction", "org.citrusframework.sql.actions.ExecutePLSQLAction", "citrus-sql"));
        renames.add(new PackageRename("org.citrusframework.actions.ExecuteSQLAction", "org.citrusframework.sql.actions.ExecuteSQLAction", "citrus-sql"));
        renames.add(new PackageRename("org.citrusframework.actions.ExecuteSQLQueryAction", "org.citrusframework.sql.actions.ExecuteSQLQueryAction", "citrus-sql"));
        renames.add(new PackageRename("org.citrusframework.util.SqlUtils", "org.citrusframework.sql.util.SqlUtils", "citrus-sql"));

        // citrus-validation-xml
        renames.add(new PackageRename("org.citrusframework.variable.dictionary.xml.", "org.citrusframework.xml.variable.dictionary.", "citrus-validation-xml"));
        renames.add(new PackageRename("org.citrusframework.xml.XsdSchemaRepository", "org.citrusframework.xml.schema.XsdSchemaRepository", "citrus-validation-xml"));
        renames.add(new PackageRename("org.citrusframework.xml.XmlFormattingMessageProcessor", "org.citrusframework.xml.message.processor.XmlFormattingMessageProcessor", "citrus-validation-xml"));
        renames.add(new PackageRename("org.citrusframework.xml.LSResolverImpl", "org.citrusframework.xml.support.LSResolverImpl", "citrus-validation-xml"));
        renames.add(new PackageRename("org.citrusframework.xml.XmlConfigurer", "org.citrusframework.xml.support.XmlConfigurer", "citrus-validation-xml"));
        renames.add(new PackageRename("org.citrusframework.util.XMLUtils", "org.citrusframework.xml.support.XMLUtils", "citrus-validation-xml"));
        renames.add(new PackageRename("org.citrusframework.XmlValidationHelper", "org.citrusframework.xml.support.XmlValidationHelper", "citrus-validation-xml"));
        renames.add(new PackageRename("org.citrusframework.dsl.XmlSupport", "org.citrusframework.xml.dsl.XmlSupport", "citrus-validation-xml"));
        renames.add(new PackageRename("org.citrusframework.dsl.XpathSupport", "org.citrusframework.xml.dsl.XpathSupport", "citrus-validation-xml"));
        renames.add(new PackageRename("org.citrusframework.message.builder.MarshallingPayloadBuilder", "org.citrusframework.xml.message.builder.MarshallingPayloadBuilder", "citrus-validation-xml"));

        // citrus-validation-json
        renames.add(new PackageRename("org.citrusframework.variable.dictionary.json.", "org.citrusframework.json.variable.dictionary.", "citrus-validation-json"));
        renames.add(new PackageRename("org.citrusframework.dsl.JsonSupport", "org.citrusframework.json.dsl.JsonSupport", "citrus-validation-json"));
        renames.add(new PackageRename("org.citrusframework.dsl.JsonPathSupport", "org.citrusframework.json.dsl.JsonPathSupport", "citrus-validation-json"));
        renames.add(new PackageRename("org.citrusframework.message.builder.ObjectMappingPayloadBuilder", "org.citrusframework.json.message.builder.ObjectMappingPayloadBuilder", "citrus-validation-json"));

        // citrus-validation-groovy
        renames.add(new PackageRename("org.citrusframework.validation.script.Groovy", "org.citrusframework.validation.groovy.Groovy", "citrus-validation-groovy"));

        // citrus-validation-hamcrest
        renames.add(new PackageRename("org.citrusframework.validation.matcher.hamcrest.", "org.citrusframework.validation.hamcrest.matcher.", "citrus-validation-hamcrest"));
        renames.add(new PackageRename("org.citrusframework.validation.HamcrestHeaderValidator", "org.citrusframework.validation.hamcrest.HamcrestHeaderValidator", "citrus-validation-hamcrest"));
        renames.add(new PackageRename("org.citrusframework.validation.HamcrestValueMatcher", "org.citrusframework.validation.hamcrest.HamcrestValueMatcher", "citrus-validation-hamcrest"));
        renames.add(new PackageRename("org.citrusframework.container.HamcrestConditionExpression", "org.citrusframework.validation.hamcrest.HamcrestConditionExpression", "citrus-validation-hamcrest"));

        // citrus-jbang (tools)
        renames.add(new PackageRename("org.citrusframework.jbang.", "org.citrusframework.jbang.cli.", "citrus-jbang"));

        return renames;
    }

    private static List<ApiChange> createApiChanges5_0() {
        List<ApiChange> changes = new ArrayList<>();
        changes.add(new ApiChange("interface-extraction",
                "CitrusContext is now an interface",
                "The concrete CitrusContext class has been extracted into an interface in citrus-api. " +
                        "The implementation is now DefaultCitrusContext in citrus-base, discovered via ServiceLoader. " +
                        "Use CitrusContext.newInstance() instead of new CitrusContext()."));
        changes.add(new ApiChange("interface-extraction",
                "TestContextFactory is now an interface",
                "The concrete TestContextFactory class has been extracted into an interface in citrus-api. " +
                        "The implementation is now DefaultTestContextFactory in citrus-base, discovered via ServiceLoader. " +
                        "Use TestContextFactory.newInstance() instead of new TestContextFactory()."));
        changes.add(new ApiChange("new-interface",
                "New container interfaces: IteratingActionContainer, TestBoundaryActionContainer, TestSuiteActionContainer",
                "Three new interfaces introduced in citrus-api under org.citrusframework.api.container. " +
                        "These are transparent additions - existing container implementations now implement these interfaces."));
        changes.add(new ApiChange("class-relocation",
                "DSL classes relocated to org.citrusframework.dsl",
                "DefaultTestActionBuilder, DefaultTestActions, and TestActionSupport moved from " +
                        "org.citrusframework to org.citrusframework.dsl."));
        changes.add(new ApiChange("class-deletion",
                "Deleted: AbstractEndpointFactoryBean and AbstractServerFactoryBean",
                "These Spring XML factory beans have been removed without replacement. " +
                        "Use AbstractEndpointParser / AbstractServerParser (now in org.citrusframework.spring.config.xml) instead."));
        return changes;
    }

    private static List<SpiChange> createSpiChanges5_0() {
        List<SpiChange> changes = new ArrayList<>();
        changes.add(new SpiChange(
                "META-INF/services/org.citrusframework.CitrusContext",
                "org.citrusframework.base.DefaultCitrusContext",
                "new"));
        changes.add(new SpiChange(
                "META-INF/services/org.citrusframework.context.TestContextFactory",
                "org.citrusframework.base.context.DefaultTestContextFactory",
                "new"));
        changes.add(new SpiChange(
                "META-INF/citrus/ SPI files",
                "~43 internal SPI files updated with new fully-qualified class names",
                "updated"));
        changes.add(new SpiChange(
                "spring.handlers",
                "Updated for citrus-spring and citrus-spring-integration to reflect new namespace handler packages",
                "updated"));
        return changes;
    }

    public record MigrationGuide(
            String targetVersion,
            String title,
            String description,
            List<ArtifactRename> artifactRenames,
            List<DependencyUpgrade> dependencyUpgrades,
            List<PackageRename> packageRenames,
            List<ApiChange> apiChanges,
            List<SpiChange> spiChanges
    ) {}

    public record ArtifactRename(String groupId, String oldArtifactId, String newArtifactId) {}

    public record DependencyUpgrade(String dependency, String oldVersion, String newVersion) {}

    public record PackageRename(String oldPackage, String newPackage, String module, String note) {
        public PackageRename(String oldPackage, String newPackage, String module) {
            this(oldPackage, newPackage, module, null);
        }
    }

    public record ApiChange(String type, String summary, String details) {}

    public record SpiChange(String serviceFile, String implementation, String changeType) {}
}
