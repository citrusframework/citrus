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

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SpringBeanXmlDslMigrationData {

    public MigrationGuide getGuide() {
        return new MigrationGuide(
                "Spring Bean XML to plain XML DSL migration guide for Citrus test cases. " +
                        "Covers XML test file migration (automated via XSLT tool), " +
                        "Java test class annotation changes, Maven dependency setup, " +
                        "and optional full Spring removal with @BindToRegistry.",
                createJavaSteps(),
                createDependencies(),
                createSpringRemovalSteps(),
                createXmlTransformations(),
                createKnownLimitations()
        );
    }

    private static List<MigrationStep> createJavaSteps() {
        return List.of(
                new MigrationStep(
                        "Change test loader type",
                        "Change @CitrusTestSource(type = TestLoader.SPRING) to @CitrusTestSource(type = TestLoader.XML) " +
                                "on each test method that loads a migrated XML test file.",
                        "@CitrusTestSource(type = TestLoader.SPRING)",
                        "@CitrusTestSource(type = TestLoader.XML)",
                        "Keep @CitrusSpringSupport and @ContextConfiguration unchanged — they are still needed " +
                                "for Spring bean configuration loading (citrus-context.xml, endpoint definitions, etc.).",
                        false
                ),
                new MigrationStep(
                        "Mixed loader types",
                        "A single Java test class can have some methods using TestLoader.SPRING and others using TestLoader.XML. " +
                                "The loader type is per-method (per @CitrusTestSource annotation), not per class. " +
                                "This is useful for incremental migration when only some XML test files in a module can be migrated.",
                        null,
                        null,
                        null,
                        false
                ),
                new MigrationStep(
                        "Java DSL tests unaffected",
                        "Tests that use @CitrusTest with Java DSL (not @CitrusTestSource) are unaffected by the XML migration. " +
                                "They don't load XML test files at all.",
                        null,
                        null,
                        null,
                        false
                ),
                new MigrationStep(
                        "File naming and default resolution",
                        "By default, name-based resolution constructs the filename as testName + '.xml'. " +
                                "If a test XML file uses a different naming pattern (e.g. .citrus.it.xml), " +
                                "set an explicit name on the @CitrusTestSource annotation to match. " +
                                "Without an explicit name, a .citrus.it.xml file will not be found by the default resolution, " +
                                "causing a vacuous test pass (empty method body succeeds without loading XML).",
                        null,
                        null,
                        "Watch for suspiciously fast test execution times (e.g. 6ms vs 3000ms+) " +
                                "as a signal that tests are passing vacuously.",
                        false
                )
        );
    }

    private static List<DependencyInfo> createDependencies() {
        return List.of(
                new DependencyInfo(
                        "org.citrusframework",
                        "citrus-xml",
                        "test",
                        "Required for TestLoader.XML. Add alongside citrus-spring."
                ),
                new DependencyInfo(
                        "org.citrusframework",
                        "citrus-spring",
                        "test",
                        "Still needed for @CitrusSpringSupport and Spring bean configuration loading. " +
                                "Can be removed only after full Spring removal (see spring_removal_steps)."
                )
        );
    }

    private static List<MigrationStep> createSpringRemovalSteps() {
        return List.of(
                new MigrationStep(
                        "Replace @CitrusSpringSupport with @CitrusSupport",
                        "Remove the Spring-specific test extension and use the plain Citrus JUnit5 extension instead.",
                        "@CitrusSpringSupport",
                        "@CitrusSupport",
                        null,
                        true
                ),
                new MigrationStep(
                        "Remove @ContextConfiguration",
                        "Remove @ContextConfiguration(classes = CitrusSpringConfig.class) since Spring context loading " +
                                "is no longer needed.",
                        "@ContextConfiguration(classes = CitrusSpringConfig.class)",
                        null,
                        null,
                        true
                ),
                new MigrationStep(
                        "Move beans to @BindToRegistry",
                        "Move endpoint and bean definitions from citrus-context.xml to Java fields annotated with @BindToRegistry. " +
                                "The field name becomes the bean name in the registry. " +
                                "For simple beans (data sources, validators, queues), declare the field and annotate it.",
                        """
                                <!-- citrus-context.xml -->
                                <bean id="testDataSource" class="org.apache.commons.dbcp2.BasicDataSource">
                                    <property name="driverClassName" value="org.hsqldb.jdbcDriver"/>
                                    <property name="url" value="jdbc:hsqldb:mem:test"/>
                                    <property name="username" value="sa"/>
                                    <property name="password" value=""/>
                                </bean>""",
                        """
                                @BindToRegistry
                                private final BasicDataSource testDataSource = new BasicDataSource();

                                @BeforeAll
                                public void setupDataSource() {
                                    testDataSource.setDriverClassName("org.hsqldb.jdbcDriver");
                                    testDataSource.setUrl("jdbc:hsqldb:mem:test");
                                    testDataSource.setUsername("sa");
                                    testDataSource.setPassword("");
                                }""",
                        "Use @TestInstance(TestInstance.Lifecycle.PER_CLASS) when using @BeforeAll with non-static setup.",
                        true
                ),
                new MigrationStep(
                        "Move endpoints to @CitrusEndpoint + @BindToRegistry",
                        "Endpoints need both @CitrusEndpoint (to create) and @BindToRegistry (to register by name) " +
                                "if the XML test references them by name via endpoint=\"...\" attribute. " +
                                "Each endpoint type has its own config annotation (e.g. @HttpClientConfig, @DirectEndpointConfig).",
                        """
                                <!-- citrus-context.xml -->
                                <citrus-http:client id="httpClient"
                                    request-url="http://localhost:8080/api"/>""",
                        """
                                @BindToRegistry
                                @CitrusEndpoint
                                @HttpClientConfig(requestUrl = "http://localhost:8080/api")
                                private HttpClient httpClient;""",
                        "Without @BindToRegistry the endpoint is created but not registered — " +
                                "XML tests using endpoint=\"httpClient\" will fail with 'unknown endpoint'.",
                        true
                ),
                new MigrationStep(
                        "Remove citrus-spring dependency",
                        "After all test classes in the module no longer use @CitrusSpringSupport, " +
                                "the citrus-spring dependency can be removed from pom.xml.",
                        null,
                        null,
                        "This is a separate concern from the XML test file migration and should be done as a follow-up.",
                        true
                )
        );
    }

    private static List<XmlTransformation> createXmlTransformations() {
        return List.of(
                new XmlTransformation("<spring:beans> wrapping <testcase>", "<test> root element"),
                new XmlTransformation("<meta-info> block (author, status, dates)", "author and status attributes on <test>"),
                new XmlTransformation("<message><data> / <header><element>", "<message><body><data> / <headers><header>"),
                new XmlTransformation("<validate> inside <message>", "<validate> at the action level"),
                new XmlTransformation("<statement> direct children of <sql>", "<statements><statement> wrapper"),
                new XmlTransformation("<resource file=\"...\"/> on <sql>", "<statements file=\"...\"/>"),
                new XmlTransformation("conditional@expression", "conditional@when"),
                new XmlTransformation("repeat-until-true", "repeat"),
                new XmlTransformation("repeat-onerror-until-true", "repeat-on-error"),
                new XmlTransformation("call-template", "apply-template"),
                new XmlTransformation("sleep@time (seconds)", "sleep with seconds attribute")
        );
    }

    private static List<KnownLimitation> createKnownLimitations() {
        return List.of(
                new KnownLimitation(
                        "<input> action",
                        "Interactive user input action is not supported in the plain XML DSL. " +
                                "Tests using <input> must remain on TestLoader.SPRING."
                ),
                new KnownLimitation(
                        "<java> action",
                        "Java method invocation action is not supported in the plain XML DSL. " +
                                "Tests using <java> must remain on TestLoader.SPRING."
                ),
                new KnownLimitation(
                        "Groovy actions in templates",
                        "Templates containing <groovy> script actions fail when loaded via the XML DSL loader " +
                                "due to JAXB unmarshalling issues."
                ),
                new KnownLimitation(
                        "Embedded template definitions",
                        "Spring Bean XML tests can define templates as <spring:bean> elements inline. " +
                                "The migration tool extracts these to separate XML files but <apply-template> elements " +
                                "need an explicit file attribute pointing to the extracted template file."
                ),
                new KnownLimitation(
                        "Schema validation on send without explicit schema",
                        "schema-validation=\"true\" on <send> and <receive> without an explicit schema or " +
                                "schema-repository attribute is silently ignored in the XML DSL."
                ),
                new KnownLimitation(
                        "Runtime modules (junit5, junit)",
                        "The runtime/citrus-junit5 and runtime/citrus-junit modules share XML test files between " +
                                "Spring-loading tests and non-Spring tests. Migrating these XML files would break " +
                                "the Spring XML loading tests."
                )
        );
    }

    public record MigrationGuide(
            String description,
            List<MigrationStep> javaSteps,
            List<DependencyInfo> dependencies,
            List<MigrationStep> springRemovalSteps,
            List<XmlTransformation> xmlTransformations,
            List<KnownLimitation> knownLimitations) {
    }

    public record MigrationStep(
            String title,
            String description,
            String before,
            String after,
            String note,
            boolean optional) {
    }

    public record DependencyInfo(
            String groupId,
            String artifactId,
            String scope,
            String note) {
    }

    public record XmlTransformation(
            String springBeanXml,
            String plainXmlDsl) {
    }

    public record KnownLimitation(
            String title,
            String description) {
    }
}
