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

import org.citrusframework.mcp.SpringBeanXmlDslMigrationData.MigrationStep;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpringBeanXmlDslMigrationDataTest {

    private final SpringBeanXmlDslMigrationData data = new SpringBeanXmlDslMigrationData();

    @Test
    void shouldReturnGuide() {
        SpringBeanXmlDslMigrationData.MigrationGuide guide = data.getGuide();

        assertThat(guide).isNotNull();
        assertThat(guide.description()).contains("Spring Bean XML");
    }

    @Test
    void shouldContainJavaSteps() {
        SpringBeanXmlDslMigrationData.MigrationGuide guide = data.getGuide();

        assertThat(guide.javaSteps()).isNotEmpty();
        assertThat(guide.javaSteps()).anyMatch(s ->
                s.title().contains("test loader type") &&
                        s.before() != null && s.before().contains("TestLoader.SPRING") &&
                        s.after() != null && s.after().contains("TestLoader.XML"));
    }

    @Test
    void shouldContainDependencies() {
        SpringBeanXmlDslMigrationData.MigrationGuide guide = data.getGuide();

        assertThat(guide.dependencies()).isNotEmpty();
        assertThat(guide.dependencies()).anyMatch(d ->
                "citrus-xml".equals(d.artifactId()) && "test".equals(d.scope()));
    }

    @Test
    void shouldContainSpringRemovalSteps() {
        SpringBeanXmlDslMigrationData.MigrationGuide guide = data.getGuide();

        assertThat(guide.springRemovalSteps()).isNotEmpty();
        assertThat(guide.springRemovalSteps()).anyMatch(s ->
                s.title().contains("@BindToRegistry"));
        assertThat(guide.springRemovalSteps()).anyMatch(s ->
                s.title().contains("@CitrusEndpoint"));
        assertThat(guide.springRemovalSteps()).anyMatch(s ->
                s.before() != null && s.before().contains("@CitrusSpringSupport") &&
                        s.after() != null && s.after().contains("@CitrusSupport"));
    }

    @Test
    void shouldContainXmlTransformations() {
        SpringBeanXmlDslMigrationData.MigrationGuide guide = data.getGuide();

        assertThat(guide.xmlTransformations()).isNotEmpty();
        assertThat(guide.xmlTransformations()).anyMatch(t ->
                t.springBeanXml().contains("spring:beans") && t.plainXmlDsl().contains("<test>"));
        assertThat(guide.xmlTransformations()).anyMatch(t ->
                t.springBeanXml().contains("call-template") && t.plainXmlDsl().contains("apply-template"));
    }

    @Test
    void shouldMarkJavaStepsAsRequired() {
        SpringBeanXmlDslMigrationData.MigrationGuide guide = data.getGuide();

        assertThat(guide.javaSteps()).allMatch(s -> !s.optional());
    }

    @Test
    void shouldMarkSpringRemovalStepsAsOptional() {
        SpringBeanXmlDslMigrationData.MigrationGuide guide = data.getGuide();

        assertThat(guide.springRemovalSteps()).allMatch(MigrationStep::optional);
    }

    @Test
    void shouldContainKnownLimitations() {
        SpringBeanXmlDslMigrationData.MigrationGuide guide = data.getGuide();

        assertThat(guide.knownLimitations()).isNotEmpty();
        assertThat(guide.knownLimitations()).anyMatch(l ->
                l.title().contains("<java>"));
        assertThat(guide.knownLimitations()).anyMatch(l ->
                l.title().contains("<input>"));
    }
}
